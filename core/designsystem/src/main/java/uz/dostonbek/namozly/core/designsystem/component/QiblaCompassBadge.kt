package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dizayndagi (NavBar.dc.html) qibla kompasi — 100×100 koordinatada chiziladi va o'lchamga moslanadi.
 * Siferblat telefon bilan aylanadi (qizil nuqta — shimol), Ka'ba belgisi qibla burchagida turadi;
 * oltin igna doim tepaga qaraydi. Ka'ba tepaga kelganda yashil halqa yonadi.
 *
 * @param heading qurilmaning haqiqiy shimolga nisbatan yo'nalishi (sensor bo'lmasa null)
 * @param qiblaBearing qibla burchagi (shimoldan soat yo'nalishida)
 */
@Composable
fun QiblaCompassBadge(
    heading: () -> Float?,
    qiblaBearing: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
) {
    val dial = remember { Animatable(0f) }
    // Yo'nalish lambda orqali o'qiladi: sensor har o'zgarganda butun ekran emas, faqat shu belgi qayta chiziladi.
    LaunchedEffect(Unit) {
        snapshotFlow { heading() }.collectLatest { h ->
            val target = -(h ?: 0f)
            var delta = (target - dial.value) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            if (kotlin.math.abs(delta) < 4f) dial.snapTo(dial.value + delta)
            else dial.animateTo(dial.value + delta, tween(110, easing = LinearEasing))
        }
    }
    val okAlpha by animateFloatAsState(if (aligned) 1f else 0f, tween(400), label = "ok")

    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 100f
        scale(k, k, pivot = Offset.Zero) {
            drawCompass(dialRotation = dial.value, qibla = qiblaBearing, okAlpha = okAlpha)
        }
    }
}

private val P = { x: Float, y: Float -> Offset(x, y) }

private fun DrawScope.drawCompass(dialRotation: Float, qibla: Float, okAlpha: Float) {
    val c = Offset(50f, 50f)
    // Oltin gardish
    drawCircle(
        Brush.radialGradient(
            0f to Color(0xFFFFF4D2), 0.38f to Color(0xFFE2B45B), 0.8f to Color(0xFF93691F), 1f to Color(0xFF5E420F),
            center = Offset(30f, 22f), radius = 90f,
        ),
        radius = 50f, center = c,
    )
    // Zumrad yuz
    drawCircle(
        Brush.radialGradient(
            0f to Color(0xFF2C9478), 0.55f to Color(0xFF0F5A49), 1f to Color(0xFF062A22),
            center = Offset(42f, 30f), radius = 80f,
        ),
        radius = 44.5f, center = c,
    )
    // Islomiy yulduz naqshi
    val pattern = Color(0xFFE2B45B).copy(alpha = 0.32f)
    drawRect(pattern, topLeft = P(29f, 29f), size = Size(42f, 42f), style = Stroke(1.6f))
    rotate(45f, c) { drawRect(pattern, topLeft = P(29f, 29f), size = Size(42f, 42f), style = Stroke(1.6f)) }
    drawCircle(pattern, radius = 13f, center = c, style = Stroke(1.6f))

    // Aylanuvchi siferblat
    rotate(dialRotation, c) {
        val tick = Color(0xFFF6E7C4).copy(alpha = 0.6f)
        for (i in 0 until 30) {
            val a = Math.toRadians(i * 12.0)
            val p1 = c + Offset((sin(a) * 37.6).toFloat(), (-cos(a) * 37.6).toFloat())
            val p2 = c + Offset((sin(a) * 40.4).toFloat(), (-cos(a) * 40.4).toFloat())
            drawLine(tick, p1, p2, strokeWidth = 0.9f)
        }
        drawCircle(Color(0xFFFF8A80), radius = 2.8f, center = P(50f, 12.5f))
        withTransform({ rotate(qibla, c) }) {
            val kc = P(50f, 13f) // qibla tomonda, markazdan 37 birlik
            drawCircle(
                Brush.radialGradient(listOf(Color(0xF2F6D58A), Color(0x00F6D58A)), center = kc, radius = 11f),
                radius = 11f, center = kc,
            )
            rotate(-qibla - dialRotation, kc) {
                drawRoundRect(Color(0xFF0A2E26), topLeft = kc - Offset(4.5f, 4.5f), size = Size(9f, 9f), cornerRadius = CornerRadius(1.5f))
                drawRoundRect(Color(0xFFF6D58A), topLeft = kc - Offset(4.5f, 4.5f), size = Size(9f, 9f), cornerRadius = CornerRadius(1.5f), style = Stroke(0.9f))
                drawRect(Color(0xFFE2B45B), topLeft = kc + Offset(-4.5f, -2.2f), size = Size(9f, 1.8f))
            }
        }
    }

    // Qo'zg'almas igna
    drawPath(Path().apply { moveTo(47f, 52f); lineTo(50f, 70f); lineTo(53f, 52f); close() }, Color(0xBFCFC4A8))
    val needle = Path().apply {
        moveTo(50f, 20f)
        cubicTo(52.4f, 24f, 53.4f, 28f, 53.4f, 33f)
        lineTo(53.4f, 52f); lineTo(46.6f, 52f); lineTo(46.6f, 33f)
        cubicTo(46.6f, 28f, 47.6f, 24f, 50f, 20f)
        close()
    }
    drawPath(needle, Brush.horizontalGradient(0f to Color(0xFFFFF1C9), 0.5f to Color(0xFFE2B45B), 1f to Color(0xFF8A6420), startX = 46.6f, endX = 53.4f))
    drawRoundRect(Color(0xFFFFF1C9), topLeft = P(44.6f, 36f), size = Size(10.8f, 2.6f), cornerRadius = CornerRadius(1.3f))
    // Igna uchidagi hilol
    drawCircle(Color(0xFFF6D58A), radius = 3.4f, center = P(50f, 14.5f))
    drawCircle(Color(0xFF145F4D), radius = 2.9f, center = P(51.5f, 13.4f))
    // Markaz
    drawCircle(Color(0xFF0A2E26), radius = 5f, center = P(50f, 52f))
    drawCircle(Color(0xFFE2B45B), radius = 5f, center = P(50f, 52f), style = Stroke(1.6f))
    // Qiblaga to'g'ri — yashil halqa
    if (okAlpha > 0f) drawCircle(Color(0xFF5EE0B0).copy(alpha = okAlpha), radius = 44.5f, center = c, style = Stroke(3.4f))
    // Tepadagi oq belgi
    drawPath(Path().apply { moveTo(50f, 0.8f); lineTo(54f, 7.5f); lineTo(46f, 7.5f); close() }, Color.White)
    // Shisha yaltirashi
    val glass = Path().apply {
        moveTo(16f, 38f)
        arcTo(Rect(Offset(50f, 49.83f), 36f), 199.2f, 141.6f, false)
        arcTo(Rect(left = 6f, top = 27.8f, right = 94f, bottom = 83.8f), -39.4f, -101.2f, false)
        close()
    }
    drawPath(glass, Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x00FFFFFF)), startY = 14f, endY = 38f))
}
