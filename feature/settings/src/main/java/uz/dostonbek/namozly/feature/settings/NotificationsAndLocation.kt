package uz.dostonbek.namozly.feature.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHeader
import uz.dostonbek.namozly.core.designsystem.component.DIconButton
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.component.EmptyState
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.model.AppNotification
import uz.dostonbek.namozly.core.model.NotificationType
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.ui.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class NotifFilter { ALL, PRAYER, CONTEST, LIVE, NEWS }

private fun NotificationType.filter() = when (this) {
    NotificationType.PRAYER, NotificationType.PRE_ALERT, NotificationType.RAMADAN, NotificationType.FRIDAY -> NotifFilter.PRAYER
    NotificationType.NEWS, NotificationType.AYAH, NotificationType.SYSTEM -> NotifFilter.NEWS
}

private fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.PRAYER, NotificationType.PRE_ALERT -> Icons.Outlined.AccessTime
    NotificationType.RAMADAN -> Icons.Outlined.WbTwilight
    NotificationType.FRIDAY -> Icons.Outlined.Mosque
    NotificationType.AYAH -> Icons.Outlined.Campaign
    NotificationType.NEWS -> Icons.Outlined.Campaign
    NotificationType.SYSTEM -> Icons.Outlined.Info
}

/** Dizayn: Bildirishnomalar.dc.html */
@Composable
internal fun NotificationsScreen(onBack: () -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val items by viewModel.items.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(NotifFilter.ALL) }
    val unread = items.count { !it.read }
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()
    val list = items.filter { filter == NotifFilter.ALL || it.type.filter() == filter }
    val grouped = list.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
    val locale = Locale.getDefault()

    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            DHeader(stringResource(R.string.notifications_title), onBack, if (unread > 0) stringResource(R.string.notifications_new_n, unread) else null) {
                DIconButton(Icons.Rounded.DoneAll, { viewModel.markAllRead() }, contentDescription = stringResource(R.string.notifications_mark_read))
            }
        }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(NotifFilter.entries.filter { it != NotifFilter.CONTEST && it != NotifFilter.LIVE }) { f ->
                    val on = filter == f
                    val label = when (f) {
                        NotifFilter.ALL -> stringResource(R.string.common_all) + if (unread > 0) " · $unread" else ""
                        NotifFilter.PRAYER -> stringResource(R.string.notif_filter_prayer)
                        NotifFilter.NEWS -> stringResource(R.string.settings_notif_news)
                        else -> ""
                    }
                    Box(
                        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface).clickable { filter = f }.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(label, 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text) }
                }
            }
        }
        if (list.isEmpty()) {
            item { EmptyState(Icons.Outlined.NotificationsNone, stringResource(R.string.notifications_empty), stringResource(R.string.notifications_empty_desc)) }
        }
        grouped.forEach { (date, group) ->
            item(key = "h$date") {
                val label = when (date) {
                    today -> stringResource(R.string.period_today)
                    today.minusDays(1) -> stringResource(R.string.notifications_yesterday)
                    else -> date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
                }
                Row(Modifier.padding(horizontal = 24.dp).padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    DText(label.uppercase(), 12.sp, FontWeight.Bold, t.muted, Modifier.weight(1f), spacing = 0.04.em)
                }
            }
            items(group, key = { it.id }) { n -> NotificationRow(n, date == today) }
        }
        if (items.isNotEmpty()) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { viewModel.clear() }) { DText(stringResource(R.string.notifications_clear), 13.sp, FontWeight.Bold, t.muted) }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, today: Boolean) {
    val t = NamozlyThemeTokens.t
    val time = Instant.ofEpochMilli(n.timestamp).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    val (bg, fg) = when (n.type.filter()) {
        NotifFilter.PRAYER -> t.chip to t.icon
        else -> t.goldSoft to t.accent
    }
    Row(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(t.surface).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
            Icon(n.type.icon(), null, tint = fg, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DText(n.title, 15.sp, FontWeight.ExtraBold, t.text, Modifier.weight(1f), maxLines = 1)
                DText(if (today) time else time, 12.sp, FontWeight.SemiBold, t.muted)
                if (!n.read) Box(Modifier.padding(start = 6.dp).size(8.dp).clip(CircleShape).background(AccentGold))
            }
            DText(n.body, 13.sp, FontWeight.Normal, t.muted, lineHeight = 19.sp)
        }
    }
}

/** Dizayn: Joylashuv.dc.html */
@Composable
internal fun LocationScreen(onBack: () -> Unit, viewModel: LocationViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val current by viewModel.current.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var detectFailed by rememberSaveable { mutableStateOf(false) }
    var draft by remember { mutableStateOf<UserLocation?>(null) }
    var editing by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(current) { draft = current }
    val d = draft ?: current

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) {
            viewModel.detect { ok -> detectFailed = !ok }
        } else {
            detectFailed = true
        }
    }

    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { DHeader(stringResource(R.string.settings_location), onBack, stringResource(R.string.location_subtitle)) }
        item {
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFF0E5A4A))
                    .clickable { permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF6F1E6).copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.MyLocation, null, tint = AccentGold, modifier = Modifier.size(26.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    DText(
                        stringResource(if (current.isAutomatic) R.string.location_gps_detected else R.string.location_detect),
                        15.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6),
                    )
                    DText(listOfNotNull(current.region, current.district).joinToString(", ").ifBlank { current.displayName }, 13.sp, FontWeight.Normal, Color(0xFFCFE3DA), maxLines = 1)
                    val extra = listOfNotNull(current.mahalla, current.accuracyMeters?.let { stringResource(R.string.location_accuracy, it) }).joinToString(" · ")
                    if (extra.isNotBlank()) DText(extra, 12.sp, FontWeight.Normal, Color(0xFFE7DDC7), maxLines = 1)
                }
            }
        }
        if (detectFailed) item { DText(stringResource(R.string.location_detect_failed), 13.sp, FontWeight.SemiBold, t.accent, Modifier.padding(horizontal = 24.dp)) }
        item {
            DCard(Modifier.padding(horizontal = 20.dp), padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), spacing = 0.dp) {
                Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
                    DText(stringResource(R.string.profile_country), 15.sp, FontWeight.SemiBold, t.text, Modifier.weight(1f))
                    DText(d.country.ifBlank { "—" }, 14.sp, FontWeight.SemiBold, t.icon)
                    Icon(Icons.Outlined.Lock, null, tint = t.faint, modifier = Modifier.padding(start = 6.dp).size(16.dp))
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (t.isDark) t.goldSoft else Color(0xFFFBF4E4)).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Info, null, tint = t.accent, modifier = Modifier.size(16.dp))
                    DText(stringResource(R.string.location_country_locked), 12.sp, FontWeight.Normal, if (t.isDark) t.goldText else Color(0xFF5C4517), lineHeight = 17.sp)
                }
                listOf(
                    Triple("region", R.string.location_region, d.region),
                    Triple("district", R.string.profile_district, d.district),
                    Triple("mahalla", R.string.profile_mahalla, d.mahalla),
                    Triple("street", R.string.location_street, d.street),
                ).forEach { (key, label, value) ->
                    HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                    Row(Modifier.fillMaxWidth().height(56.dp).clickable { editing = key }, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DText(stringResource(label), 12.sp, FontWeight.SemiBold, t.muted)
                            DText(value?.ifBlank { null } ?: stringResource(R.string.location_not_selected), 15.sp, FontWeight.Bold, if (value.isNullOrBlank()) t.faint else t.text, maxLines = 1)
                        }
                        if (key == "mahalla" || key == "street") DText(stringResource(R.string.common_optional), 11.sp, FontWeight.SemiBold, t.faint)
                    }
                }
            }
        }
        item {
            DCard(Modifier.padding(horizontal = 20.dp), spacing = 10.dp) {
                DText(stringResource(R.string.location_affects), 13.sp, FontWeight.Bold, t.text)
                listOf(
                    Icons.Outlined.AccessTime to R.string.settings_prayer_times,
                    Icons.Outlined.Mosque to R.string.home_nearest_mosques,
                    Icons.Outlined.Explore to R.string.qibla_direction_title,
                    Icons.Outlined.EmojiEvents to R.string.leaderboard_title,
                ).forEach { (icon, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(icon, null, tint = t.icon, modifier = Modifier.size(16.dp))
                        DText(stringResource(label), 13.sp, FontWeight.Normal, t.text)
                    }
                }
            }
        }
        item {
            Box(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(54.dp).clip(RoundedCornerShape(27.dp)).background(t.primary)
                    .clickable { draft?.let { viewModel.select(it, onBack) } },
                contentAlignment = Alignment.Center,
            ) { DText(stringResource(R.string.common_save), 15.sp, FontWeight.ExtraBold, Color.White) }
        }
        item { DText(stringResource(R.string.location_search_title), 15.sp, FontWeight.ExtraBold, t.strong, Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) }
        item {
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(t.surface).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(18.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) DText(stringResource(R.string.location_search_hint), 15.sp, FontWeight.Normal, t.faint)
                    BasicTextField(
                        query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, color = t.text), cursorBrush = SolidColor(t.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { viewModel.search(query) }),
                    )
                }
                if (searching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = t.primary)
            }
        }
        items(results) { r ->
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(t.surface).clickable { viewModel.select(r, onBack) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    DText(r.city, 15.sp, FontWeight.Bold, t.text, maxLines = 1)
                    DText(listOfNotNull(r.district, r.region, r.country).filter { it.isNotBlank() }.joinToString(", "), 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                }
                Icon(Icons.Rounded.Check, null, tint = t.faint, modifier = Modifier.size(18.dp))
            }
        }
        item { DText(stringResource(R.string.location_osm_note), 11.sp, FontWeight.Normal, t.muted, Modifier.padding(horizontal = 24.dp)) }
    }

    editing?.let { key ->
        var text by remember(key) {
            mutableStateOf(when (key) { "region" -> d.region; "district" -> d.district; "mahalla" -> d.mahalla; else -> d.street }.orEmpty())
        }
        AlertDialog(
            onDismissRequest = { editing = null },
            containerColor = t.bg,
            title = { Text(stringResource(when (key) { "region" -> R.string.location_region; "district" -> R.string.profile_district; "mahalla" -> R.string.profile_mahalla; else -> R.string.location_street })) },
            text = { OutlinedTextField(text, { text = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    val v = text.trim().ifBlank { null }
                    draft = when (key) {
                        "region" -> d.copy(region = v); "district" -> d.copy(district = v); "mahalla" -> d.copy(mahalla = v); else -> d.copy(street = v)
                    }
                    editing = null
                }) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

