package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Bosh sahifa hero kartasidagi animatsion osmon: kunduzi yoyi bo'ylab harakatlanuvchi quyosh
 * (nurlar aylanadi, porlaydi), kechasi — hilol oy va miltillovchi yulduzlar.
 *
 * @param isDay quyosh chiqqan va botmagan bo'lsa true
 * @param progress kun (yoki tun) qancha o'tgani 0..1 — yoydagi joylashuv
 */
@Composable
fun SunMoonSky(
    isDay: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "sky")
    val spin by t.animateFloat(0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "spin")
    val pulse by t.animateFloat(0.85f, 1.1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "pulse")
    val twinkle by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "twinkle")
    val stars = remember { List(18) { Triple(Random.nextFloat(), Random.nextFloat() * 0.75f, Random.nextFloat()) } }

    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val w = size.width
        val h = size.height
        val p = progress.coerceIn(0f, 1f)

        // Yoy (gorizontdan gorizontgacha)
        val arcRadius = w * 0.42f
        val arcCenter = Offset(w / 2f, h * 0.95f)
        drawArc(
            color = Color.White.copy(alpha = 0.22f),
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
            size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))),
        )
        val angle = PI * (1 - p)
        val body = Offset(
            arcCenter.x + (cos(angle) * arcRadius).toFloat(),
            arcCenter.y - (sin(angle) * arcRadius).toFloat(),
        )
        val bodyR = h * 0.13f

        if (isDay) {
            drawSun(body, bodyR, spin, pulse)
        } else {
            stars.forEach { (x, y, phase) ->
                val a = (0.25f + 0.75f * ((sin((twinkle + phase) * 2 * PI) + 1) / 2)).toFloat()
                drawCircle(Color.White.copy(alpha = a * 0.9f), radius = 1.6f + phase * 1.6f, center = Offset(x * w, y * h))
            }
            drawMoon(body, bodyR, pulse)
        }
    }
}

private fun DrawScope.drawSun(c: Offset, r: Float, spin: Float, pulse: Float) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0x66FFE08A), Color.Transparent), c, r * 3.2f * pulse),
        radius = r * 3.2f * pulse, center = c,
    )
    rotate(spin, c) {
        repeat(12) { i ->
            val a = i * (2 * PI / 12)
            val inner = r * 1.25f
            val outer = r * (if (i % 2 == 0) 1.85f else 1.6f)
            drawLine(
                Color(0xFFFFD36B).copy(alpha = 0.85f),
                c + Offset((cos(a) * inner).toFloat(), (sin(a) * inner).toFloat()),
                c + Offset((cos(a) * outer).toFloat(), (sin(a) * outer).toFloat()),
                strokeWidth = r * 0.16f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF4C2), Color(0xFFFFC94A), Color(0xFFF29F05)), c + Offset(-r * 0.3f, -r * 0.3f), r * 1.4f),
        radius = r, center = c,
    )
}

private fun DrawScope.drawMoon(c: Offset, r: Float, pulse: Float) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0x55E8F0FF), Color.Transparent), c, r * 2.6f * pulse),
        radius = r * 2.6f * pulse, center = c,
    )
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFFBEA), Color(0xFFE9DFC0)), c + Offset(-r * 0.2f, -r * 0.2f), r * 1.2f),
        radius = r, center = c,
    )
    // Hilol shakli uchun ichki doirani "kesib tashlaymiz"
    drawCircle(Color.Black, radius = r * 0.92f, center = c + Offset(r * 0.48f, -r * 0.18f), blendMode = BlendMode.DstOut)
}
