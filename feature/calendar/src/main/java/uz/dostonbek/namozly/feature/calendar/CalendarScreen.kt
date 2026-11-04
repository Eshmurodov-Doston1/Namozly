package uz.dostonbek.namozly.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
        }.filter { !it.second.isBefore(today) }.sortedBy { it.second }.take(12)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = month.flatMapLatest { ym ->
        combine(observeMonth(ym.year, ym.monthValue), settings.settings.map { it.language }) { days, lang ->
            CalendarUiState(ym, days, events, lang)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState(events = events))

    fun shiftMonth(delta: Long) { month.value = month.value.plusMonths(delta) }
}

private val CreamBg = Color(0xFFF7F4EE)
private val DarkEmerald = Color(0xFF0E4D40)
private val SelectedDayBg = Color(0xFF0E5A4A)
private val TabBarBg = Color(0xFFEBE5D8)
private val BronzeTitle = Color(0xFF9E782F)
private val FridayGold = Color(0xFFB4782A)
private val CardBorder = Color(0xFFE8E2D8)
private val BadgeCreamBg = Color(0xFFF5EFE4)
private val DividerColor = Color(0xFFF0EAE1)

/**
 * Taqvim ekrani — foydalanuvchi yuborgan dizaynga 100% mos:
 * - Titul: "Taqvim"
 * - Segmented tablar: Oylik vaqtlar, Muhim sanalar, Konvertor
 * - Oylik taqvim kartasi: Milodiy kun tepada, Hijriy kun pastda, Juma oltin rangda, tanlangan kun zumrad rangda
 * - "YAQINLASHAYOTGAN SANALAR" bloki (≈10, ≈5, ≈8)
 * - Kun tanlanganda namoz vaqtlari qulay ModalBottomSheet da ochiladi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarScreenRoute(viewModel: CalendarViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val today = LocalDate.now()
    var selectedDate by rememberSaveable { mutableStateOf(today) }
    var showDayDetailSheet by remember { mutableStateOf(false) }

    val screenBg = if (t.isDark) t.bg else CreamBg

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(screenBg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Sarlavha: Taqvim
        item {
            Box(
                Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .height(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                DText(
                    text = stringResource(R.string.nav_calendar),
                    size = 28.sp,
                    weight = FontWeight.ExtraBold,
                    color = if (t.isDark) t.strong else DarkEmerald
                )
            }
        }

        // 2. Segmented Pill Tab Bar (Oylik vaqtlar | Muhim sanalar | Konvertor)
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (t.isDark) t.chip else TabBarBg)
                    .padding(4.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        R.string.calendar_monthly_times,
                        R.string.calendar_events,
                        R.string.calendar_converter
                    ).forEachIndexed { i, res ->
                        val on = tab == i
                        Box(
                            Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .then(
                                    if (on) {
                                        Modifier
                                            .shadow(2.dp, RoundedCornerShape(22.dp))
                                            .background(if (t.isDark) t.surface else Color.White)
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                )
                                .clickable { tab = i },
                            contentAlignment = Alignment.Center,
                        ) {
                            DText(
                                stringResource(res),
                                13.sp,
                                if (on) FontWeight.Bold else FontWeight.Medium,
                                if (on) (if (t.isDark) t.primary else DarkEmerald) else Color(0xFF6B665C),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Tab tarkibi
        when (tab) {
            0 -> {
                // Asosiy oy jadvali
                item {
                    MonthGridCard(
                        state = state,
                        today = today,
                        selected = selectedDate,
                        onShift = viewModel::shiftMonth,
                        onSelect = {
                            selectedDate = it
                            showDayDetailSheet = true
                        }
                    )
                }

                // Yaqinlashayotgan sanalar bloki (Rasm bilan 100% bir xil)
                item {
                    UpcomingSectionCard(
                        events = state.events.take(3),
                        today = today,
                        language = state.language
                    )
                }
            }
            1 -> {
                item {
                    AllEventsCard(
                        events = state.events,
                        today = today,
                        language = state.language
                    )
                }
            }
            else -> {
                item {
                    ConverterCard(state.language)
                }
            }
        }

        item {
            DText(
                stringResource(R.string.calendar_hijri_note),
                12.sp,
                FontWeight.Normal,
                Color(0xFF8C867A),
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 4.dp).padding(top = 4.dp)
            )
        }
    }

    // Kun ustiga bosilganda namoz vaqtlari varaqchasi
    if (showDayDetailSheet) {
        val dayTimes = state.days.firstOrNull { it.date == selectedDate }
        DayPrayerTimesBottomSheet(
            day = dayTimes,
            date = selectedDate,
            language = state.language,
            onDismiss = { showDayDetailSheet = false }
        )
    }
}

/**
 * 1-Karta: Oy ko'rinishi va ikki qavatli kunlar jadvali (Noyabr 2026...)
 */
@Composable
private fun MonthGridCard(
    state: CalendarUiState,
    today: LocalDate,
    selected: LocalDate,
    onShift: (Long) -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (t.isDark) t.surface else Color.White)
            .border(0.6.dp, if (t.isDark) t.line else CardBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Oy navigatsiya qatori (< Noyabr 2026 >)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (t.isDark) t.chip else Color(0xFFF3ECE1))
                    .clickable { onShift(-1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Oldingi oy",
                    tint = if (t.isDark) t.strong else DarkEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DText(
                    text = "${ym.month.getDisplayName(JTextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }} ${ym.year}",
                    size = 17.sp,
                    weight = FontWeight.ExtraBold,
                    color = if (t.isDark) t.strong else DarkEmerald
                )
                if (hijriTitle.isNotBlank()) {
                    DText(
                        text = hijriTitle,
                        size = 12.sp,
                        weight = FontWeight.Normal,
                        color = Color(0xFF756F64),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (t.isDark) t.chip else Color(0xFFF3ECE1))
                    .clickable { onShift(1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Keyingi oy",
                    tint = if (t.isDark) t.strong else DarkEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Hafta kunlari sarlavhasi (Du, Se, Ch, Pa, Ju, Sh, Ya — Juma oltin rangda)
        val daysOfWeek = listOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { d ->
                val isFriday = d == "Ju"
                DText(
                    text = d,
                    size = 13.sp,
                    weight = FontWeight.Bold,
                    color = if (isFriday) FridayGold else (if (t.isDark) t.muted else Color(0xFF38352F)),
                    modifier = Modifier.weight(1f),
                    align = TextAlign.Center
                )
            }
        }

        // Kunlar panjarasi
        val offset = first.dayOfWeek.value - 1 // Dushanba = 0 ... Yakshanba = 6
        val cells = offset + ym.lengthOfMonth()
        val rows = (cells + 6) / 7
        val byDate = state.days.associateBy { it.date }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(rows) { r ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    repeat(7) { c ->
                        val dayNum = r * 7 + c - offset + 1
                        if (dayNum in 1..ym.lengthOfMonth()) {
                            val date = ym.atDay(dayNum)
                            val hijri = byDate[date]?.hijri ?: runCatching { HijriCalendar.toHijri(date) }.getOrNull()
                            val isSelected = date == selected
                            val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(if (isSelected) SelectedDayBg else Color.Transparent)
                                    .clickable { onSelect(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    DText(
                                        text = dayNum.toString(),
                                        size = 15.sp,
                                        weight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = when {
                                            isSelected -> Color.White
                                            isFriday -> FridayGold
                                            else -> (if (t.isDark) t.text else Color(0xFF1E1E1E))
                                        }
                                    )
                                    if (hijri != null) {
                                        DText(
                                            text = hijri.day.toString(),
                                            size = 10.5.sp,
                                            weight = FontWeight.SemiBold,
                                            color = when {
                                                isSelected -> Color(0xFFD2E5DC)
                                                isFriday -> FridayGold
                                                else -> (if (t.isDark) t.faint else Color(0xFF756F64))
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2-Karta: YAQINLASHAYOTGAN SANALAR bloki (media_1793616690042_d394b5be.png ga 100% mos)
 */
@Composable
private fun UpcomingSectionCard(
    events: List<Pair<IslamicEvent, LocalDate>>,
    today: LocalDate,
    language: AppLanguage,
) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (t.isDark) t.surface else Color.White)
            .border(0.6.dp, if (t.isDark) t.line else CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        DText(
            text = "YAQINLASHAYOTGAN SANALAR",
            size = 12.5.sp,
            weight = FontWeight.Bold,
            color = BronzeTitle,
            spacing = 0.06.em,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        events.forEachIndexed { i, (event, date) ->
            if (i > 0) {
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = if (t.isDark) t.line else DividerColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            // Ramazon yoki ulug' oy bo'lsa to'q zumrad fonli nishon
            val isSpecial = event.name.uz.contains("Ramazon", ignoreCase = true) ||
                event.name.uz.contains("Qadr", ignoreCase = true) ||
                event.name.uz.contains("Hayit", ignoreCase = true)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Chap tomondagi kun nishoni (≈10, ≈5, ≈8)
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSpecial) SelectedDayBg else (if (t.isDark) t.chip else BadgeCreamBg)),
                    contentAlignment = Alignment.Center
                ) {
                    DText(
                        text = "≈${date.dayOfMonth}",
                        size = 14.sp,
                        weight = FontWeight.ExtraBold,
                        color = if (isSpecial) Color.White else (if (t.isDark) t.primary else DarkEmerald)
                    )
                }

                // Sarlavha va oy/yil
                Column(Modifier.weight(1f)) {
                    DText(
                        text = event.name.localized(),
                        size = 15.sp,
                        weight = FontWeight.Bold,
                        color = if (t.isDark) t.text else Color(0xFF1E201E)
                    )
                    val monthYear = date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                        .replaceFirstChar { it.titlecase(locale) }
                    DText(
                        text = monthYear,
                        size = 12.5.sp,
                        weight = FontWeight.Normal,
                        color = if (t.isDark) t.muted else Color(0xFF707572)
                    )
                }
            }
        }
    }
}

/**
 * Muhim sanalar to'liq ro'yxati (2-tab)
 */
@Composable
private fun AllEventsCard(
    events: List<Pair<IslamicEvent, LocalDate>>,
    today: LocalDate,
    language: AppLanguage,
) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (t.isDark) t.surface else Color.White)
            .border(0.6.dp, if (t.isDark) t.line else CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        DText(
            text = "BARCHA MUHIM SANALAR",
            size = 12.5.sp,
            weight = FontWeight.Bold,
            color = BronzeTitle,
            spacing = 0.06.em,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        events.forEachIndexed { i, (event, date) ->
            if (i > 0) {
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = if (t.isDark) t.line else DividerColor,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
            val days = ChronoUnit.DAYS.between(today, date).toInt()
            val isSpecial = event.name.uz.contains("Ramazon", ignoreCase = true) ||
                event.name.uz.contains("Qadr", ignoreCase = true) ||
                event.name.uz.contains("Hayit", ignoreCase = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isSpecial) SelectedDayBg else (if (t.isDark) t.chip else BadgeCreamBg)),
                    contentAlignment = Alignment.Center
                ) {
                    DText(
                        text = "≈${date.dayOfMonth}",
                        size = 14.sp,
                        weight = FontWeight.ExtraBold,
                        color = if (isSpecial) Color.White else (if (t.isDark) t.primary else DarkEmerald)
                    )
                }

                Column(Modifier.weight(1f)) {
                    DText(
                        text = event.name.localized(),
                        size = 15.sp,
                        weight = FontWeight.Bold,
                        color = if (t.isDark) t.text else Color(0xFF1E201E)
                    )
                    val hijriText = "${event.hijriDay} ${HijriDate(event.hijriDay, event.hijriMonth, 0).monthName(language)}"
                    val monthYear = date.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) }
                    DText(
                        text = "$monthYear · $hijriText",
                        size = 12.sp,
                        weight = FontWeight.Normal,
                        color = if (t.isDark) t.muted else Color(0xFF707572)
                    )
                }

                DText(
                    text = stringResource(R.string.calendar_days_left, days),
                    size = 12.sp,
                    weight = FontWeight.SemiBold,
                    color = if (t.isDark) t.faint else Color(0xFF8C867A)
                )
            }
        }
    }
}

/**
 * 3-Tab: Hijriy va Milodiy sanalarni o'girish konvertori
 */
@Composable
private fun ConverterCard(language: AppLanguage) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (t.isDark) t.surface else Color.White)
            .border(0.6.dp, if (t.isDark) t.line else CardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DText(
                stringResource(if (toHijri) R.string.calendar_greg_to_hijri else R.string.calendar_hijri_to_greg),
                16.sp,
                FontWeight.ExtraBold,
                if (t.isDark) t.strong else DarkEmerald,
                Modifier.weight(1f)
            )
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (t.isDark) t.chip else Color(0xFFF3ECE1))
                    .clickable { toHijri = !toHijri },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SwapVert, null, tint = if (t.isDark) t.strong else DarkEmerald, modifier = Modifier.size(20.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumField(stringResource(R.string.calendar_day), d, { d = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
            NumField(stringResource(R.string.calendar_month), m, { m = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f))
            NumField(stringResource(R.string.calendar_year), y, { y = it.filter(Char::isDigit).take(4) }, Modifier.weight(1.4f))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SelectedDayBg)
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            DText(
                result ?: "—",
                18.sp,
                FontWeight.ExtraBold,
                Color.White,
                align = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val t = NamozlyThemeTokens.t
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DText(label, 12.sp, FontWeight.Bold, if (t.isDark) t.muted else Color(0xFF756F64))
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (t.isDark) t.chip else Color(0xFFF3ECE1))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value,
                onChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JakartaFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (t.isDark) t.text else Color(0xFF1E1E1E)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(if (t.isDark) t.primary else DarkEmerald)
            )
        }
    }
}

/**
 * Tanlangan kun uchun namoz vaqtlari pastki varag'i (ModalBottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPrayerTimesBottomSheet(
    day: DailyPrayerTimes?,
    date: LocalDate,
    language: AppLanguage,
    onDismiss: () -> Unit,
) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    val hijri = day?.hijri ?: runCatching { HijriCalendar.toHijri(date) }.getOrNull()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (t.isDark) t.surface else Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sarlavha
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    DText(
                        text = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", locale)),
                        size = 18.sp,
                        weight = FontWeight.ExtraBold,
                        color = if (t.isDark) t.strong else DarkEmerald
                    )
                    if (hijri != null) {
                        DText(
                            text = "${hijri.day} ${hijri.monthName(language)} ${hijri.year}",
                            size = 13.sp,
                            weight = FontWeight.SemiBold,
                            color = FridayGold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (t.isDark) t.chip else Color(0xFFF3ECE1))
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Yopish",
                        tint = if (t.isDark) t.strong else DarkEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = if (t.isDark) t.line else DividerColor)

            if (day == null) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    DText("Bu kun uchun ma'lumot yuklanmadi", 14.sp, FontWeight.Medium, t.muted)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val prayers = listOf(
                        Prayer.FAJR to R.string.prayer_fajr,
                        Prayer.SUNRISE to R.string.prayer_sunrise,
                        Prayer.DHUHR to R.string.prayer_dhuhr,
                        Prayer.ASR to R.string.prayer_asr,
                        Prayer.MAGHRIB to R.string.prayer_maghrib,
                        Prayer.ISHA to R.string.prayer_isha,
                    )
                    prayers.forEach { (prayer, nameRes) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (t.isDark) t.chip else Color(0xFFF9F6F0))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DText(
                                text = stringResource(nameRes),
                                size = 15.sp,
                                weight = FontWeight.Bold,
                                color = if (t.isDark) t.text else Color(0xFF1E201E)
                            )
                            DText(
                                text = day[prayer].hhmm(),
                                size = 16.sp,
                                weight = FontWeight.ExtraBold,
                                color = if (t.isDark) t.primary else DarkEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}

