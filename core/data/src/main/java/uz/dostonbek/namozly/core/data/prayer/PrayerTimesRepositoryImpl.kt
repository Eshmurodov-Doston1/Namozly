package uz.dostonbek.namozly.core.data.prayer

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.dostonbek.namozly.core.common.ApplicationScope
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.database.dao.PrayerDayDao
import uz.dostonbek.namozly.core.database.entity.PrayerDayEntity
import uz.dostonbek.namozly.core.datastore.NamozlyPreferencesDataSource
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.HijriDate
import uz.dostonbek.namozly.core.model.Madhab
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.PrayerSettings
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.network.api.AladhanService
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manba strategiyasi (offline-first):
 * 1) Room keshida Aladhan ma'lumoti bo'lsa — o'sha ko'rsatiladi;
 * 2) bo'lmasa darhol oflayn (adhan) hisob ko'rsatiladi va fonda oy keshi yuklanadi;
 * 3) har ikkala manbaga foydalanuvchi tuzatishlari qo'llanadi.
 */
@Singleton
class PrayerTimesRepositoryImpl @Inject constructor(
    private val aladhan: AladhanService,
    private val dao: PrayerDayDao,
    private val preferences: NamozlyPreferencesDataSource,
    private val locationRepository: LocationRepository,
    private val offline: OfflinePrayerCalculator,
    @ApplicationScope private val appScope: CoroutineScope,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : PrayerTimesRepository {

    private val fetchMutex = Mutex()
    private val fetched = mutableSetOf<String>()

    private data class Context(val location: UserLocation, val settings: PrayerSettings) {
        val key: String = String.format(
            Locale.US, "%.2f,%.2f|%s|%s",
            location.point.latitude, location.point.longitude, settings.method.name, settings.madhab.name,
        )
    }

    private val context: Flow<Context> = combine(
        locationRepository.location,
        preferences.settings.map { it.prayer }.distinctUntilChanged(),
    ) { loc, s -> Context(loc, s) }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDay(date: LocalDate): Flow<DailyPrayerTimes> = context.flatMapLatest { ctx ->
        val fallback = offline.calculate(date, ctx.location.point, ctx.settings)
        if (!ctx.settings.useOnlineSource) {
            flowOf(fallback.adjusted(ctx.settings.adjustments))
        } else {
            dao.observe(ctx.key, date.iso())
                .map { (it?.toModel() ?: fallback).adjusted(ctx.settings.adjustments) }
                .onStart { ensureMonth(ctx, YearMonth.from(date)) }
        }
    }.distinctUntilChanged().flowOn(io)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMonth(year: Int, month: Int): Flow<List<DailyPrayerTimes>> = context.flatMapLatest { ctx ->
        val ym = YearMonth.of(year, month)
        val days = (1..ym.lengthOfMonth()).map { ym.atDay(it) }
        val fallback = days.map { offline.calculate(it, ctx.location.point, ctx.settings) }
        if (!ctx.settings.useOnlineSource) {
            flowOf(fallback.map { it.adjusted(ctx.settings.adjustments) })
        } else {
            dao.observeRange(ctx.key, days.first().iso(), days.last().iso())
                .map { cached ->
                    val byDate = cached.associateBy { it.date }
                    fallback.map { off -> (byDate[off.date.iso()]?.toModel() ?: off).adjusted(ctx.settings.adjustments) }
                }
                .onStart { ensureMonth(ctx, ym) }
        }
    }.flowOn(io)

    override suspend fun getRange(from: LocalDate, days: Int): List<DailyPrayerTimes> = withContext(io) {
        val ctx = context.first()
        val to = from.plusDays((days - 1).toLong())
        val cached = if (ctx.settings.useOnlineSource) {
            dao.range(ctx.key, from.iso(), to.iso()).associateBy { it.date }
        } else emptyMap()
        (0 until days).map { i ->
            val d = from.plusDays(i.toLong())
            (cached[d.iso()]?.toModel() ?: offline.calculate(d, ctx.location.point, ctx.settings))
                .adjusted(ctx.settings.adjustments)
        }
    }

    override suspend fun refresh(months: Int) = withContext(io) {
        val ctx = context.first()
        if (!ctx.settings.useOnlineSource) return@withContext
        val now = YearMonth.now()
        repeat(months.coerceAtLeast(1)) { fetchMonth(ctx, now.plusMonths(it.toLong())) }
    }

    private fun ensureMonth(ctx: Context, ym: YearMonth) {
        appScope.launch(io) { fetchMonth(ctx, ym) }
    }

    private suspend fun fetchMonth(ctx: Context, ym: YearMonth) {
        val tag = "${ctx.key}#$ym"
        run {
            fetchMutex.withLock {
                if (tag in fetched) return
                val existing = dao.range(ctx.key, ym.atDay(1).iso(), ym.atEndOfMonth().iso())
                if (existing.size >= ym.lengthOfMonth()) {
                    fetched += tag; return
                }
                runSuspendCatching {
                    aladhan.calendar(
                        year = ym.year,
                        month = ym.monthValue,
                        latitude = ctx.location.point.latitude,
                        longitude = ctx.location.point.longitude,
                        method = ctx.settings.method.aladhanId,
                        school = if (ctx.settings.madhab == Madhab.HANAFI) 1 else 0,
                    )
                }.onSuccess { response ->
                    val now = System.currentTimeMillis()
                    val entities = response.data.mapNotNull { day ->
                        val parts = day.date.gregorian.date.split("-")
                        if (parts.size != 3) return@mapNotNull null
                        PrayerDayEntity(
                            locationKey = ctx.key,
                            date = "${parts[2]}-${parts[1]}-${parts[0]}",
                            fajr = day.timings.fajr.clean(),
                            sunrise = day.timings.sunrise.clean(),
                            dhuhr = day.timings.dhuhr.clean(),
                            asr = day.timings.asr.clean(),
                            maghrib = day.timings.maghrib.clean(),
                            isha = day.timings.isha.clean(),
                            hijriDay = day.date.hijri.day.toIntOrNull() ?: 0,
                            hijriMonth = day.date.hijri.month.number,
                            hijriYear = day.date.hijri.year.toIntOrNull() ?: 0,
                            fetchedAt = now,
                        )
                    }
                    if (entities.isNotEmpty()) {
                        dao.upsert(entities)
                        fetched += tag
                    }
                }
            }
        }
    }

    private fun PrayerDayEntity.toModel(): DailyPrayerTimes = DailyPrayerTimes(
        date = LocalDate.parse(date),
        times = mapOf(
            Prayer.FAJR to LocalTime.parse(fajr),
            Prayer.SUNRISE to LocalTime.parse(sunrise),
            Prayer.DHUHR to LocalTime.parse(dhuhr),
            Prayer.ASR to LocalTime.parse(asr),
            Prayer.MAGHRIB to LocalTime.parse(maghrib),
            Prayer.ISHA to LocalTime.parse(isha),
        ),
        hijri = if (hijriYear > 0) HijriDate(hijriDay, hijriMonth, hijriYear) else null,
        source = DailyPrayerTimes.Source.API,
    )

    /** "05:44 (+05)" -> "05:44" */
    private fun String.clean(): String = trim().take(5)

    private fun LocalDate.iso(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
}
