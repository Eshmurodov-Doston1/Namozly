package uz.dostonbek.namozly.feature.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dostonbek.namozly.core.designsystem.component.MosqueImage
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyTokens
import uz.dostonbek.namozly.core.domain.usecase.TodayPrayers
import uz.dostonbek.namozly.core.model.CalculationMethod
import uz.dostonbek.namozly.core.model.DailyPrayerTimes
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.countdown
import uz.dostonbek.namozly.core.ui.hhmm
import uz.dostonbek.namozly.core.ui.labelRes
import uz.dostonbek.namozly.core.ui.localized
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

sealed interface HomeDestination {
    data object Quran : HomeDestination
    data class Surah(val number: Int, val ayah: Int? = null) : HomeDestination
    data object Qibla : HomeDestination
    data object Tasbih : HomeDestination
    data object Duas : HomeDestination
    data object Names : HomeDestination
    data object Qaza : HomeDestination
    data object Ramadan : HomeDestination
    data object Mosques : HomeDestination
    data object Live : HomeDestination
    data object Learn : HomeDestination
    data object Calendar : HomeDestination
    data object Notifications : HomeDestination
    data object Location : HomeDestination
    data object Profile : HomeDestination
    data object Books : HomeDestination
}

// Dizayndagi hero ichidagi doimiy ranglar
private val Cream = Color(0xFFF6F1E6)
private val Mint = Color(0xFFCFE3DA)
private val GoldDark = Color(0xFF2B1F06)

/** Plus Jakarta Sans matni, dizayndagi px qiymatlari bilan. */
@Composable
private fun T(
    text: String,
    size: TextUnit,
    weight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier,
    spacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) = Text(
    text, modifier, color = color, fontSize = size, fontWeight = weight, fontFamily = JakartaFamily,
    letterSpacing = spacing, lineHeight = lineHeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis, textAlign = align,
)

@Composable
internal fun HomeRoute(
    onNavigate: (HomeDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onLocationPermissionResult(result.values.any { it })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    var asked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (asked) return@LaunchedEffect
        asked = true
        locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    HomeScreen(state, playback, onNavigate, viewModel::toggleAyahAudio, viewModel::togglePrayed)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    playback: PlaybackState,
    onNavigate: (HomeDestination) -> Unit,
    onToggleAyah: () -> Unit,
    onTogglePrayed: (Prayer) -> Unit,
) {
    val t = NamozlyThemeTokens.t
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(t.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Header(state.unread, onNavigate) }
        state.today?.let { today ->
            item { Hero(state, today, onNavigate) }
        }
        item { LearnCard(state.guidesCount) { onNavigate(HomeDestination.Learn) } }
        item { LiveCard { onNavigate(HomeDestination.Live) } }
        item { BooksCard { onNavigate(HomeDestination.Books) } }
        state.today?.let { today -> item { TodayTimes(today, state.method) } }
        item { QuickGrid(onNavigate) }
        state.today?.let { item { MyPrayers(state.prayedToday, state.streak, onTogglePrayed) } }
        if (state.mosques.isNotEmpty()) {
            item { NearestMosques(state.mosques, onNavigate) }
        }
        state.ayah?.let { ayah ->
            item {
                val playing = playback.surah == ayah.ayah.surah && playback.ayah == ayah.ayah.number && playback.isPlaying
                AyahCard(
                    surahName = ayah.surah.name.localized(),
                    ayahNumber = ayah.ayah.number,
                    arabic = ayah.ayah.arabic,
                    translation = ayah.ayah.translation,
                    playing = playing,
                    onPlay = onToggleAyah,
                    onOpen = { onNavigate(HomeDestination.Surah(ayah.surah.number, ayah.ayah.number)) },
                )
            }
        }
        item { BottomTiles(state, onNavigate) }
    }
}

// ───────────────────────── Sarlavha ─────────────────────────

@Composable
private fun Header(unread: Int, onNavigate: (HomeDestination) -> Unit) {
    val t = NamozlyThemeTokens.t
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(top = 16.dp).height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NamozlyLogo(Modifier.size(40.dp))
        Column(Modifier.weight(1f)) {
            T("Namozly", 19.sp, FontWeight.ExtraBold, t.strong, spacing = (-0.02).em)
            T(stringResource(R.string.home_greeting), 13.sp, FontWeight.Medium, t.muted)
        }
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(t.surface).clickable { onNavigate(HomeDestination.Notifications) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Notifications, stringResource(R.string.notifications_title), tint = t.strong, modifier = Modifier.size(22.dp))
            if (unread > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).offset(x = (-12).dp, y = 11.dp).size(9.dp)
                        .clip(CircleShape).background(t.surface).padding(2.dp).clip(CircleShape).background(AccentGold),
                )
            }
        }
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(t.surface).clickable { onNavigate(HomeDestination.Profile) },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Person, stringResource(R.string.profile_title), tint = t.strong, modifier = Modifier.size(22.dp)) }
    }
}

// ───────────────────────── Hero ─────────────────────────

@Composable
private fun Hero(state: HomeUiState, today: TodayPrayers, onNavigate: (HomeDestination) -> Unit) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    val date = today.today.date
    val sunrise = today.today.dateTime(Prayer.SUNRISE)
    val maghrib = today.today.dateTime(Prayer.MAGHRIB)
    val isDay = today.now.isAfter(sunrise) && today.now.isBefore(maghrib)
    val compass = androidx.compose.ui.res.stringArrayResource(R.array.compass_points)
    val bearing = state.qiblaBearing
    val dir = compass[(((bearing + 22.5) % 360) / 45).toInt().coerceIn(0, 7)]

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(t.hero)
            .drawBehind { drawHeroPattern() }
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(Cream.copy(alpha = 0.14f))
                    .clickable { onNavigate(HomeDestination.Location) }.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = Cream, modifier = Modifier.size(16.dp))
                T(state.location.city.ifBlank { state.location.displayName }, 13.sp, FontWeight.Bold, Cream, maxLines = 1)
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Cream, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                T(
                    "${date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }}, " +
                        date.format(DateTimeFormatter.ofPattern("d-MMMM", locale)),
                    13.sp, FontWeight.Bold, Cream,
                )
                state.hijri?.let { T("${it.day} ${it.monthName(state.language)} ${it.year}", 12.sp, FontWeight.Medium, Mint) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                T(stringResource(R.string.home_next_prayer), 13.sp, FontWeight.SemiBold, Mint)
                T(stringResource(today.moment.next.labelRes()), 26.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                T(today.moment.nextAt.toLocalTime().hhmm(), 52.sp, FontWeight.ExtraBold, Cream, spacing = (-0.03).em, lineHeight = 55.sp)
                Box(
                    Modifier.padding(top = 6.dp).clip(RoundedCornerShape(12.dp)).background(AccentGold).padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    T(stringResource(R.string.home_time_left, Duration.between(today.now, today.moment.nextAt).countdown()), 13.sp, FontWeight.Bold, GoldDark)
                }
            }
            SunMoonRing(progress = today.progress, isDay = isDay, heroColor = t.hero, modifier = Modifier.size(132.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroTile(stringResource(R.string.home_sunrise), today.today[Prayer.SUNRISE].hhmm())
            HeroTile(stringResource(R.string.home_sunset), today.today[Prayer.MAGHRIB].hhmm())
            HeroTile(stringResource(R.string.qibla_title), "${bearing.toInt()}° $dir", onClick = { onNavigate(HomeDestination.Qibla) })
        }
    }
}

@Composable
private fun RowScope.HeroTile(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Cream.copy(alpha = 0.10f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        T(label, 11.sp, FontWeight.SemiBold, Mint, maxLines = 1)
        T(value, 15.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
    }
}

// ───────────────────────── Namoz o'rganish ─────────────────────────

@Composable
private fun LearnCard(guides: Int, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 172.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(t.goldSoft)
            .drawBehind {
                val c = t.goldText.copy(alpha = 0.12f)
                val k = 2.dp.toPx()
                val center = Offset(-40.dp.toPx() + 50 * k, size.height + 60.dp.toPx() - 50 * k)
                val half = 30 * k
                drawRect(c, center - Offset(half, half), androidx.compose.ui.geometry.Size(half * 2, half * 2), style = Stroke(1.2f * k))
                rotate(45f, center) {
                    drawRect(c, center - Offset(half, half), androidx.compose.ui.geometry.Size(half * 2, half * 2), style = Stroke(1.2f * k))
                }
                drawCircle(c, 18 * k, center, style = Stroke(1.2f * k))
            }
            .clickable(onClick = onClick)
            .padding(start = 18.dp, top = 18.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF0E5A4A)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                T(stringResource(R.string.home_learn_tag), 11.sp, FontWeight.ExtraBold, Color.White, spacing = 0.06.em)
            }
            T(stringResource(R.string.home_learn_title), 20.sp, FontWeight.ExtraBold, t.goldStrong, lineHeight = 24.sp, spacing = (-0.01).em)
            T(stringResource(R.string.home_learn_meta, guides), 13.sp, FontWeight.Normal, t.goldText, lineHeight = 17.sp)
            Row(
                Modifier.padding(top = 4.dp).height(40.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0E5A4A)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                T(stringResource(R.string.home_learn_cta), 13.sp, FontWeight.ExtraBold, Color.White)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        MihrabIllustration(Modifier.align(Alignment.Bottom).offset(y = 18.dp).size(width = 128.dp, height = 150.dp))
    }
}

// ───────────────────────── Jonli efir ─────────────────────────

@Composable
private fun LiveCard(onClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "live")
    val ring by pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Restart), label = "ring")
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0A1F1A)).clickable(onClick = onClick)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(width = 124.dp, height = 70.dp).clip(RoundedCornerShape(16.dp))) {
            KaabaThumb(Modifier.fillMaxSize())
            Row(
                Modifier.padding(6.dp).height(20.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE5484D)).padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Canvas(Modifier.size(6.dp)) {
                    drawCircle(Color.White.copy(alpha = 0.7f * (1 - ring)), radius = size.minDimension / 2 * (1 + ring * 1.2f))
                    drawCircle(Color.White)
                }
                T(stringResource(R.string.live_badge), 10.sp, FontWeight.ExtraBold, Color.White, spacing = 0.05.em)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            T(stringResource(R.string.home_live_title), 16.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
            T(stringResource(R.string.home_live_sub), 12.sp, FontWeight.Normal, Color(0xFFAFC2BA), lineHeight = 16.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = AccentGold, modifier = Modifier.size(20.dp))
    }
}

// ───────────────────────── Bugungi vaqtlar ─────────────────────────

private fun prayerIcon(p: Prayer): ImageVector = when (p) {
    Prayer.FAJR -> Icons.Outlined.WbTwilight
    Prayer.SUNRISE -> Icons.Outlined.LightMode
    Prayer.DHUHR -> Icons.Outlined.WbSunny
    Prayer.ASR -> Icons.Outlined.WbCloudy
    Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
    Prayer.ISHA -> Icons.Outlined.DarkMode
}

@Composable
private fun TodayTimes(today: TodayPrayers, method: CalculationMethod) {
    val t = NamozlyThemeTokens.t
    val source = if (today.today.source == DailyPrayerTimes.Source.API) stringResource(R.string.home_source_api, methodShort(method))
    else stringResource(R.string.home_source_offline)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.surface).padding(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            T(stringResource(R.string.home_today_times), 15.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            T(source, 12.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Prayer.entries.forEach { p ->
                val current = today.moment.current == p
                val passed = !current && !today.today.dateTime(p).isAfter(today.now) && today.moment.next != p
                val textColor = when { current -> t.chipText; passed -> t.muted; else -> t.text }
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if (current) t.chip else Color.Transparent).padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(prayerIcon(p), null, tint = if (passed) t.faint else t.icon, modifier = Modifier.size(22.dp))
                    T(stringResource(p.shortLabel()), 11.sp, if (current) FontWeight.ExtraBold else FontWeight.SemiBold, textColor, maxLines = 1)
                    T(today.today[p].hhmm(), 14.sp, if (current) FontWeight.ExtraBold else FontWeight.Bold, textColor, maxLines = 1)
                }
            }
        }
    }
}

private fun Prayer.shortLabel(): Int = if (this == Prayer.SUNRISE) R.string.prayer_sunrise_short else labelRes()

private fun methodShort(m: CalculationMethod) = when (m) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> "MWL"
    CalculationMethod.KARACHI -> "Karachi"
    CalculationMethod.ISNA -> "ISNA"
    CalculationMethod.UMM_AL_QURA -> "Umm al-Qura"
    CalculationMethod.EGYPTIAN -> "Egypt"
    CalculationMethod.DUBAI -> "Dubai"
    CalculationMethod.TURKEY -> "Diyanet"
    CalculationMethod.RUSSIA -> "DUM"
}

@Composable
private fun BooksCard(onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.surface).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(t.chip), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = t.icon, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            T(stringResource(R.string.home_books), 15.sp, FontWeight.ExtraBold, t.strong, maxLines = 1)
            T(stringResource(R.string.home_books_sub), 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = t.faint, modifier = Modifier.size(20.dp))
    }
}

// ───────────────────────── Tezkor bo'limlar ─────────────────────────

private class Q(val label: Int, val dest: HomeDestination, val gold: Boolean = false, val icon: @Composable (Color) -> Unit)

@Composable
private fun QuickGrid(onNavigate: (HomeDestination) -> Unit) {
    val t = NamozlyThemeTokens.t
    val items = listOf(
        Q(R.string.nav_quran, HomeDestination.Quran) { Icon(Icons.AutoMirrored.Outlined.MenuBook, null, tint = it, modifier = Modifier.size(24.dp)) },
        Q(R.string.qibla_title, HomeDestination.Qibla) { Icon(Icons.Outlined.Explore, null, tint = it, modifier = Modifier.size(24.dp)) },
        Q(R.string.nav_tasbih, HomeDestination.Tasbih) { TasbihIcon(it, Modifier.size(24.dp)) },
        Q(R.string.duas_title, HomeDestination.Duas) { Icon(Icons.Outlined.VolunteerActivism, null, tint = it, modifier = Modifier.size(24.dp)) },
        Q(R.string.names_short, HomeDestination.Names) {
            Text("٩٩", color = it, fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        Q(R.string.learn_title, HomeDestination.Learn, gold = true) { Icon(Icons.Rounded.SelfImprovement, null, tint = GoldDark, modifier = Modifier.size(24.dp)) },
        Q(R.string.mosques_title, HomeDestination.Mosques) { Icon(Icons.Outlined.Mosque, null, tint = it, modifier = Modifier.size(24.dp)) },
        Q(R.string.qaza_short, HomeDestination.Qaza) { Icon(Icons.Outlined.EventAvailable, null, tint = it, modifier = Modifier.size(24.dp)) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { q ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(t.surface).clickable { onNavigate(q.dest) }
                            .padding(start = 2.dp, end = 2.dp, top = 12.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(if (q.gold) AccentGold else t.chip),
                            contentAlignment = Alignment.Center,
                        ) { q.icon(t.icon) }
                        T(stringResource(q.label), 12.sp, FontWeight.Bold, t.text, maxLines = 2, align = TextAlign.Center, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

// ───────────────────────── Bugungi namozlarim ─────────────────────────

@Composable
private fun MyPrayers(prayed: Set<Prayer>, streak: Int, onToggle: (Prayer) -> Unit) {
    val t = NamozlyThemeTokens.t
    val fard = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(stringResource(R.string.home_my_prayers), 15.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            if (streak > 0) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = t.accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                T(stringResource(R.string.home_streak, streak), 12.sp, FontWeight.ExtraBold, t.accent)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            fard.forEach { p ->
                val done = p in prayed
                Column(
                    Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .then(if (done) Modifier.background(Color(0xFF0E5A4A)) else Modifier.dashedBorder(t.dash))
                        .clickable { onToggle(p) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    if (done) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Box(Modifier.size(20.dp).drawBehind { drawCircle(t.dash, radius = size.minDimension / 2 - 1.dp.toPx(), style = Stroke(2.dp.toPx())) })
                    }
                    T(stringResource(p.labelRes()), 11.sp, FontWeight.Bold, if (done) Color.White else t.text, maxLines = 1)
                }
            }
        }
    }
}

// ───────────────────────── Eng yaqin masjidlar ─────────────────────────

@Composable
private fun NearestMosques(mosques: List<Mosque>, onNavigate: (HomeDestination) -> Unit) {
    val t = NamozlyThemeTokens.t
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            T(stringResource(R.string.home_nearest_mosques), 17.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            T(
                stringResource(R.string.home_view_map), 13.sp, FontWeight.Bold, t.icon,
                Modifier.clip(RoundedCornerShape(8.dp)).clickable { onNavigate(HomeDestination.Mosques) }.padding(vertical = 8.dp),
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 16.dp)) {
            items(mosques, key = { it.id }) { m ->
                Column(Modifier.width(252.dp).clip(RoundedCornerShape(24.dp)).background(t.surface)) {
                    Box(Modifier.fillMaxWidth().height(124.dp)) {
                        MosqueImage(photoUrl = m.photoUrl, seed = m.id, contentDescription = m.name, modifier = Modifier.fillMaxSize())
                        m.distanceMeters?.let { d ->
                            Box(Modifier.padding(12.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 5.dp)) {
                                T(stringResource(R.string.home_mosque_eta, formatDistance(d), (d / 80).toInt().coerceAtLeast(1)), 12.sp, FontWeight.ExtraBold, Color(0xFF0A3D33))
                            }
                        }
                    }
                    Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            T(m.name, 15.sp, FontWeight.ExtraBold, t.text, maxLines = 1)
                            T(m.address ?: m.city ?: "OpenStreetMap", 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(20.dp)).background(t.primary).clickable {
                                    val uri = Uri.parse("geo:${m.point.latitude},${m.point.longitude}?q=${m.point.latitude},${m.point.longitude}(${Uri.encode(m.name)})")
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                },
                                contentAlignment = Alignment.Center,
                            ) { T(stringResource(R.string.home_directions), 13.sp, FontWeight.Bold, Color.White) }
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(t.chip).clickable { onNavigate(HomeDestination.Mosques) },
                                contentAlignment = Alignment.Center,
                            ) { Icon(Icons.Rounded.ChevronRight, null, tint = t.icon, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── Kun oyati ─────────────────────────

@Composable
private fun AyahCard(
    surahName: String,
    ayahNumber: Int,
    arabic: String,
    translation: String,
    playing: Boolean,
    onPlay: () -> Unit,
    onOpen: () -> Unit,
) {
    val t = NamozlyThemeTokens.t
    val context = LocalContext.current
    val ref = stringResource(R.string.home_ayah_ref, surahName, ayahNumber)
    val shareTitle = stringResource(R.string.home_ayah_of_day)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.surface).clickable(onClick = onOpen).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            T(stringResource(R.string.home_ayah_label), 12.sp, FontWeight.ExtraBold, t.accent, Modifier.weight(1f), spacing = 0.06.em)
            T(ref, 12.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
        }
        Text(
            arabic,
            modifier = Modifier.fillMaxWidth(),
            fontFamily = AmiriFamily,
            fontSize = 24.sp,
            lineHeight = 38.sp,
            color = t.strong,
            style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Rtl),
        )
        T(translation, 14.sp, FontWeight.Normal, t.text, lineHeight = 21.sp)
        Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipButton(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, stringResource(if (playing) R.string.common_pause else R.string.common_listen), t, onPlay)
            ChipButton(Icons.Rounded.Share, stringResource(R.string.common_share), t) {
                val text = "$arabic\n\n$translation\n\n— $ref\n#Namozly"
                val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
                context.startActivity(Intent.createChooser(send, shareTitle))
            }
        }
    }
}

@Composable
private fun ChipButton(icon: ImageVector, label: String, t: NamozlyTokens, onClick: () -> Unit) {
    Row(
        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(t.chip).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = t.chipText, modifier = Modifier.size(14.dp))
        T(label, 12.sp, FontWeight.Bold, t.chipText)
    }
}

// ───────────────────────── Ramazon / Juma ─────────────────────────

@Composable
private fun BottomTiles(state: HomeUiState, onNavigate: (HomeDestination) -> Unit) {
    val t = NamozlyThemeTokens.t
    val locale = Locale.getDefault()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            Modifier.weight(1f).heightIn(min = 104.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF0A3D33))
                .drawBehind {
                    val k = 0.7.dp.toPx()
                    val origin = Offset(size.width + 14.dp.toPx() - 70.dp.toPx(), -14.dp.toPx())
                    drawCircle(AccentGold, 30 * k, origin + Offset(48 * k, 52 * k))
                    drawCircle(Color(0xFF0A3D33), 24 * k, origin + Offset(60 * k, 44 * k))
                }
                .clickable { onNavigate(HomeDestination.Ramadan) }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.ramadanDay > 0) {
                T(stringResource(R.string.ramadan_title), 12.sp, FontWeight.SemiBold, Mint)
                T(stringResource(R.string.ramadan_day_n, state.ramadanDay), 22.sp, FontWeight.ExtraBold, Cream)
            } else {
                T(stringResource(R.string.home_until_ramadan), 12.sp, FontWeight.SemiBold, Mint)
                state.ramadanStart?.let { start ->
                    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), start).toInt()
                    T(stringResource(R.string.home_days_approx, days), 30.sp, FontWeight.ExtraBold, Cream)
                    T(start.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) }, 12.sp, FontWeight.Normal, Mint)
                }
            }
        }
        Column(
            Modifier.weight(1f).heightIn(min = 104.dp).clip(RoundedCornerShape(24.dp)).background(t.goldSoft).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            T(stringResource(R.string.home_friday), 12.sp, FontWeight.Bold, t.goldText)
            T(
                if (state.daysToFriday == 0) stringResource(R.string.period_today) else stringResource(R.string.home_days, state.daysToFriday),
                30.sp, FontWeight.ExtraBold, t.goldStrong,
            )
            T(stringResource(R.string.home_friday_hint), 12.sp, FontWeight.Normal, t.goldText)
        }
    }
}

internal fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m" else String.format(Locale.getDefault(), "%.1f km", meters / 1000)

