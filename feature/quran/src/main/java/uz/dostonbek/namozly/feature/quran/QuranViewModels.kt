package uz.dostonbek.namozly.feature.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.domain.repository.BookmarkRepository
import uz.dostonbek.namozly.core.domain.repository.QuranPlayer
import uz.dostonbek.namozly.core.domain.repository.QuranRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Ayah
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.Surah
import uz.dostonbek.namozly.feature.quran.navigation.SurahRoute
import javax.inject.Inject

enum class QuranTab { SURAHS, JUZ, SAVED }

data class SurahListUiState(
    val surahs: List<Surah> = emptyList(),
    val query: String = "",
    val tab: QuranTab = QuranTab.SURAHS,
    val lastRead: Pair<Surah, Int>? = null,
    val bookmarks: List<Pair<Surah, Int>> = emptyList(),
    val language: AppLanguage = AppLanguage.UZ_LATN,
)

@HiltViewModel
class SurahListViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val player: QuranPlayer,
    bookmarks: BookmarkRepository,
    settings: SettingsRepository,
) : ViewModel() {
    /** Ro'yxatdan turib butun surani tinglash. */
    val playback: StateFlow<PlaybackState> = player.state

    init {
        // Ro'yxatdan boshlangan sura tugasa — keyingisi avtomatik (oyatma-oyat ijro emas, butun sura).
        viewModelScope.launch {
            player.state.collect { s ->
                val n = s.surah
                if (s.ended && s.ayah == null && s.streamTitle == null && n != null && n < 114) player.playSurah(n + 1)
            }
        }
    }

    fun toggleSurah(n: Int) {
        val s = player.state.value
        when {
            s.surah == n && s.ayah == null && s.isPlaying -> player.pause()
            s.surah == n && s.ayah == null && !s.ended && (s.positionMs > 0 || s.isBuffering) -> player.resume()
            else -> player.playSurah(n)
        }
    }

    fun skip(delta: Int) {
        val n = player.state.value.surah ?: return
        (n + delta).takeIf { it in 1..114 }?.let { player.playSurah(it) }
    }

    fun stop() = player.stop()

    private val query = MutableStateFlow("")
    private val tab = MutableStateFlow(QuranTab.SURAHS)
    private val all = repository.surahs()

    val uiState: StateFlow<SurahListUiState> = combine(
        query, tab, repository.lastRead, bookmarks.observe(), settings.settings.map { it.language },
    ) { q, tb, last, marks, lang ->
        val needle = q.trim()
        val filtered = if (needle.isBlank()) all else all.filter { s ->
            s.number.toString() == needle ||
                listOf(s.name.uz, s.name.uzCyrl, s.name.ru, s.name.en, s.arabicName).any { it.contains(needle, ignoreCase = true) }
        }
        SurahListUiState(
            surahs = filtered,
            query = q,
            tab = tb,
            lastRead = last?.let { (s, a) -> all.getOrNull(s - 1)?.let { it to a } },
            bookmarks = marks.mapNotNull { (s, a) -> all.getOrNull(s - 1)?.let { it to a } },
            language = lang,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SurahListUiState(all))

    fun onQuery(value: String) { query.value = value }
    fun onTab(value: QuranTab) { tab.value = value }
    fun surah(n: Int): Surah? = all.getOrNull(n - 1)
}

data class SurahDetailUiState(
    val surah: Surah? = null,
    val ayahs: List<Ayah> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
    val initialAyah: Int = 0,
    val language: AppLanguage = AppLanguage.UZ_LATN,
    val bookmarked: Set<Int> = emptySet(),
    val repeat: Boolean = false,
)

@HiltViewModel
class SurahDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
    private val player: QuranPlayer,
    private val bookmarks: BookmarkRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<SurahRoute>()
    private val surah = repository.surahs().getOrNull(route.number - 1)
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow(false)
    private val repeat = MutableStateFlow(false)
    /** Tarjima tili — standart: ilova tili, ekranda alohida almashtiriladi. */
    private val language = MutableStateFlow<AppLanguage?>(null)

    val playback: StateFlow<PlaybackState> = player.state

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SurahDetailUiState> = language.filterNotNull().distinctUntilChanged()
        .flatMapLatest { lang ->
            sync(lang)
            combine(repository.observeAyahs(route.number, lang), loading, error, bookmarks.observe(), repeat) { ayahs, l, e, marks, rep ->
                SurahDetailUiState(
                    surah = surah,
                    ayahs = ayahs,
                    loading = l && ayahs.isEmpty(),
                    error = e && ayahs.isEmpty(),
                    initialAyah = route.ayah,
                    language = lang,
                    bookmarked = marks.filter { it.first == route.number }.map { it.second }.toSet(),
                    repeat = rep,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SurahDetailUiState(surah = surah, initialAyah = route.ayah))

    init {
        viewModelScope.launch { language.value = settingsRepository.settings.first().language }
        // Oyat tugaganda keyingisiga avtomatik o'tish (yoki takrorlash).
        viewModelScope.launch {
            player.state.collect { s ->
                val endedAyah = s.ayah
                if (s.ended && s.surah == route.number && endedAyah != null) {
                    val list = uiState.value.ayahs
                    val target = if (repeat.value) list.firstOrNull { it.number == endedAyah } else list.firstOrNull { it.number == endedAyah + 1 }
                    if (target != null) player.playAyah(target.surah, target.number, target.globalNumber)
                }
            }
        }
    }

    private fun sync(lang: AppLanguage) {
        viewModelScope.launch {
            loading.value = true
            error.value = repository.sync(route.number, lang).isFailure
            loading.value = false
        }
    }

    fun setLanguage(lang: AppLanguage) { language.value = lang }
    fun retry() { language.value?.let(::sync) }

    fun togglePlay() {
        val s = player.state.value
        val list = uiState.value.ayahs
        when {
            s.surah == route.number && s.isPlaying -> player.pause()
            s.surah == route.number && s.ayah != null && !s.ended -> player.resume()
            else -> list.firstOrNull { it.number == (s.ayah?.takeIf { s.surah == route.number } ?: 1) }
                ?.let { player.playAyah(it.surah, it.number, it.globalNumber) }
        }
    }

    fun playAyah(ayah: Ayah) {
        val s = player.state.value
        if (s.surah == ayah.surah && s.ayah == ayah.number && s.isPlaying) player.pause()
        else player.playAyah(ayah.surah, ayah.number, ayah.globalNumber)
        markRead(ayah.number)
    }

    fun step(delta: Int) {
        val s = player.state.value
        val current = s.ayah?.takeIf { s.surah == route.number } ?: 1
        uiState.value.ayahs.firstOrNull { it.number == current + delta }?.let { playAyah(it) }
    }

    fun toggleRepeat() { repeat.value = !repeat.value }

    fun cycleSpeed() {
        val speeds = listOf(1f, 1.25f, 1.5f, 0.75f)
        val i = speeds.indexOf(player.state.value.speed).let { if (it < 0) 0 else it }
        player.setSpeed(speeds[(i + 1) % speeds.size])
    }

    fun toggleBookmark(ayah: Int) = viewModelScope.launch { bookmarks.toggle(route.number, ayah) }
    fun markRead(ayah: Int) = viewModelScope.launch { repository.setLastRead(route.number, ayah) }
}
