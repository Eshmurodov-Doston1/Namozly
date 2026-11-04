package uz.dostonbek.namozly.feature.tasbih

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.Dp
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHeader
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.LeaderboardRepository
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.TasbihRepository
import uz.dostonbek.namozly.core.model.Leaderboard
import uz.dostonbek.namozly.core.model.LeaderboardPeriod
import uz.dostonbek.namozly.core.model.LeaderboardScope
import uz.dostonbek.namozly.core.model.TasbihStats
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.model.UserProfile
import uz.dostonbek.namozly.core.ui.R
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class LeaderboardUiState(
    val profile: UserProfile? = null,
    val location: UserLocation = UserLocation.Default,
    val scope: LeaderboardScope = LeaderboardScope.DISTRICT,
    val period: LeaderboardPeriod = LeaderboardPeriod.WEEK,
    val board: Leaderboard? = null,
    val stats: TasbihStats = TasbihStats(0, 0, 0, 0, emptyMap()),
    val unread: Int = 0,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: LeaderboardRepository,
    locationRepository: LocationRepository,
    tasbih: TasbihRepository,
    notifications: NotificationRepository,
) : ViewModel() {
    private val scope = MutableStateFlow(LeaderboardScope.DISTRICT)
    private val period = MutableStateFlow(LeaderboardPeriod.WEEK)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val board = combine(scope, period) { s, p -> s to p }.flatMapLatest { (s, p) -> repository.leaderboard(s, p) }

    val uiState: StateFlow<LeaderboardUiState> = combine(
        combine(repository.profile, locationRepository.location, notifications.unreadCount()) { a, b, c -> Triple(a, b, c) },
        scope, period, board, tasbih.observeStats(),
    ) { (profile, loc, unread), s, p, b, stats ->
        LeaderboardUiState(profile, loc, s, p, b, stats, unread)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaderboardUiState())

    fun setScope(v: LeaderboardScope) { scope.value = v }
    fun setPeriod(v: LeaderboardPeriod) { period.value = v }

    fun register(first: String, last: String, phone: String, anonymous: Boolean) = viewModelScope.launch {
        val loc = uiState.value.location
        val name = first.trim() + (last.trim().firstOrNull()?.let { " $it." } ?: "")
        repository.register(
            UserProfile(
                uid = UUID.randomUUID().toString(),
                displayName = name,
                countryCode = loc.countryCode.ifBlank { "UZ" },
                region = loc.region, district = loc.district, mahalla = loc.mahalla,
                phone = phone.filter(Char::isDigit).ifBlank { null }, anonymous = anonymous,
            ),
        )
    }

    fun signOut() = viewModelScope.launch { repository.signOut() }
}

private fun initials(name: String) = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "?" }

/** Dizayn: Musobaqa.dc.html */
@Composable
internal fun LeaderboardRoute(onBack: () -> Unit, onOpenRegister: () -> Unit, viewModel: LeaderboardViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nf = NumberFormat.getIntegerInstance(Locale.getDefault())
    val profile = state.profile
    val board = state.board

    Box(Modifier.fillMaxSize().background(t.bg)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                DHeader(stringResource(R.string.leaderboard_title), onBack, stringResource(R.string.leaderboard_subtitle)) {
                    Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(t.surface).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                        DText(state.location.countryCode.ifBlank { "UZ" }, 13.sp, FontWeight.ExtraBold, t.strong)
                    }
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(LeaderboardScope.MAHALLA, LeaderboardScope.DISTRICT, LeaderboardScope.REGION, LeaderboardScope.COUNTRY)) { s ->
                        Pill(stringResource(s.labelRes()), s == state.scope) { viewModel.setScope(s) }
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(14.dp)).background(if (t.isDark) t.chip else Color(0xFFEAE3D3)).padding(3.dp)) {
                    LeaderboardPeriod.entries.forEach { p ->
                        val on = p == state.period
                        Box(
                            Modifier.weight(1f).height(34.dp).clip(RoundedCornerShape(11.dp)).background(if (on) t.surface else Color.Transparent).clickable { viewModel.setPeriod(p) },
                            contentAlignment = Alignment.Center,
                        ) { DText(stringResource(p.labelRes()), 13.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold, if (on) t.strong else t.muted) }
                    }
                }
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0E5A4A)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val area = board?.areaName?.ifBlank { null } ?: stringResource(R.string.leaderboard_area_unknown)
                    DText("${stringResource(state.period.labelRes())} · $area", 13.sp, FontWeight.SemiBold, Color(0xFFCFE3DA))
                    val total = when (state.period) { LeaderboardPeriod.DAY -> state.stats.today; LeaderboardPeriod.WEEK -> state.stats.week; LeaderboardPeriod.MONTH -> state.stats.month }
                    DText(stringResource(R.string.leaderboard_total, nf.format(total), board?.entries?.size ?: 0), 18.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("salawat" to "Salovot", "subhanallah" to "Subhanalloh", "alhamdulillah" to "Alhamdulillah", "allahuakbar" to "Allohu akbar").forEach { (id, label) ->
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF6F1E6).copy(alpha = 0.1f)).padding(8.dp)) {
                                DText(label, 10.sp, FontWeight.SemiBold, Color(0xFFCFE3DA), maxLines = 1)
                                DText(nf.format(state.stats.perDhikr[id] ?: 0), 14.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6), maxLines = 1)
                            }
                        }
                    }
                }
            }
            if (profile == null) {
                item {
                    DCard(Modifier.padding(horizontal = 20.dp), radius = 22.dp) {
                        DText(stringResource(R.string.leaderboard_join_title), 16.sp, FontWeight.ExtraBold, t.strong)
                        DText(stringResource(R.string.leaderboard_join_body), 13.sp, FontWeight.Normal, t.muted, lineHeight = 19.sp)
                        Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(t.primary).clickable(onClick = onOpenRegister), contentAlignment = Alignment.Center) {
                            DText(stringResource(R.string.profile_register), 14.sp, FontWeight.ExtraBold, Color.White)
                        }
                    }
                }
            } else if (board != null) {
                item {
                    Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                        DText(stringResource(R.string.leaderboard_leaders), 16.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
                        DText(stringResource(R.string.leaderboard_updates), 12.sp, FontWeight.Normal, t.muted)
                    }
                }
                item { Podium(board) }
                items(board.entries.drop(3)) { e ->
                    Row(
                        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (e.isMe) t.chip else t.surface).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DText(e.rank.toString(), 14.sp, FontWeight.ExtraBold, t.muted, Modifier.size(24.dp), align = TextAlign.Center)
                        Avatar(initials(e.displayName), 36.dp)
                        DText(e.displayName, 14.sp, FontWeight.Bold, t.text, Modifier.weight(1f), maxLines = 1)
                        DText(nf.format(e.count), 14.sp, FontWeight.ExtraBold, t.strong)
                    }
                }
                if (board.hiddenForPrivacy) {
                    item {
                        Row(
                            Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (t.isDark) t.goldSoft else Color(0xFFFBF4E4)).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Outlined.Lock, null, tint = t.accent, modifier = Modifier.size(18.dp))
                            DText(stringResource(R.string.leaderboard_offline_note), 12.sp, FontWeight.Normal, if (t.isDark) t.goldText else Color(0xFF5C4517), lineHeight = 17.sp)
                        }
                    }
                }
                item {
                    DText(stringResource(R.string.leaderboard_niyat), 12.sp, FontWeight.Normal, t.muted, Modifier.padding(horizontal = 24.dp), lineHeight = 17.sp)
                }
            }
        }
        // Pastki "Sizning o'rningiz" paneli
        val me = board?.entries?.firstOrNull { it.isMe }
        if (profile != null && me != null) {
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color(0xFF0A3D33))
                    .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(initials(me.displayName), 40.dp, gold = true)
                Column(Modifier.weight(1f)) {
                    DText(stringResource(R.string.leaderboard_your_place), 12.sp, FontWeight.SemiBold, Color(0xFFCFE3DA))
                    DText(stringResource(R.string.leaderboard_rank_n, me.rank), 17.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6))
                }
                DText(nf.format(me.count), 18.sp, FontWeight.ExtraBold, AccentGold)
            }
        }
    }
}

@Composable
private fun Podium(board: Leaderboard) {
    val t = NamozlyThemeTokens.t
    val nf = NumberFormat.getIntegerInstance(Locale.getDefault())
    val e = board.entries
    Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        listOf(1 to 96.dp, 0 to 128.dp, 2 to 80.dp).forEach { (idx, h) ->
            val entry = e.getOrNull(idx)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (entry != null) {
                    Avatar(initials(entry.displayName), if (idx == 0) 56.dp else 46.dp, gold = idx == 0)
                    DText(entry.displayName, 12.sp, FontWeight.Bold, t.text, maxLines = 1)
                    DText(nf.format(entry.count), 13.sp, FontWeight.ExtraBold, t.strong)
                }
                Box(
                    Modifier.fillMaxWidth().height(h).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(when (idx) { 0 -> AccentGold; 1 -> Color(0xFFCFD6D3); else -> Color(0xFFE3C9A0) }.copy(alpha = if (entry == null) 0.35f else 1f)),
                    contentAlignment = Alignment.Center,
                ) { DText((idx + 1).toString(), 28.sp, FontWeight.ExtraBold, Color(0xFF2B1F06).copy(alpha = 0.7f)) }
            }
        }
    }
}

@Composable
private fun Avatar(text: String, size: Dp, gold: Boolean = false) {
    Box(Modifier.size(size).clip(CircleShape).background(if (gold) AccentGold else Color(0xFF0E5A4A)), contentAlignment = Alignment.Center) {
        DText(text, (size.value / 2.8f).sp, FontWeight.ExtraBold, if (gold) Color(0xFF2B1F06) else Color.White)
    }
}

@Composable
private fun Pill(text: String, on: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface).clickable(onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { DText(text, 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text) }
}

/** Dizayn: Profil.dc.html */
@Composable
internal fun ProfileScreen(
    onBack: () -> Unit, onOpenRegister: () -> Unit, onOpenLeaderboard: () -> Unit,
    onOpenLocation: () -> Unit, onOpenNotifications: () -> Unit, onOpenSettings: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile
    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DHeader(stringResource(R.string.profile_title), onBack) }
        item {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(if (profile != null) Color(0xFF0E5A4A) else t.surface), contentAlignment = Alignment.Center) {
                    if (profile != null) DText(initials(profile.displayName), 22.sp, FontWeight.ExtraBold, Color.White)
                    else Icon(Icons.Outlined.Person, null, tint = t.muted, modifier = Modifier.size(30.dp))
                }
                Column {
                    DText(profile?.publicName ?: stringResource(R.string.profile_guest), 20.sp, FontWeight.ExtraBold, t.strong)
                    DText(
                        if (profile != null) listOfNotNull(profile.region, profile.district).joinToString(" · ").ifBlank { profile.countryCode }
                        else stringResource(R.string.profile_optional), 13.sp, FontWeight.Normal, t.muted,
                    )
                }
            }
        }
        item {
            Column(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0E5A4A)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DText(stringResource(R.string.leaderboard_title).uppercase(), 11.sp, FontWeight.ExtraBold, AccentGold, spacing = 0.06.em)
                DText(stringResource(R.string.profile_contest_title), 18.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6))
                DText(stringResource(R.string.profile_contest_body), 13.sp, FontWeight.Normal, Color(0xFFCFE3DA), lineHeight = 19.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).background(AccentGold).clickable { if (profile == null) onOpenRegister() else onOpenLeaderboard() }.padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(if (profile == null) R.string.profile_register else R.string.profile_view_contest), 14.sp, FontWeight.ExtraBold, Color(0xFF2B1F06)) }
                    if (profile == null) DText(stringResource(R.string.profile_not_yet), 13.sp, FontWeight.SemiBold, Color(0xFFCFE3DA))
                }
            }
        }
        item {
            DCard(Modifier.padding(horizontal = 20.dp), radius = 22.dp, spacing = 14.dp) {
                DText(stringResource(R.string.profile_what_happens), 15.sp, FontWeight.ExtraBold, t.strong)
                listOf(
                    Triple(Icons.Outlined.EmojiEvents, R.string.profile_rule1_t, R.string.profile_rule1_b),
                    Triple(Icons.Outlined.Lock, R.string.profile_rule2_t, R.string.profile_rule2_b),
                    Triple(Icons.Outlined.LocationOn, R.string.profile_rule3_t, R.string.profile_rule3_b),
                    Triple(Icons.Outlined.Public, R.string.profile_rule4_t, R.string.profile_rule4_b),
                    Triple(Icons.Outlined.VisibilityOff, R.string.profile_rule5_t, R.string.profile_rule5_b),
                ).forEach { (icon, title, body) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(t.chip), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = t.icon, modifier = Modifier.size(18.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            DText(stringResource(title), 14.sp, FontWeight.Bold, t.text)
                            DText(stringResource(body), 12.sp, FontWeight.Normal, t.muted, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }
        item {
            DCard(Modifier.padding(horizontal = 20.dp), radius = 18.dp, padding = PaddingValues(horizontal = 16.dp), spacing = 0.dp) {
                MenuRow(Icons.Outlined.LocationOn, stringResource(R.string.settings_location), listOfNotNull(state.location.city.ifBlank { null }, state.location.district).joinToString(" · "), false, onOpenLocation)
                MenuRow(Icons.Outlined.Notifications, stringResource(R.string.notifications_title), if (state.unread > 0) stringResource(R.string.notifications_new_n, state.unread) else null, true, onOpenNotifications)
                MenuRow(Icons.Outlined.Settings, stringResource(R.string.nav_settings), null, true, onOpenSettings)
            }
        }
        if (profile != null) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DText(stringResource(R.string.profile_delete), 14.sp, FontWeight.Bold, Color(0xFFC62828), Modifier.clip(RoundedCornerShape(8.dp)).clickable { viewModel.signOut() }.padding(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, value: String?, divider: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Column {
        if (divider) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
        Row(Modifier.fillMaxWidth().height(54.dp).clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = t.icon, modifier = Modifier.size(20.dp))
            DText(title, 15.sp, FontWeight.SemiBold, t.text, Modifier.weight(1f))
            if (!value.isNullOrBlank()) DText(value, 13.sp, FontWeight.SemiBold, t.muted, maxLines = 1)
            Icon(Icons.Rounded.ChevronRight, null, tint = t.faint, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Dizayn: Royxat.dc.html. SMS-tasdiqlash Firebase Phone Auth ulanganda qo'shiladi —
 * hozircha ma'lumotlar faqat qurilmada saqlanadi (bu ekranda ochiq aytiladi).
 */
@Composable
internal fun RegisterScreen(onBack: () -> Unit, onDone: () -> Unit, viewModel: LeaderboardViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var anonymous by rememberSaveable { mutableStateOf(false) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var done by rememberSaveable { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DHeader(stringResource(R.string.profile_register), onBack, stringResource(if (done) R.string.register_step_done else R.string.register_step_1)) }
        if (!done) {
            item { DText(stringResource(R.string.register_intro), 14.sp, FontWeight.Normal, t.muted, Modifier.padding(horizontal = 24.dp)) }
            item { Field(stringResource(R.string.register_first), first, { first = it }) }
            item { Field(stringResource(R.string.register_last), last, { last = it }) }
            item { Field(stringResource(R.string.register_phone), phone, { phone = it.filter(Char::isDigit).take(9) }, prefix = "+998", keyboard = KeyboardType.Phone) }
            item {
                DCard(Modifier.padding(horizontal = 20.dp), radius = 18.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DText(stringResource(R.string.register_anonymous), 15.sp, FontWeight.Bold, t.text)
                            DText(stringResource(R.string.register_anonymous_desc), 12.sp, FontWeight.Normal, t.muted)
                        }
                        Switch(anonymous, { anonymous = it }, colors = SwitchDefaults.colors(checkedTrackColor = t.primary))
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.Top) {
                    Checkbox(consent, { consent = it }, colors = CheckboxDefaults.colors(checkedColor = t.primary))
                    DText(stringResource(R.string.register_consent), 13.sp, FontWeight.Normal, t.text, Modifier.padding(top = 12.dp), lineHeight = 19.sp)
                }
            }
            item {
                DText(stringResource(R.string.register_sms_note), 12.sp, FontWeight.Normal, t.accent, Modifier.padding(horizontal = 24.dp), lineHeight = 17.sp)
            }
            item {
                val ok = first.isNotBlank() && consent
                Box(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(54.dp).clip(RoundedCornerShape(27.dp))
                        .background(if (ok) t.primary else t.dash)
                        .clickable(enabled = ok) { viewModel.register(first, last, phone, anonymous); done = true },
                    contentAlignment = Alignment.Center,
                ) { DText(stringResource(R.string.register_submit), 15.sp, FontWeight.ExtraBold, Color.White) }
            }
        } else {
            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(88.dp).clip(CircleShape).background(t.chip), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = t.primary, modifier = Modifier.size(48.dp))
                    }
                    DText(stringResource(R.string.register_welcome, first.trim()), 22.sp, FontWeight.ExtraBold, t.strong, align = TextAlign.Center)
                    DText(
                        stringResource(R.string.register_welcome_body, listOfNotNull(state.location.region, state.location.district).joinToString(", ").ifBlank { state.location.displayName }),
                        14.sp, FontWeight.Normal, t.muted, align = TextAlign.Center, lineHeight = 20.sp,
                    )
                    Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(27.dp)).background(t.primary).clickable(onClick = onDone), contentAlignment = Alignment.Center) {
                        DText(stringResource(R.string.profile_view_contest), 15.sp, FontWeight.ExtraBold, Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, prefix: String? = null, keyboard: KeyboardType = KeyboardType.Text) {
    val t = NamozlyThemeTokens.t
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DText(label, 13.sp, FontWeight.Bold, t.text)
        Row(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(t.surface).border(1.dp, t.line, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (prefix != null) DText(prefix, 15.sp, FontWeight.Bold, t.muted)
            BasicTextField(value, onChange, singleLine = true, modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = t.text),
                keyboardOptions = KeyboardOptions(keyboardType = keyboard), cursorBrush = SolidColor(t.primary))
        }
    }
}

private fun LeaderboardScope.labelRes(): Int = when (this) {
    LeaderboardScope.MAHALLA -> R.string.profile_mahalla
    LeaderboardScope.DISTRICT -> R.string.profile_district
    LeaderboardScope.REGION -> R.string.profile_region
    LeaderboardScope.COUNTRY -> R.string.profile_country
}

private fun LeaderboardPeriod.labelRes(): Int = when (this) {
    LeaderboardPeriod.DAY -> R.string.period_today
    LeaderboardPeriod.WEEK -> R.string.period_week
    LeaderboardPeriod.MONTH -> R.string.period_month
}

