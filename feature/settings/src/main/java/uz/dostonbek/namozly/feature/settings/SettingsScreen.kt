package uz.dostonbek.namozly.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DOverline
import uz.dostonbek.namozly.core.designsystem.component.DPill
import uz.dostonbek.namozly.core.designsystem.component.DRow
import uz.dostonbek.namozly.core.designsystem.component.DSegmented
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.CalculationMethod
import uz.dostonbek.namozly.core.model.Madhab
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.labelRes

/** TODO: rasmiy aloqa manzilini kiriting (masalan, mailto:support@... yoki https://t.me/...). */
private const val CONTACT_URI = "mailto:"
private const val APP_VERSION = "v1.0.0"

private enum class SettingsDialog { LANGUAGE, METHOD, MADHAB, ADJUST, PRAYERS, ABOUT }

/** Dizayn: Sozlamalar.dc.html */
@Composable
internal fun SettingsScreen(
    onOpenLocation: () -> Unit,
    onOpenNotifications: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val t = NamozlyThemeTokens.t
    val s by viewModel.state.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val context = LocalContext.current
    val languageNames = mapOf(
        AppLanguage.UZ_LATN to "O'zbekcha (lotin)", AppLanguage.UZ_CYRL to "Ўзбекча (кирилл)",
        AppLanguage.RU to "Русский", AppLanguage.EN to "English",
    )

    LazyColumn(
        Modifier.fillMaxSize().background(t.bg),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(Modifier.statusBarsPadding().padding(top = 20.dp, start = 4.dp).height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                DText(stringResource(R.string.nav_settings), 24.sp, FontWeight.ExtraBold, t.strong)
            }
        }
        item { Section(stringResource(R.string.settings_general)) }
        item {
            Group {
                DRow(stringResource(R.string.settings_app_language), languageNames[s.language], onClick = { dialog = SettingsDialog.LANGUAGE })
                DRow(stringResource(R.string.settings_appearance), height = 54.dp, divider = true, chevron = false, trailing = {
                    val modes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
                    DSegmented(
                        listOf(stringResource(R.string.theme_light), stringResource(R.string.theme_dark), stringResource(R.string.theme_system)),
                        modes.indexOf(s.themeMode),
                    ) { viewModel.setTheme(modes[it]) }
                })
            }
        }
        item { Section(stringResource(R.string.settings_prayer_times)) }
        item {
            Group {
                DRow(
                    stringResource(R.string.settings_location),
                    if (location.isAutomatic) stringResource(R.string.settings_location_auto) else location.city.ifBlank { location.displayName },
                    onClick = onOpenLocation,
                )
                DRow(stringResource(R.string.settings_method), stringResource(s.prayer.method.labelRes()), divider = true, onClick = { dialog = SettingsDialog.METHOD })
                DRow(
                    stringResource(R.string.settings_madhab),
                    stringResource(if (s.prayer.madhab == Madhab.HANAFI) R.string.madhab_hanafi else R.string.madhab_shafi),
                    divider = true, onClick = { dialog = SettingsDialog.MADHAB },
                )
                val adjusted = s.prayer.adjustments.count { it.value != 0 }
                DRow(stringResource(R.string.settings_adjustments), if (adjusted > 0) "$adjusted" else null, divider = true, onClick = { dialog = SettingsDialog.ADJUST })
                DRow(stringResource(R.string.settings_online_source), divider = true, chevron = false, trailing = {
                    DSwitch(s.prayer.useOnlineSource) { v -> viewModel.updatePrayer { it.copy(useOnlineSource = v) } }
                })
            }
        }
        item { Section(stringResource(R.string.settings_reminders)) }
        item {
            val n = s.notifications
            Group {
                DRow(stringResource(R.string.settings_prayer_alerts), chevron = false, trailing = {
                    DSwitch(n.enabled) { v -> viewModel.updateNotifications { it.copy(enabled = v) } }
                })
                if (n.enabled) {
                    DRow(stringResource(R.string.settings_per_prayer), "${n.prayers.size}/6", divider = true, onClick = { dialog = SettingsDialog.PRAYERS })
                    DRow(stringResource(R.string.settings_pre_alert_n, 10), divider = true, chevron = false, trailing = {
                        DSwitch(n.preAlertMinutes > 0) { v -> viewModel.updateNotifications { it.copy(preAlertMinutes = if (v) 10 else 0) } }
                    })
                    DRow(stringResource(R.string.settings_notif_ramadan), divider = true, chevron = false, trailing = {
                        DSwitch(n.ramadanAlerts) { v -> viewModel.updateNotifications { it.copy(ramadanAlerts = v) } }
                    })
                }
                DRow(stringResource(R.string.settings_notif_news), divider = true, chevron = false, trailing = {
                    DSwitch(n.news) { v -> viewModel.updateNotifications { it.copy(news = v) } }
                })
                DRow(stringResource(R.string.settings_haptics), divider = true, chevron = false, trailing = {
                    DSwitch(s.haptics) { viewModel.setHaptics(it) }
                })
            }
        }
        item { Section(stringResource(R.string.settings_other)) }
        item {
            Group {
                DRow(stringResource(R.string.notifications_title), onClick = onOpenNotifications)
                DRow(stringResource(R.string.settings_feedback), divider = true, onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(CONTACT_URI)).putExtra(Intent.EXTRA_SUBJECT, "Namozly")) }
                })
                DRow(stringResource(R.string.settings_about_app), divider = true, onClick = { dialog = SettingsDialog.ABOUT }, trailing = {
                    DText(APP_VERSION, 14.sp, FontWeight.Normal, t.muted)
                })
            }
        }
    }

    when (dialog) {
        SettingsDialog.LANGUAGE -> ChoiceDialog(stringResource(R.string.settings_app_language), onDismiss = { dialog = null }) {
            AppLanguage.entries.forEach { l ->
                DPill(languageNames.getValue(l), s.language == l, { viewModel.setLanguage(l); dialog = null }, Modifier.fillMaxWidth())
            }
        }
        SettingsDialog.METHOD -> ChoiceDialog(stringResource(R.string.settings_method), onDismiss = { dialog = null }) {
            CalculationMethod.entries.forEach { m ->
                DPill(stringResource(m.labelRes()), s.prayer.method == m, { viewModel.updatePrayer { it.copy(method = m) }; dialog = null }, Modifier.fillMaxWidth())
            }
        }
        SettingsDialog.MADHAB -> ChoiceDialog(stringResource(R.string.settings_madhab), onDismiss = { dialog = null }) {
            listOf(Madhab.HANAFI to R.string.madhab_hanafi, Madhab.SHAFI to R.string.madhab_shafi).forEach { (m, label) ->
                DPill(stringResource(label), s.prayer.madhab == m, { viewModel.updatePrayer { it.copy(madhab = m) }; dialog = null }, Modifier.fillMaxWidth())
            }
        }
        SettingsDialog.ADJUST -> ChoiceDialog(stringResource(R.string.settings_adjustments), onDismiss = { dialog = null }) {
            DText(stringResource(R.string.settings_adjustments_desc), 12.sp, FontWeight.Normal, t.muted)
            Prayer.entries.forEach { p ->
                val value = s.prayer.adjustments[p] ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DText(stringResource(p.labelRes()), 15.sp, FontWeight.SemiBold, t.text, Modifier.weight(1f))
                    IconButton(onClick = { viewModel.updatePrayer { it.copy(adjustments = it.adjustments + (p to (value - 1).coerceAtLeast(-30))) } }) { Icon(Icons.Rounded.Remove, null) }
                    DText(if (value > 0) "+$value" else "$value", 15.sp, FontWeight.ExtraBold, t.strong, Modifier.width(36.dp))
                    IconButton(onClick = { viewModel.updatePrayer { it.copy(adjustments = it.adjustments + (p to (value + 1).coerceAtMost(30))) } }) { Icon(Icons.Rounded.Add, null) }
                }
            }
        }
        SettingsDialog.PRAYERS -> ChoiceDialog(stringResource(R.string.settings_per_prayer), onDismiss = { dialog = null }) {
            val n = s.notifications
            Prayer.entries.forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DText(stringResource(p.labelRes()), 15.sp, FontWeight.SemiBold, t.text, Modifier.weight(1f))
                    DSwitch(p in n.prayers) { v -> viewModel.updateNotifications { it.copy(prayers = if (v) it.prayers + p else it.prayers - p) } }
                }
            }
        }
        SettingsDialog.ABOUT -> ChoiceDialog("Namozly $APP_VERSION", onDismiss = { dialog = null }) {
            DText(stringResource(R.string.settings_about_body), 13.sp, FontWeight.Normal, t.text, lineHeight = 19.sp)
        }
        null -> Unit
    }
}

@Composable
private fun Section(text: String) {
    DOverline(text, Modifier.padding(start = 4.dp, top = 4.dp), color = NamozlyThemeTokens.t.muted)
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    DCard(radius = 18.dp, padding = PaddingValues(horizontal = 16.dp), spacing = 0.dp) { content() }
}

@Composable
private fun DSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val t = NamozlyThemeTokens.t
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(checkedTrackColor = t.primary, checkedThumbColor = Color.White, uncheckedBorderColor = t.dash),
    )
}

@Composable
private fun ChoiceDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NamozlyThemeTokens.t.bg,
        title = { Text(title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() } },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) } },
    )
}

private fun CalculationMethod.labelRes(): Int = when (this) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> R.string.method_mwl
    CalculationMethod.KARACHI -> R.string.method_karachi
    CalculationMethod.ISNA -> R.string.method_isna
    CalculationMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
    CalculationMethod.EGYPTIAN -> R.string.method_egypt
    CalculationMethod.DUBAI -> R.string.method_dubai
    CalculationMethod.TURKEY -> R.string.method_turkey
    CalculationMethod.RUSSIA -> R.string.method_russia
}
