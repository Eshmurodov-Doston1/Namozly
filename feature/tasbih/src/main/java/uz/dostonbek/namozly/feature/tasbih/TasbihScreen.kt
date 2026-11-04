package uz.dostonbek.namozly.feature.tasbih

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.model.TasbihStyle
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import uz.dostonbek.namozly.feature.tasbih.style.TasbihCounter

/** Har bir uslub foni — dizayn fayllaridan (Tasbeh*.dc.html). */
private fun TasbihStyle.background() = when (this) {
    TasbihStyle.CLASSIC -> Color(0xFF0A2F28)
    TasbihStyle.MINIMAL -> Color(0xFFF6F1E6)
    TasbihStyle.BEADS -> Color(0xFF0B2E27)
    TasbihStyle.CRESCENT -> Color(0xFF0A2722)
    TasbihStyle.DIGITAL -> Color(0xFFEFE8D9)
}
private val TasbihStyle.dark get() = this == TasbihStyle.CLASSIC || this == TasbihStyle.BEADS || this == TasbihStyle.CRESCENT

private val TargetOptions = listOf(33 to R.string.tasbih_hint_33, 99 to R.string.tasbih_hint_99, 100 to R.string.tasbih_hint_100, 500 to R.string.tasbih_hint_500, 1000 to R.string.tasbih_hint_1000, 0 to R.string.tasbih_hint_inf)

/** Dizayn: Tasbeh.dc.html (+ Minimal / Ipli / Hilol / Raqamli) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbihRoute(onBack: () -> Unit, onOpenLeaderboard: () -> Unit, viewModel: TasbihViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    var sheet by remember { mutableStateOf(false) }
    var toastRound by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) { onDispose { viewModel.flush() } }
    LaunchedEffect(toastRound) { if (toastRound > 0) { delay(2200); toastRound = 0 } }

    val style = state.style
    val dark = style.dark
    val fg = if (dark) Color(0xFFF6F1E6) else Color(0xFF0A3D33)
    val sub = if (dark) Color(0xFFBFD5CC) else Color(0xFF5E625C)
    val chipBg = if (dark) Color(0xFFF6F1E6).copy(alpha = 0.10f) else Color.White
    val targetLabel = if (state.target > 0) state.target.toString() else "∞"

    Box(Modifier.fillMaxSize().background(style.background())) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RoundIcon(Icons.AutoMirrored.Rounded.ArrowBack, chipBg, fg, onBack)
                DText(stringResource(R.string.nav_tasbih), 22.sp, FontWeight.ExtraBold, fg, Modifier.weight(1f))
                Row(
                    Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).background(chipBg).clickable { sheet = true }.padding(start = 12.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    DText("$targetLabel ×", 14.sp, FontWeight.ExtraBold, if (dark) AccentGold else Color(0xFF8A6420))
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = if (dark) AccentGold else Color(0xFF8A6420), modifier = Modifier.size(14.dp))
                }
                RoundIcon(Icons.Outlined.EmojiEvents, chipBg, fg, onOpenLeaderboard)
                RoundIcon(Icons.Rounded.Refresh, chipBg, fg) { viewModel.reset() }
            }
            // Uslublar
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (dark) Color(0xFFF6F1E6).copy(alpha = 0.08f) else Color(0xFFE7DFCF)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TasbihStyle.entries.forEach { s ->
                    val on = s == style
                    Box(
                        Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (on) (if (dark) Color(0xFFF6F1E6) else Color.White) else Color.Transparent)
                            .clickable { viewModel.setStyle(s) },
                        contentAlignment = Alignment.Center,
                    ) {
                        DText(stringResource(s.labelRes()), 12.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold,
                            if (on) Color(0xFF0A3D33) else if (dark) Color(0xFFDCE8E2) else Color(0xFF4E5249), maxLines = 1)
                    }
                }
            }
            // Zikrlar
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 20.dp)) {
                items(state.dhikrs, key = { it.id }) { d ->
                    val on = d.id == state.dhikr?.id
                    Box(
                        Modifier.height(40.dp).clip(RoundedCornerShape(20.dp))
                            .background(if (on) AccentGold else Color.Transparent)
                            .then(if (on) Modifier else Modifier.border(1.dp, if (dark) Color(0xFFF6F1E6).copy(alpha = 0.22f) else Color(0xFFD9D0BE), RoundedCornerShape(20.dp)))
                            .clickable { viewModel.selectDhikr(d) }.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(d.text.localized(), 13.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold, if (on) Color(0xFF2B1F06) else fg) }
                }
            }
            state.dhikr?.let { d ->
                Column(Modifier.fillMaxWidth().height(80.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(d.arabic, fontFamily = AmiriFamily, fontSize = 34.sp, lineHeight = 46.sp, color = fg, textAlign = TextAlign.Center)
                    DText(d.meaning.localized(), 14.sp, FontWeight.Normal, sub, align = TextAlign.Center)
                }
            }
            TasbihCounter(
                style = style, count = state.count, inRound = state.inRound, target = state.target, progress = state.progress,
                onTap = {
                    val event = viewModel.increment()
                    if (event is TasbihEvent.TargetReached) toastRound = state.rounds + 1
                    if (state.haptics) view.performHapticFeedback(
                        if (event is TasbihEvent.TargetReached) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.KEYBOARD_TAP,
                    )
                },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(state.rounds.toString(), stringResource(R.string.tasbih_round), dark, Modifier.weight(1f))
                Stat(state.count.toString(), stringResource(R.string.period_total), dark, Modifier.weight(1f))
                Stat(state.stats.today.toString(), stringResource(R.string.period_today), dark, Modifier.weight(1f))
            }
            DText(stringResource(R.string.tasbih_tap_hint, targetLabel), 12.sp, FontWeight.Normal, sub, Modifier.fillMaxWidth(), align = TextAlign.Center)
        }
        AnimatedVisibility(
            toastRound > 0,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp),
            enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it },
        ) {
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(AccentGold).padding(horizontal = 18.dp, vertical = 12.dp)) {
                DText(stringResource(R.string.tasbih_round_done, toastRound), 14.sp, FontWeight.ExtraBold, Color(0xFF2B1F06))
            }
        }
    }

    if (sheet) {
        var custom by remember { mutableStateOf("") }
        ModalBottomSheet(onDismissRequest = { sheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Color(0xFFF6F1E6)) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    DText(stringResource(R.string.tasbih_sheet_title), 20.sp, FontWeight.ExtraBold, Color(0xFF0A3D33))
                    DText(stringResource(R.string.tasbih_sheet_sub), 13.sp, FontWeight.Normal, Color(0xFF5E625C))
                }
                TargetOptions.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (value, hint) ->
                            val on = state.target == value
                            Column(
                                Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if (on) Color(0xFF0E5A4A) else Color.White)
                                    .then(if (on) Modifier else Modifier.border(2.dp, Color(0xFFEFE8D9), RoundedCornerShape(18.dp)))
                                    .clickable { viewModel.setTarget(value); viewModel.reset(); sheet = false },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                            ) {
                                DText(if (value > 0) value.toString() else "∞", 20.sp, FontWeight.ExtraBold, if (on) Color.White else Color(0xFF0A3D33))
                                DText(stringResource(hint), 11.sp, FontWeight.SemiBold, if (on) Color(0xFFCFE3DA) else Color(0xFF5E625C), maxLines = 1)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                        if (custom.isEmpty()) DText(stringResource(R.string.tasbih_custom_input), 15.sp, FontWeight.Normal, Color(0xFF9AA096))
                        BasicTextField(custom, { v -> custom = v.filter(Char::isDigit).take(6) }, singleLine = true,
                            textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A3D33)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), cursorBrush = SolidColor(Color(0xFF0E5A4A)), modifier = Modifier.fillMaxWidth())
                    }
                    Box(
                        Modifier.height(52.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF0E5A4A))
                            .clickable { custom.toIntOrNull()?.takeIf { it > 0 }?.let { viewModel.setTarget(it); viewModel.reset(); sheet = false } }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(R.string.tasbih_choose), 14.sp, FontWeight.ExtraBold, Color.White) }
                }
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, bg: Color, fg: Color, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(bg).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Stat(value: String, label: String, dark: Boolean, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(if (dark) Color(0xFFF6F1E6).copy(alpha = 0.08f) else Color.White).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DText(value, 18.sp, FontWeight.ExtraBold, if (dark) Color(0xFFF6F1E6) else Color(0xFF0A3D33))
        DText(label, 11.sp, FontWeight.SemiBold, if (dark) Color(0xFFBFD5CC) else Color(0xFF5E625C))
    }
}

private fun TasbihStyle.labelRes(): Int = when (this) {
    TasbihStyle.CLASSIC -> R.string.tasbih_style_classic
    TasbihStyle.MINIMAL -> R.string.tasbih_style_minimal
    TasbihStyle.BEADS -> R.string.tasbih_style_beads
    TasbihStyle.CRESCENT -> R.string.tasbih_style_crescent
    TasbihStyle.DIGITAL -> R.string.tasbih_style_digital
}
