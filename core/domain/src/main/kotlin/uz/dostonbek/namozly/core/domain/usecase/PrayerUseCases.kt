package uz.dostonbek.namozly.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.domain.util.PrayerMomentCalculator
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.PrayerMoment
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class TodayPrayers(
    val now: LocalDateTime,
    val today: DailyPrayerTimes,
    val tomorrow: DailyPrayerTimes,
    val moment: PrayerMoment,
) {
    /** Joriy namoz oralig'ining necha foizi o'tgani (0..1) — progress halqasi uchun. */
    val progress: Float
        get() {
            val startPrayer = moment.current ?: Prayer.SUNRISE
            val start = if (startPrayer == Prayer.ISHA && now.toLocalDate() == today.date && now.isBefore(today.dateTime(Prayer.FAJR))) {
                today.dateTime(Prayer.ISHA).minusDays(1)
            } else today.dateTime(startPrayer)
            val total = java.time.Duration.between(start, moment.nextAt).seconds.toFloat()
            val done = java.time.Duration.between(start, now).seconds.toFloat()
            return if (total <= 0f) 0f else (done / total).coerceIn(0f, 1f)
        }
}

/** Bosh sahifa: har soniyada yangilanadigan hozirgi/keyingi namoz holati. */
class ObserveTodayPrayersUseCase @Inject constructor(
    private val repository: PrayerTimesRepository,
    private val time: TimeProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<TodayPrayers> =
        ticker(1_000).flatMapLatestDay { date ->
            combine(
                repository.observeDay(date),
                repository.observeDay(date.plusDays(1)),
                ticker(1_000),
            ) { today, tomorrow, now ->
                TodayPrayers(now, today, tomorrow, PrayerMomentCalculator.compute(now, today, tomorrow))
            }
        }

    private fun ticker(periodMs: Long): Flow<LocalDateTime> = flow {
        while (true) {
            emit(time.now())
            delay(periodMs)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <R> Flow<LocalDateTime>.flatMapLatestDay(block: (LocalDate) -> Flow<R>): Flow<R> =
        flowOfDates(this).flatMapLatest(block)

    private fun flowOfDates(source: Flow<LocalDateTime>): Flow<LocalDate> = flow {
        source.collect { emit(it.toLocalDate()) }
    }.distinctUntilChanged()
}

class ObserveMonthPrayersUseCase @Inject constructor(
    private val repository: PrayerTimesRepository,
) {
    operator fun invoke(year: Int, month: Int): Flow<List<DailyPrayerTimes>> = repository.observeMonth(year, month)
}
