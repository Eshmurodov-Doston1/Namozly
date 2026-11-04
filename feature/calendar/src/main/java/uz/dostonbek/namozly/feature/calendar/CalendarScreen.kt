package uz.dostonbek.namozly.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.domain.usecase.ObserveMonthPrayersUseCase
import uz.dostonbek.namozly.core.domain.util.HijriCalendar
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.HijriDate
import uz.dostonbek.namozly.core.model.IslamicEvent
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.hhmm
import uz.dostonbek.namozly.core.ui.labelRes
import uz.dostonbek.namozly.core.ui.localized
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val days: List<DailyPrayerTimes> = emptyList(),
    val events: List<Pair<IslamicEvent, LocalDate>> = emptyList(),
    val language: AppLanguage = AppLanguage.UZ_LATN,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    observeMonth: ObserveMonthPrayersUseCase,
    content: ContentRepository,
    settings: SettingsRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())

    /** Joriy hijriy yildagi (va keyingi) bayramlarning milodiy sanalari. */
    private val events: List<Pair<IslamicEvent, LocalDate>> = run {
        val today = LocalDate.now()
        val hijriYear = HijriCalendar.toHijri(today).year
        listOf(hijriYear, hijriYear + 1).flatMap { y ->
            content.islamicEvents().mapNotNull { e ->
                HijriCalendar.toGregorian(HijriDate(e.hijriDay, e.hijriMonth, y))?.let { e to it }
            }
        }.filter { !it.second.isBefore(today) }.sortedBy { it.second }.take(10)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = month.flatMapLatest { ym ->
        combine(observeMonth(ym.year, ym.monthValue), settings.settings.map { it.language }) { days, lang ->
            CalendarUiState(ym, days, events, lang)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(events = events))

    fun shiftMonth(delta: Long) { month.value = month.value.plusMonths(delta) }
}

private val Gold = Color(0xFF8A6420)

/** Dizayn: Taqvim.dc.html */
@Composable
internal fun CalendarScreenRoute(viewModel: CalendarViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(0) }
    val today = LocalDate.now()
    var selected by rememberSaveable { mutableStateOf(today.toString()) }
    val selectedDate = LocalDate.parse(selected)

    LazyColumn(
        Modifier.fillMaxSize().background(t.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Box(Modifier.statusBarsPadding().padding(top = 20.dp).height(44.dp), contentAlignment = Alignment.CenterStart) {
                DText(stringResource(R.string.nav_calendar), 24.sp, FontWeight.ExtraBold, t.strong)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (t.isDark) t.chip else Color(0xFFEAE3D3)).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(R.string.calendar_monthly_times, R.string.calendar_events, R.string.calendar_converter).forEachIndexed { i, res ->
                    val on = tab == i
                    Box(
                        Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(11.dp)).background(if (on) t.surface else Color.Transparent).clickable { tab = i },
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(res), 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) t.strong else t.muted, maxLines = 1) }
                }
            }
        }
        when (tab) {
            0 -> {
                item { MonthGrid(state, today, selectedDate, viewModel::shiftMonth) { selected = it.toString() } }
                item { DayTimes(state.days.firstOrNull { it.date == selectedDate }, selectedDate, state.language) }
                item { Upcoming(state.events.take(3), today, state.language) }
            }
            1 -> item { Upcoming(state.events, today, state.language, showHijri = true) }
            else -> item { Converter(state.language) }
        }
        item { DText(stringResource(R.string.calendar_hijri_note), 12.sp, FontWeight.Normal, t.muted, lineHeight = 17.sp) }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, today: LocalDate, selected: LocalDate, onShift: (Long) -> Unit, onSelect: (LocalDate) -> Unit) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    val ym = state.month
    val first = ym.atDay(1)
    val hFirst = runCatching { HijriCalendar.toHijri(first) }.getOrNull()
    val hLast = runCatching { HijriCalendar.toHijri(ym.atEndOfMonth()) }.getOrNull()
    val hijriTitle = when {
        hFirst == null || hLast == null -> ""
        hFirst.month == hLast.month -> "${hFirst.monthName(state.language)} ${hLast.year}"
        else -> "${hFirst.monthName(state.language)} – ${hLast.monthName(state.language)} ${hLast.year}"
    }
    DCard(radius = 20.dp, padding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundNav(Icons.AutoMirrored.Rounded.KeyboardArrowLeft) { onShift(-1) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                DText("${ym.month.getDisplayName(JTextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${ym.year}", 16.sp, FontWeight.ExtraBold, t.strong)
                if (hijriTitle.isNotBlank()) DText(hijriTitle, 12.sp, FontWeight.SemiBold, t.muted)
            }
            RoundNav(Icons.AutoMirrored.Rounded.KeyboardArrowRight) { onShift(1) }
        }
        // Hafta kunlari — dushanbadan
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { d ->
                DText(
                    d.getDisplayName(JTextStyle.SHORT_STANDALONE, locale).take(2).replaceFirstChar { it.titlecase(locale) },
                    11.sp, FontWeight.Bold, if (d == DayOfWeek.FRIDAY) Gold else t.muted, Modifier.weight(1f), align = TextAlign.Center,
                )
            }
        }
        val offset = first.dayOfWeek.value - 1
        val cells = offset + ym.lengthOfMonth()
        val rows = (cells + 6) / 7
        val byDate = state.days.associateBy { it.date }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { r ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { c ->
                        val dayNum = r * 7 + c - offset + 1
                        Box(Modifier.weight(1f).height(40.dp)) {
                            if (dayNum in 1..ym.lengthOfMonth()) {
                                val date = ym.atDay(dayNum)
                                val hijri = byDate[date]?.hijri ?: runCatching { HijriCalendar.toHijri(date) }.getOrNull()
                                val isToday = date == today
                                val isSel = date == selected && !isToday
                                val friday = date.dayOfWeek == DayOfWeek.FRIDAY
                                Column(
                                    Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        .background(if (isToday) Color(0xFF0E5A4A) else Color.Transparent)
                                        .then(if (isSel) Modifier.border(1.5.dp, t.primary, RoundedCornerShape(12.dp)) else Modifier)
                                        .clickable { onSelect(date) },
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                                ) {
                                    DText(dayNum.toString(), 14.sp, if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        when { isToday -> Color.White; friday -> Gold; else -> t.text })
                                    if (hijri != null) DText(hijri.day.toString(), 9.sp, FontWeight.SemiBold, if (isToday) Color(0xFFCFE3DA) else t.faint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundNav(icon: ImageVector, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Box(Modifier.size(36.dp).clip(CircleShape).background(t.bg).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = t.strong, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DayTimes(day: DailyPrayerTimes?, date: LocalDate, language: AppLanguage) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    DCard(radius = 20.dp, padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), spacing = 0.dp) {
        val hijri = day?.hijri ?: runCatching { HijriCalendar.toHijri(date) }.getOrNull()
        Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            DText(date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", locale)), 15.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            if (hijri != null) DText("${hijri.day} ${hijri.monthName(language)}", 12.sp, FontWeight.SemiBold, Gold)
        }
        if (day == null) {
            DText("—", 14.sp, FontWeight.SemiBold, t.muted, Modifier.padding(vertical = 8.dp))
        } else {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Prayer.entries.forEach { p ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        DText(stringResource(p.labelRes()), 11.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
                        DText(day[p].hhmm(), 14.sp, FontWeight.ExtraBold, t.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun Upcoming(events: List<Pair<IslamicEvent, LocalDate>>, today: LocalDate, language: AppLanguage, showHijri: Boolean = false) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    DCard(radius = 20.dp, padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), spacing = 0.dp) {
        DText(stringResource(R.string.calendar_upcoming).uppercase(), 12.sp, FontWeight.Bold, Gold, Modifier.padding(vertical = 8.dp, horizontal = 4.dp), spacing = 0.04.em)
        events.forEachIndexed { i, (event, date) ->
            if (i > 0) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
            val days = ChronoUnit.DAYS.between(today, date).toInt()
            val soon = days <= 30
            Row(Modifier.fillMaxWidth().height(if (showHijri) 58.dp else 50.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.width(44.dp).height(36.dp).clip(RoundedCornerShape(10.dp)).background(if (soon) Color(0xFF0E5A4A) else t.bg), contentAlignment = Alignment.Center) {
                    DText("≈${date.dayOfMonth}", 13.sp, FontWeight.ExtraBold, if (soon) Color.White else t.strong)
                }
                Column(Modifier.weight(1f)) {
                    DText(event.name.localized(), 14.sp, FontWeight.Bold, t.text, maxLines = 1)
                    val sub = date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) } +
                        if (showHijri) " · ${event.hijriDay} ${HijriDate(event.hijriDay, event.hijriMonth, 0).monthName(language)}" else ""
                    DText(sub, 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                }
                DText(stringResource(R.string.calendar_days_left, days), 12.sp, FontWeight.SemiBold, t.faint)
            }
        }
    }
}

@Composable
private fun Converter(language: AppLanguage) {
    val t = NamozlyThemeTokens.t
    var toHijri by rememberSaveable { mutableStateOf(true) }
    val now = LocalDate.now()
    var d by rememberSaveable { mutableStateOf(now.dayOfMonth.toString()) }
    var m by rememberSaveable { mutableStateOf(now.monthValue.toString()) }
    var y by rememberSaveable { mutableStateOf(now.year.toString()) }
    val result = remember(toHijri, d, m, y, language) {
        val day = d.toIntOrNull(); val month = m.toIntOrNull(); val year = y.toIntOrNull()
        if (day == null || month == null || year == null) null
        else if (toHijri) runCatching { LocalDate.of(year, month, day) }.getOrNull()?.let { runCatching { HijriCalendar.toHijri(it) }.getOrNull() }
            ?.let { "${it.day} ${it.monthName(language)} ${it.year}" }
        else HijriCalendar.toGregorian(HijriDate(day, month, year))?.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
    }
    DCard(radius = 20.dp, spacing = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DText(stringResource(if (toHijri) R.string.calendar_greg_to_hijri else R.string.calendar_hijri_to_greg), 16.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            RoundNav(Icons.Rounded.SwapVert) { toHijri = !toHijri }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumField(stringResource(R.string.calendar_day), d, { d = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
            NumField(stringResource(R.string.calendar_month), m, { m = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
            NumField(stringResource(R.string.calendar_year), y, { y = it.filter(Char::isDigit).take(4) }, Modifier.weight(1.4f))
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF0E5A4A)).padding(16.dp), contentAlignment = Alignment.Center) {
            DText(result ?: "—", 20.sp, FontWeight.ExtraBold, Color.White, align = TextAlign.Center)
        }
    }
}

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val t = NamozlyThemeTokens.t
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DText(label, 12.sp, FontWeight.Bold, t.muted)
        Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(t.bg).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(value, onChange, singleLine = true,
                textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), cursorBrush = SolidColor(t.primary))
        }
    }
}
