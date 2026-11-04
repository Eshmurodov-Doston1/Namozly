package uz.dostonbek.namozly.feature.qibla

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHint
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.sensor.isAlignedToQibla
import uz.dostonbek.namozly.core.ui.sensor.magneticDeclination
import uz.dostonbek.namozly.core.ui.sensor.rememberDeviceHeading
import uz.dostonbek.namozly.core.ui.sensor.trueHeading
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class QiblaViewModel @Inject constructor(locationRepository: LocationRepository) : ViewModel() {
    val location: StateFlow<UserLocation> = locationRepository.location
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserLocation.Default)
}

/** Dizayn: Qibla.dc.html */
@Composable
internal fun QiblaRoute(onOpenLocation: () -> Unit, viewModel: QiblaViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val location by viewModel.location.collectAsStateWithLifecycle()
    val headingState by rememberDeviceHeading()
    val point = location.point
    val bearing = remember(point) { QiblaCalculator.bearing(point) }
    val distance = remember(point) { QiblaCalculator.distanceKm(point) }
    val declination = remember(point) { magneticDeclination(point.latitude, point.longitude) }
    val heading = headingState?.let { trueHeading(it.azimuth, declination) }
    val aligned = isAlignedToQibla(bearing, heading)
    val directions = stringArrayResource(R.array.compass_names)
    val dirName = directions[(((bearing + 22.5) % 360) / 45).toInt().coerceIn(0, 7)]

    val view = LocalView.current
    LaunchedEffect(aligned) {
        if (aligned) view.performHapticFeedback(if (android.os.Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY)
    }

    Column(
        Modifier.fillMaxSize().background(t.bg).verticalScroll(rememberScrollState()).statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DText(stringResource(R.string.qibla_title), 24.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            Row(
                Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(t.surface).clickable(onClick = onOpenLocation).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = t.strong, modifier = Modifier.size(16.dp))
                DText(location.city.ifBlank { location.displayName }, 13.sp, FontWeight.SemiBold, t.strong, maxLines = 1)
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            QiblaDial(heading = heading ?: 0f, qibla = bearing.toFloat(), modifier = Modifier.size(width = 320.dp, height = 340.dp))
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                Modifier.clip(RoundedCornerShape(22.dp)).background(if (aligned) t.chip else t.surface).padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(if (aligned) Icons.Rounded.CheckCircle else Icons.Rounded.Explore, null, tint = t.chipText, modifier = Modifier.size(18.dp))
                DText(
                    stringResource(when { headingState == null -> R.string.qibla_no_sensor_short; aligned -> R.string.qibla_aligned; else -> R.string.qibla_rotate_hint }),
                    14.sp, FontWeight.Bold, t.chipText,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DCard(Modifier.weight(1f), radius = 18.dp, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp), spacing = 4.dp) {
                DText(stringResource(R.string.qibla_direction), 12.sp, FontWeight.SemiBold, t.muted)
                DText("${bearing.toInt()}°", 24.sp, FontWeight.ExtraBold, t.strong)
                DText(dirName, 12.sp, FontWeight.Normal, t.muted)
            }
            DCard(Modifier.weight(1f), radius = 18.dp, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp), spacing = 4.dp) {
                DText(stringResource(R.string.qibla_distance), 12.sp, FontWeight.SemiBold, t.muted)
                DText("${NumberFormat.getIntegerInstance(Locale.getDefault()).format(distance.toLong())} km", 24.sp, FontWeight.ExtraBold, t.strong)
                DText(stringResource(R.string.qibla_straight_line), 12.sp, FontWeight.Normal, t.muted)
            }
        }
        DHint(Icons.Outlined.Info, stringResource(R.string.qibla_calibrate_hint))
        Spacer(Modifier.height(80.dp))
    }
}

/**
 * Oq siferblat: N/E/S/W telefon bilan aylanadi, Ka'ba belgisi esa qibla burchagida.
 * Qiblaga to'g'ri qaralganda Ka'ba tepadagi oltin belgi ostiga keladi.
 */
@Composable
private fun QiblaDial(heading: Float, qibla: Float, modifier: Modifier) {
    val t = NamozlyThemeTokens.t
    val rot = remember { Animatable(0f) }
    LaunchedEffect(heading) {
        var delta = (-heading - rot.value) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        // Sensor allaqachon silliqlangan: kichik o'zgarish — darhol, katta — qisqa animatsiya.
        if (kotlin.math.abs(delta) < 4f) rot.snapTo(rot.value + delta)
        else rot.animateTo(rot.value + delta, tween(110, easing = LinearEasing))
    }
    val labels = listOf("N", "E", "S", "W")
    val dialColor = t.surface
    val ring = if (t.isDark) t.line else Color(0xFFE4DCC9)
    val dash = if (t.isDark) t.line else Color(0xFFEFE8D9)
    val tick = Color(0xFF9AA096)
    val labelColor = if (t.isDark) t.muted else Color(0xFF45493F)
    Canvas(modifier) {
        val k = minOf(size.width / 320f, size.height / 340f)
        translate(size.width / 2, size.height / 2 + 10f * k) {
            scale(k, k, pivot = Offset.Zero) {
                drawPath(Path().apply { moveTo(0f, -176f); lineTo(-10f, -158f); lineTo(10f, -158f); close() }, Color(0xFFC9973F))
                drawCircle(dialColor, 148f, Offset.Zero)
                drawCircle(ring, 148f, Offset.Zero, style = Stroke(2f))
                drawCircle(dash, 98f, Offset.Zero, style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))))
                rotate(rot.value, Offset.Zero) {
                    for (i in 0 until 12) rotate(i * 30f, Offset.Zero) {
                        drawLine(tick, Offset(0f, -140f), Offset(0f, if (i % 3 == 0) -128f else -132f), strokeWidth = 2f, cap = StrokeCap.Round)
                    }
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER; textSize = 16f; isFakeBoldText = true
                    }
                    labels.forEachIndexed { i, l ->
                        val a = Math.toRadians(i * 90.0)
                        val p = Offset((sin(a) * 112).toFloat(), (-cos(a) * 112).toFloat())
                        paint.color = if (l == "N") Color(0xFFB3261E).toArgb() else labelColor.toArgb()
                        drawContext.canvas.nativeCanvas.drawText(l, p.x, p.y + 6f, paint)
                    }
                    rotate(qibla, Offset.Zero) {
                        drawPath(Path().apply { moveTo(0f, -92f); lineTo(14f, -10f); lineTo(0f, -22f); lineTo(-14f, -10f); close() }, Color(0xFFC9973F))
                        drawPath(Path().apply { moveTo(0f, 92f); lineTo(14f, 10f); lineTo(0f, 22f); lineTo(-14f, 10f); close() }, Color(0xFFD9D2C1))
                        translate(0f, -122f) {
                            rotate(-qibla - rot.value, Offset.Zero) {
                                drawRoundRect(Color(0xFF0A3D33), Offset(-13f, -13f), Size(26f, 26f), CornerRadius(3f))
                                drawRect(Color(0xFFC9973F), Offset(-13f, -6f), Size(26f, 4f))
                            }
                        }
                    }
                }
                drawCircle(Color(0xFF0A3D33), 10f, Offset.Zero)
                drawCircle(Color(0xFFF6F1E6), 4f, Offset.Zero)
            }
        }
    }
}
