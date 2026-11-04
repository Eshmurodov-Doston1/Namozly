package uz.dostonbek.namozly.feature.qaza

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
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
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHeader
import uz.dostonbek.namozly.core.designsystem.component.DProgress
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.QazaRepository
import uz.dostonbek.namozly.core.domain.util.QazaCalculator
import uz.dostonbek.namozly.core.model.QazaPrayer
import uz.dostonbek.namozly.core.model.QazaRecord
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.labelRes
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@Serializable data object QazaRoute

fun NavController.navigateToQaza() = navigate(QazaRoute)

fun NavGraphBuilder.qazaScreen(onBack: () -> Unit) {
    composable<QazaRoute> { QazaScreen(onBack) }
}

@HiltViewModel
class QazaViewModel @Inject constructor(private val repository: QazaRepository) : ViewModel() {
    val records: StateFlow<List<QazaRecord>> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QazaPrayer.entries.map { QazaRecord(it, 0, 0) })

    /** "−" — bitta qazo ado etildi. */
    fun complete(p: QazaPrayer) = viewModelScope.launch { repository.complete(p) }
    /** "+" — qazo qo'shish. */
    fun add(p: QazaPrayer) = viewModelScope.launch {
        repository.setRemaining(p, (records.value.firstOrNull { it.prayer == p }?.remaining ?: 0) + 1)
    }
    fun set(p: QazaPrayer, n: Int) = viewModelScope.launch { repository.setRemaining(p, n) }
    fun setFromYears(years: Int, months: Int) = viewModelScope.launch {
        val days = QazaCalculator.missedDaysForYears(years, months)
        QazaPrayer.entries.forEach { repository.setRemaining(it, days) }
    }
}

/** Dizayn: Qazo.dc.html */
@Composable
internal fun QazaScreen(onBack: () -> Unit, viewModel: QazaViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val records by viewModel.records.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<QazaPrayer?>(null) }
    var calculator by remember { mutableStateOf(false) }
    val remaining = records.sumOf { it.remaining }
    val done = records.sumOf { it.completed }
    val nf = NumberFormat.getIntegerInstance(Locale.getDefault())
    val cream = Color(0xFFF6F1E6)

    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DHeader(stringResource(R.string.qaza_title), onBack) }
        item {
            Column(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0A3D33))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        DText(stringResource(R.string.qaza_remaining), 13.sp, FontWeight.SemiBold, Color(0xFFCFE3DA))
                        DText(nf.format(remaining), 40.sp, FontWeight.ExtraBold, cream, spacing = (-0.02).em)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        DText(stringResource(R.string.qaza_read), 13.sp, FontWeight.SemiBold, Color(0xFFCFE3DA))
                        DText(nf.format(done), 20.sp, FontWeight.ExtraBold, AccentGold)
                    }
                }
                DProgress(if (remaining + done == 0) 0f else done.toFloat() / (remaining + done), cream.copy(alpha = 0.18f), AccentGold)
                if (remaining > 0) {
                    val days = (remaining + 1) / 2
                    val span = when {
                        days >= 365 -> stringResource(R.string.qaza_years_n, (days + 182) / 365)
                        days >= 30 -> stringResource(R.string.qaza_months_n, days / 30)
                        else -> stringResource(R.string.qaza_days_n, days)
                    }
                    DText(stringResource(R.string.qaza_estimate, span), 13.sp, FontWeight.Normal, Color(0xFFE7DDC7))
                }
            }
        }
        item {
            DCard(Modifier.padding(horizontal = 20.dp), padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), spacing = 0.dp) {
                records.forEachIndexed { i, r ->
                    if (i > 0) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                    Row(Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f).clickable { editing = r.prayer }) {
                            DText(stringResource(r.prayer.labelRes()), 15.sp, FontWeight.Bold, t.text)
                            if (r.prayer == QazaPrayer.WITR) DText(stringResource(R.string.qaza_wajib), 11.sp, FontWeight.Normal, t.muted)
                        }
                        StepButton("−", filled = false, enabled = r.remaining > 0) { viewModel.complete(r.prayer) }
                        DText(nf.format(r.remaining), 17.sp, FontWeight.ExtraBold, t.strong, Modifier.width(52.dp).clickable { editing = r.prayer }, align = TextAlign.Center)
                        StepButton("+", filled = true, enabled = true) { viewModel.add(r.prayer) }
                    }
                }
            }
        }
        item {
            val dash = if (t.isDark) t.dash else Color(0xFFC9B98F)
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp))
                    .drawBehind {
                        val w = 2.dp.toPx()
                        drawRoundRect(dash, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), CornerRadius(16.dp.toPx()),
                            style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))))
                    }
                    .clickable { calculator = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(Icons.Outlined.Calculate, null, tint = t.strong, modifier = Modifier.size(18.dp))
                DText(stringResource(R.string.qaza_calc_by_age), 14.sp, FontWeight.Bold, t.strong)
            }
        }
        item { DText(stringResource(R.string.qaza_note), 12.sp, FontWeight.Normal, t.muted, Modifier.padding(horizontal = 24.dp), lineHeight = 17.sp) }
    }

    editing?.let { prayer ->
        var text by remember(prayer) { mutableStateOf(records.firstOrNull { it.prayer == prayer }?.remaining?.toString().orEmpty()) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(prayer.labelRes())) },
            text = {
                OutlinedTextField(text, { v -> text = v.filter(Char::isDigit).take(6) }, label = { Text(stringResource(R.string.qaza_remaining)) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            },
            confirmButton = { TextButton(onClick = { viewModel.set(prayer, text.toIntOrNull() ?: 0); editing = null }) { Text(stringResource(R.string.common_save)) } },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
    if (calculator) {
        var years by remember { mutableStateOf("") }
        var months by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { calculator = false },
            title = { Text(stringResource(R.string.qaza_calc_by_age)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.qaza_calculate_hint))
                    val kb = KeyboardOptions(keyboardType = KeyboardType.Number)
                    OutlinedTextField(years, { years = it.filter(Char::isDigit).take(2) }, label = { Text(stringResource(R.string.qaza_years)) }, singleLine = true, keyboardOptions = kb)
                    OutlinedTextField(months, { months = it.filter(Char::isDigit).take(2) }, label = { Text(stringResource(R.string.qaza_months)) }, singleLine = true, keyboardOptions = kb)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.setFromYears(years.toIntOrNull() ?: 0, months.toIntOrNull() ?: 0); calculator = false }) { Text(stringResource(R.string.common_save)) } },
            dismissButton = { TextButton(onClick = { calculator = false }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

@Composable
private fun StepButton(label: String, filled: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(if (filled) t.primary else t.bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { DText(label, 20.sp, FontWeight.Bold, if (filled) Color.White else t.strong.copy(alpha = if (enabled) 1f else 0.35f)) }
}
