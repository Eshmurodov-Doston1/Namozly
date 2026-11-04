package uz.dostonbek.namozly.feature.ramadan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHeader
import uz.dostonbek.namozly.core.designsystem.component.DOverline
import uz.dostonbek.namozly.core.designsystem.component.DProgress
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.domain.usecase.ObserveTodayPrayersUseCase
import uz.dostonbek.namozly.core.domain.usecase.TodayPrayers
import uz.dostonbek.namozly.core.domain.util.HijriCalendar
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.Dua
import uz.dostonbek.namozly.core.model.DuaCategory
import uz.dostonbek.namozly.core.model.HijriDate
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.countdown
import uz.dostonbek.namozly.core.ui.hhmm
import uz.dostonbek.namozly.core.ui.localized
import java.time.Duration
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@Serializable data object RamadanRoute

fun NavController.navigateToRamadan() = navigate(RamadanRoute)

fun NavGraphBuilder.ramadanScreen(onBack: () -> Unit) {
    composable<RamadanRoute> { RamadanScreen(onBack) }
}

data class RamadanUiState(
    val today: TodayPrayers? = null,
    val location: UserLocation = UserLocation.Default,
    val hijriYear: Int = 0,
    val ramadanDay: Int = 0,
    val length: Int = 30,
    val start: LocalDate? = null,
    val days: List<DailyPrayerTimes> = emptyList(),
    val duas: List<Dua> = emptyList(),
)

@HiltViewModel
class RamadanViewModel @Inject constructor(
    observeToday: ObserveTodayPrayersUseCase,
    prayerTimes: PrayerTimesRepository,
    locationRepository: LocationRepository,
    content: ContentRepository,
) : ViewModel() {
    private val duas = content.duas().filter { it.category == DuaCategory.RAMADAN }

    /** Joriy yoki keyingi Ramazon: (hijriy yil, boshlanish sanasi, kunlar soni). */
    private val period: Triple<Int, LocalDate, Int>? = run {
        val h = HijriCalendar.toHijri(LocalDate.now())
        val year = if (h.month <= 9) h.year else h.year + 1
        val start = HijriCalendar.toGregorian(HijriDate(1, 9, year)) ?: return@run null
        Triple(year, start, HijriCalendar.monthLength(year, 9))
    }

    private val table = flow<List<DailyPrayerTimes>> {
        emit(emptyList())
        period?.let { (_, start, len) -> emit(prayerTimes.getRange(start, len)) }
    }

    val uiState: StateFlow<RamadanUiState> = combine(observeToday(), locationRepository.location, table) { today, loc, days ->
        val h = HijriCalendar.toHijri(today.today.date)
        RamadanUiState(
            today = today,
            location = loc,
            hijriYear = period?.first ?: h.year,
            ramadanDay = if (h.month == 9) h.day else 0,
            length = period?.third ?: 30,
            start = period?.second,
            days = days,
            duas = duas,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RamadanUiState(duas = duas))
}

/** Dizayn: Ramazon.dc.html */
@Composable
internal fun RamadanScreen(onBack: () -> Unit, viewModel: RamadanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    val todayDate = LocalDate.now()
    val city = state.location.city.ifBlank { state.location.displayName }
    val subtitle = if (state.ramadanDay > 0) stringResource(R.string.ramadan_sub_day, state.ramadanDay, city)
    else stringResource(R.string.ramadan_sub_wait, state.start?.let { ChronoUnit.DAYS.between(todayDate, it).toInt() } ?: 0, city)

    LazyColumn(
        Modifier.fillMaxSize().background(t.bg),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DHeader(stringResource(R.string.ramadan_year, state.hijriYear), onBack, subtitle) }
        state.today?.let { today ->
            item { Hero(today, state, Modifier.padding(horizontal = 20.dp)) }
        }
        state.duas.forEach { dua ->
            item(key = dua.id) {
                DCard(Modifier.padding(horizontal = 20.dp)) {
                    DOverline(dua.title.localized())
                    if (dua.id != "suhoor_niyat") {
                        Text(
                            dua.arabic,
                            modifier = Modifier.fillMaxWidth(),
                            fontFamily = AmiriFamily, fontSize = 19.sp, lineHeight = 30.sp, color = t.strong,
                            style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
                        )
                    }
                    DText(dua.transliteration, 13.sp, FontWeight.Normal, if (t.isDark) t.text else Color(0xFF45493F), lineHeight = 19.sp)
                    DText(dua.translation.localized(), 12.sp, FontWeight.Normal, t.muted, lineHeight = 17.sp)
                }
            }
        }
        if (state.days.isNotEmpty()) {
            item {
                DCard(Modifier.padding(horizontal = 20.dp), padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), spacing = 0.dp) {
                    Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                        DText(stringResource(R.string.calendar_day), 12.sp, FontWeight.Bold, t.muted, Modifier.weight(1f))
                        DText(stringResource(R.string.ramadan_suhoor), 12.sp, FontWeight.Bold, t.muted, Modifier.width(80.dp), align = TextAlign.End)
                        DText(stringResource(R.string.ramadan_iftar), 12.sp, FontWeight.Bold, t.muted, Modifier.width(80.dp), align = TextAlign.End)
                    }
                    val visible = state.days.withIndex().filter { !it.value.date.isBefore(todayDate) }
                    visible.forEach { (i, d) ->
                        val isToday = d.date == todayDate
                        HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                            val label = "${i + 1} · " + if (isToday) stringResource(R.string.ramadan_today_suffix)
                            else d.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }
                            val w = if (isToday) FontWeight.Bold else FontWeight.Medium
                            val c = if (isToday) t.strong else t.text
                            DText(label, 14.sp, w, c, Modifier.weight(1f), maxLines = 1)
                            DText(d[Prayer.FAJR].hhmm(), 14.sp, w, c, Modifier.width(80.dp), align = TextAlign.End)
                            DText(d[Prayer.MAGHRIB].hhmm(), 14.sp, w, c, Modifier.width(80.dp), align = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(today: TodayPrayers, state: RamadanUiState, modifier: Modifier) {
    val cream = Color(0xFFF6F1E6)
    val mint = Color(0xFFCFE3DA)
    val suhoorEnd = today.today.dateTime(Prayer.FAJR)
    val iftar = today.today.dateTime(Prayer.MAGHRIB)
    val (label, at) = when {
        today.now.isBefore(suhoorEnd) -> R.string.ramadan_suhoor_ends to suhoorEnd
        today.now.isBefore(iftar) -> R.string.ramadan_until_iftar to iftar
        else -> R.string.ramadan_suhoor_ends to today.tomorrow.dateTime(Prayer.FAJR)
    }
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0A3D33))) {
        // Burchakdagi hilol
        Canvas(Modifier.align(Alignment.TopEnd).offset(x = 20.dp, y = (-20).dp).size(120.dp)) {
            drawCircle(AccentGold, size.minDimension * 0.30f, Offset(size.width * 0.48f, size.height * 0.52f))
            drawCircle(Color(0xFF0A3D33), size.minDimension * 0.25f, Offset(size.width * 0.60f, size.height * 0.44f))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DText(stringResource(label), 13.sp, FontWeight.SemiBold, mint)
                DText(Duration.between(today.now, at).countdown(), 44.sp, FontWeight.ExtraBold, cream, spacing = (-0.02).em)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(cream.copy(alpha = 0.10f)).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    DText(stringResource(R.string.ramadan_suhoor_close), 12.sp, FontWeight.SemiBold, mint)
                    DText(today.today[Prayer.FAJR].hhmm(), 24.sp, FontWeight.ExtraBold, cream)
                }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(AccentGold).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    DText(stringResource(R.string.ramadan_iftar_open), 12.sp, FontWeight.Bold, Color(0xFF2B1F06))
                    DText(today.today[Prayer.MAGHRIB].hhmm(), 24.sp, FontWeight.ExtraBold, Color(0xFF2B1F06))
                }
            }
            if (state.ramadanDay > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        DText(stringResource(R.string.ramadan_progress, state.ramadanDay, state.length), 12.sp, FontWeight.SemiBold, mint, Modifier.weight(1f))
                        DText(stringResource(R.string.ramadan_days_left, (state.length - state.ramadanDay).coerceAtLeast(0)), 12.sp, FontWeight.SemiBold, mint)
                    }
                    DProgress(state.ramadanDay.toFloat() / state.length, cream.copy(alpha = 0.18f), AccentGold)
                }
            }
        }
    }
}
