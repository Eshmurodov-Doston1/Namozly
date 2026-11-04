package uz.dostonbek.namozly.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.domain.repository.PrayerLogRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerStreak
import uz.dostonbek.namozly.core.domain.repository.QuranPlayer
import uz.dostonbek.namozly.core.domain.repository.QuranRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.usecase.ObserveTodayPrayersUseCase
import uz.dostonbek.namozly.core.domain.usecase.TodayPrayers
import uz.dostonbek.namozly.core.domain.util.AyahOfDayPicker
import uz.dostonbek.namozly.core.domain.util.HijriCalendar
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.AyahOfDay
import uz.dostonbek.namozly.core.model.CalculationMethod
import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.HijriDate
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.UserLocation
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val location: UserLocation = UserLocation.Default,
    val today: TodayPrayers? = null,
    val hijri: HijriDate? = null,
    val ayah: AyahOfDay? = null,
    val mosques: List<Mosque> = emptyList(),
    val unread: Int = 0,
    val language: AppLanguage = AppLanguage.UZ_LATN,
    val method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val qiblaBearing: Double = 0.0,
    val prayedToday: Set<Prayer> = emptySet(),
    val streak: Int = 0,
    val ramadanStart: LocalDate? = null,
    val ramadanDay: Int = 0,
    val daysToFriday: Int = 0,
    val guidesCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeToday: ObserveTodayPrayersUseCase,
    private val locationRepository: LocationRepository,
    private val quranRepository: QuranRepository,
    private val mosqueRepository: MosqueRepository,
    notificationRepository: NotificationRepository,
    settingsRepository: SettingsRepository,
    private val prayerLog: PrayerLogRepository,
    content: ContentRepository,
    private val scheduler: PrayerAlarmScheduler,
    private val player: QuranPlayer,
    private val time: TimeProvider,
) : ViewModel() {

    private val settings = settingsRepository.settings
    private val language = settings.map { it.language }.distinctUntilChanged()
    private val guidesCount = content.prayerGuides(Gender.MALE).size

    @OptIn(ExperimentalCoroutinesApi::class)
    private val ayah = language.flatMapLatest { lang ->
        flow<AyahOfDay?> {
            emit(null)
            val (s, a) = AyahOfDayPicker.pick(time.today())
            val surah = quranRepository.surahs().getOrNull(s - 1)
            val ayah = quranRepository.ayah(s, a, lang)
            emit(if (surah != null && ayah != null) AyahOfDay(surah, ayah) else null)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val mosques = locationRepository.location
        .map { it.point }
        .distinctUntilChanged()
        .flatMapLatest { point ->
            flow<List<Mosque>> {
                emit(emptyList())
                emit(mosqueRepository.nearby(point, 2500).getOrDefault(emptyList()).take(6))
            }
        }

    val playback: StateFlow<PlaybackState> = player.state

    private data class Extras(
        val unread: Int,
        val language: AppLanguage,
        val method: CalculationMethod,
        val log: Map<LocalDate, Set<Prayer>>,
    )

    private val extras = combine(
        notificationRepository.unreadCount(),
        language,
        settings.map { it.prayer.method }.distinctUntilChanged(),
        prayerLog.observe(),
    ) { unread, lang, method, log -> Extras(unread, lang, method, log) }

    val uiState: StateFlow<HomeUiState> = combine(
        locationRepository.location,
        observeToday(),
        ayah,
        mosques,
        extras,
    ) { location, today, ayah, mosques, ex ->
        val date = today.today.date
        val hijri = today.today.hijri ?: HijriCalendar.toHijri(date)
        HomeUiState(
            location = location,
            today = today,
            hijri = hijri,
            ayah = ayah,
            mosques = mosques,
            unread = ex.unread,
            language = ex.language,
            method = ex.method,
            qiblaBearing = QiblaCalculator.bearing(location.point),
            prayedToday = ex.log[date].orEmpty(),
            streak = PrayerStreak.streak(ex.log, date),
            ramadanStart = nextRamadanStart(date, hijri),
            ramadanDay = if (hijri.month == 9) hijri.day else 0,
            daysToFriday = ChronoUnit.DAYS.between(date, date.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))).toInt(),
            guidesCount = guidesCount,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun nextRamadanStart(date: LocalDate, hijri: HijriDate): LocalDate? {
        val year = if (hijri.month < 9) hijri.year else hijri.year + 1
        return HijriCalendar.toGregorian(HijriDate(1, 9, year))?.takeIf { it.isAfter(date) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) locationRepository.refreshFromDevice()
            scheduler.rescheduleAll()
        }
    }

    fun togglePrayed(prayer: Prayer) {
        viewModelScope.launch { prayerLog.toggle(time.today(), prayer) }
    }

    fun toggleAyahAudio() {
        val a = uiState.value.ayah?.ayah ?: return
        val s = player.state.value
        when {
            s.surah == a.surah && s.ayah == a.number && s.isPlaying -> player.pause()
            s.surah == a.surah && s.ayah == a.number -> player.resume()
            else -> player.playAyah(a.surah, a.number, a.globalNumber)
        }
    }
}
