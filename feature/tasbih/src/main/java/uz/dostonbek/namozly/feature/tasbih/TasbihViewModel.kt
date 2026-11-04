package uz.dostonbek.namozly.feature.tasbih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.dostonbek.namozly.core.common.TimeProvider
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.LeaderboardRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.repository.TasbihRepository
import uz.dostonbek.namozly.core.model.Dhikr
import uz.dostonbek.namozly.core.model.TasbihEntry
import uz.dostonbek.namozly.core.model.TasbihStats
import uz.dostonbek.namozly.core.model.TasbihStyle
import javax.inject.Inject

data class TasbihUiState(
    val dhikrs: List<Dhikr> = emptyList(),
    val dhikr: Dhikr? = null,
    val count: Int = 0,
    /** 0 — cheksiz (∞) */
    val target: Int = 33,
    val rounds: Int = 0,
    val style: TasbihStyle = TasbihStyle.CLASSIC,
    val haptics: Boolean = true,
    val stats: TasbihStats = TasbihStats(0, 0, 0, 0, emptyMap()),
) {
    val progress: Float get() = if (target <= 0) 0f else (count % target).toFloat() / target
    val inRound: Int get() = if (target <= 0) count else count % target
}

sealed interface TasbihEvent {
    data object Tick : TasbihEvent
    data object TargetReached : TasbihEvent
}

@HiltViewModel
class TasbihViewModel @Inject constructor(
    content: ContentRepository,
    private val repository: TasbihRepository,
    private val settings: SettingsRepository,
    private val leaderboard: LeaderboardRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val session = MutableStateFlow(Session(dhikrId = content.dhikrs().first().id))
    private val dhikrs = content.dhikrs()
    private var unsaved = 0

    private data class Session(val dhikrId: String, val count: Int = 0, val rounds: Int = 0)

    val uiState: StateFlow<TasbihUiState> = combine(session, settings.settings, repository.observeStats()) { s, st, stats ->
        TasbihUiState(
            dhikrs = dhikrs,
            dhikr = dhikrs.firstOrNull { it.id == s.dhikrId },
            count = s.count,
            target = st.tasbihTarget,
            rounds = s.rounds,
            style = st.tasbihStyle,
            haptics = st.haptics,
            stats = stats,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasbihUiState(dhikrs = dhikrs, dhikr = dhikrs.first()))

    /** @return hodisa turi — UI tebranish/animatsiya uchun. */
    fun increment(): TasbihEvent {
        val target = uiState.value.target
        var reached = false
        session.update { s ->
            val c = s.count + 1
            reached = target > 0 && c % target == 0
            s.copy(count = c, rounds = if (reached) s.rounds + 1 else s.rounds)
        }
        unsaved++
        if (reached) persist()
        return if (reached) TasbihEvent.TargetReached else TasbihEvent.Tick
    }

    fun reset() {
        persist()
        session.update { it.copy(count = 0, rounds = 0) }
    }

    fun selectDhikr(dhikr: Dhikr) {
        if (dhikr.id == session.value.dhikrId) return
        persist()
        session.value = Session(dhikr.id)
    }

    fun setTarget(target: Int) = viewModelScope.launch { settings.setTasbihTarget(target.coerceAtLeast(0)) }
    fun setStyle(style: TasbihStyle) = viewModelScope.launch { settings.setTasbihStyle(style) }

    private fun persist() {
        val toSave = unsaved
        if (toSave <= 0) return
        unsaved = 0
        val s = session.value
        val target = uiState.value.target
        viewModelScope.launch {
            withContext(NonCancellable) {
                repository.save(TasbihEntry(dhikrId = s.dhikrId, count = toSave, target = target, timestamp = time.millis()))
                if (leaderboard.profile.first() != null) leaderboard.submit(toSave)
            }
        }
    }

    fun flush() = persist()
}
