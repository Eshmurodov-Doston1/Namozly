package uz.dostonbek.namozly.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** SVG viewBox koordinatalarida chizish uchun: [w]×[h] maydonni Canvas o'lchamiga moslaydi. */
private fun DrawScope.viewBox(w: Float, h: Float, slice: Boolean = false, block: DrawScope.() -> Unit) {
    val sx = size.width / w
    val sy = size.height / h
    val k = if (slice) maxOf(sx, sy) else minOf(sx, sy)
    val dx = (size.width - w * k) / 2f
    val dy = (size.height - h * k) / 2f
    drawContext.transform.translate(dx, dy)
    scale(k, k, pivot = Offset.Zero) { block() }
    drawContext.transform.translate(-dx, -dy)
}

/** Namozly logotipi (gumbaz + hilol). */
@Composable
internal fun NamozlyLogo(modifier: Modifier) {
    Canvas(modifier) {
        viewBox(100f, 100f) {
            drawRoundRect(Color(0xFF0E5A4A), size = Size(100f, 100f), cornerRadius = CornerRadius(26f))
            val arch = Path().apply {
                moveTo(28f, 82f); lineTo(28f, 48f)
                cubicTo(28f, 33f, 40f, 24f, 50f, 16f)
                cubicTo(60f, 24f, 72f, 33f, 72f, 48f)
                lineTo(72f, 82f); close()
            }
            drawPath(arch, Color(0xFFF6F1E6))
            drawCircle(Color(0xFF0E5A4A), radius = 10f, center = Offset(52.6f, 52f))
            drawCircle(Color(0xFFF6F1E6), radius = 9.5f, center = Offset(60f, 52f))
            drawRect(Color(0xFFC9973F), topLeft = Offset(28f, 78f), size = Size(44f, 4f))
        }
    }
}

/** Hero fonidagi xira islomiy yulduzlar. */
internal fun DrawScope.drawHeroPattern() {
    val c = Color(0xFFF6F1E6).copy(alpha = 0.07f)
    val k = size.width / 390f
    listOf(Offset(300f, 70f), Offset(360f, 190f), Offset(220f, 230f)).forEach { o ->
        val center = Offset(o.x * k, o.y * k)
        val half = 40f * k
        drawRect(c, topLeft = center - Offset(half, half), size = Size(half * 2, half * 2), style = Stroke(1.5f * k))
        rotate(45f, center) { drawRect(c, topLeft = center - Offset(half, half), size = Size(half * 2, half * 2), style = Stroke(1.5f * k)) }
        drawCircle(c, radius = 22f * k, center = center, style = Stroke(1.5f * k))
    }
}

/**
 * Hero o'ng tomonidagi 132dp halqa: oltin progress + ichida animatsion quyosh (kunduz) yoki oy (tun).
 */
@Composable
internal fun SunMoonRing(progress: Float, isDay: Boolean, heroColor: Color, modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "sky")
    val spin by t.animateFloat(0f, 360f, infiniteRepeatable(tween(28_000, easing = LinearEasing)), label = "spin")
    val pulse by t.animateFloat(0.9f, 1.08f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "pulse")
    val breath by t.animateFloat(1f, 1.04f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "breath")
    val rock by t.animateFloat(-7f, 7f, infiniteRepeatable(tween(3500), RepeatMode.Reverse), label = "rock")
    val twinkle by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "tw")

    Canvas(modifier) {
        viewBox(132f, 132f) {
            val c = Offset(66f, 66f)
            drawCircle(Color(0xFFF6F1E6).copy(alpha = 0.14f), radius = 58f, center = c, style = Stroke(7f))
            drawArc(
                Color(0xFFD9A94F), startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false,
                topLeft = Offset(8f, 8f), size = Size(116f, 116f), style = Stroke(7f, cap = StrokeCap.Round),
            )
            if (isDay) {
                scale(pulse, pivot = c) {
                    drawCircle(Brush.radialGradient(0.3f to Color(0x8CFFD978), 1f to Color(0x00FFD978), center = c, radius = 48f), radius = 48f, center = c)
                }
                rotate(spin, c) {
                    for (i in 0 until 12) rotate(i * 30f, c) {
                        drawPath(Path().apply { moveTo(66f, 20f); lineTo(69.5f, 34f); lineTo(62.5f, 34f); close() }, Color(0xFFFFD978))
                    }
                    for (i in 0 until 12) rotate(15f + i * 30f, c) {
                        drawPath(Path().apply { moveTo(66f, 26f); lineTo(68f, 35f); lineTo(64f, 35f); close() }, Color(0xD9F7AE34))
                    }
                }
                scale(breath, pivot = c) {
                    drawCircle(
                        Brush.radialGradient(
                            0f to Color(0xFFFFFBEA), 0.4f to Color(0xFFFFE08A), 0.78f to Color(0xFFF7AE34), 1f to Color(0xFFE5851C),
                            center = Offset(61f, 58.6f), radius = 29.4f,
                        ),
                        radius = 21f, center = c,
                    )
                }
                drawOval(Color(0x73FFFFFF), topLeft = Offset(52f, 53f), size = Size(14f, 10f))
            } else {
                scale(pulse, pivot = c) {
                    drawCircle(Brush.radialGradient(0.35f to Color(0x59F6E7C4), 1f to Color(0x00F6E7C4), center = c, radius = 46f), radius = 46f, center = c)
                }
                rotate(rock, c) {
                    drawCircle(
                        Brush.radialGradient(0f to Color(0xFFFFFDF2), 0.6f to Color(0xFFF3E5BC), 1f to Color(0xFFCDB57A), center = Offset(57.6f, 57.6f), radius = 38f),
                        radius = 24f, center = c,
                    )
                    val crater = Color(0x73B79E63)
                    drawCircle(crater, 3.4f, Offset(54f, 72f)); drawCircle(crater, 2.2f, Offset(59f, 82f))
                    drawCircle(crater, 2.4f, Offset(50f, 60f)); drawCircle(crater, 1.6f, Offset(62f, 64f))
                    drawCircle(heroColor, 21f, Offset(78f, 57f))
                }
                fun star(cx: Float, cy: Float, r: Float, phase: Float) {
                    val a = 0.2f + 0.8f * (kotlin.math.sin((twinkle + phase) * 2 * Math.PI).toFloat() + 1f) / 2f
                    val p = Path().apply {
                        moveTo(cx, cy - r); lineTo(cx + r * 0.4f, cy - r * 0.4f); lineTo(cx + r, cy)
                        lineTo(cx + r * 0.4f, cy + r * 0.4f); lineTo(cx, cy + r); lineTo(cx - r * 0.4f, cy + r * 0.4f)
                        lineTo(cx - r, cy); lineTo(cx - r * 0.4f, cy - r * 0.4f); close()
                    }
                    drawPath(p, Color(0xFFF6E7C4).copy(alpha = a))
                }
                star(100f, 39f, 7f, 0f); star(94f, 101f, 5f, 0.35f); star(30f, 39f, 5f, 0.65f)
            }
        }
    }
}

/** "Namoz o'rganish" kartasidagi mehrob va sajdadagi odam. */
@Composable
internal fun MihrabIllustration(modifier: Modifier) {
    Canvas(modifier) {
        viewBox(120f, 140f) {
            drawPath(Path().apply {
                moveTo(10f, 140f); lineTo(10f, 66f); cubicTo(10f, 36f, 36f, 20f, 60f, 2f)
                cubicTo(84f, 20f, 110f, 36f, 110f, 66f); lineTo(110f, 140f); close()
            }, Color(0xFF0E5A4A))
            drawPath(Path().apply {
                moveTo(24f, 140f); lineTo(24f, 72f); cubicTo(24f, 50f, 42f, 37f, 60f, 24f)
                cubicTo(78f, 37f, 96f, 50f, 96f, 72f); lineTo(96f, 140f); close()
            }, Color(0xFF14705C))
            drawPath(Path().apply {
                moveTo(60f, 7f); lineTo(62.2f, 12.4f); lineTo(67.6f, 14.6f); lineTo(62.2f, 16.8f)
                lineTo(60f, 22.2f); lineTo(57.8f, 16.8f); lineTo(52.4f, 14.6f); lineTo(57.8f, 12.4f); close()
            }, Color(0xFFD9A94F))
            drawRoundRect(Color(0xFFD9A94F), topLeft = Offset(20f, 118f), size = Size(82f, 9f), cornerRadius = CornerRadius(3f))
            val body = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val cream = Color(0xFFF6F1E6)
            drawPath(Path().apply { moveTo(80f, 118f); lineTo(80f, 86f) }, cream, style = body)
            drawPath(Path().apply { moveTo(80f, 86f); lineTo(46f, 80f) }, cream, style = body)
            drawPath(Path().apply { moveTo(54f, 81f); lineTo(76f, 99f) }, cream, style = body)
            drawCircle(cream, 7f, Offset(38f, 79f))
        }
    }
}

/** Jonli efir kartasidagi kichik Ka'ba manzarasi. */
@Composable
internal fun KaabaThumb(modifier: Modifier) {
    Canvas(modifier) {
        viewBox(360f, 202f, slice = true) {
            drawRect(Color(0xFF0B1F2A), size = Size(360f, 202f))
            listOf(30f to 18f, 120f to 12f, 230f to 26f, 340f to 10f).forEach { (x, y) -> drawCircle(Color(0xB3F6F1E6), 1.2f, Offset(x, y)) }
            drawRect(Color(0xFF183242), Offset(266f, 14f), Size(30f, 110f))
            drawRect(Color(0xFF183242), Offset(274f, 4f), Size(14f, 12f))
            drawCircle(Color(0xE6F6E7C4), 10f, Offset(281f, 40f))
            drawRect(Color(0xFFD9A94F), Offset(280.3f, 0f), Size(1.5f, 5f))
            listOf(34f to 40f, 72f to 52f, 318f to 46f).forEach { (x, y) ->
                drawRect(Color(0xFF20404F), Offset(x, y), Size(7f, 124f - y))
                drawPath(Path().apply { moveTo(x - 1, y + 1); lineTo(x + 3.5f, y - 10); lineTo(x + 8, y + 1); close() }, Color(0xFFD9A94F))
            }
            drawRect(Color(0xFF22424F), Offset(0f, 112f), Size(360f, 34f))
            for (i in 0 until 14) drawRoundRect(Color(0x8CF3D98F), Offset(10f + i * 26f, 120f), Size(10f, 16f), CornerRadius(5f))
            drawRect(Color(0xFFD5D0C4), Offset(0f, 146f), Size(360f, 56f))
            val dash = Stroke(2.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f, 3.5f)))
            drawOval(Color(0xFF7E7A70), Offset(35f, 146f), Size(300f, 52f), style = dash)
            drawOval(Color(0xFF7E7A70), Offset(73f, 150f), Size(224f, 38f), style = dash)
            drawOval(Color(0x40000000), Offset(152f, 161f), Size(68f, 10f))
            drawPath(Path().apply { moveTo(164f, 126f); lineTo(178f, 120f); lineTo(214f, 120f); lineTo(200f, 126f); close() }, Color(0xFF2A2A2A))
            drawRect(Color(0xFF0E0E0E), Offset(164f, 126f), Size(36f, 38f))
            drawPath(Path().apply { moveTo(200f, 126f); lineTo(214f, 120f); lineTo(214f, 156f); lineTo(200f, 164f); close() }, Color(0xFF1C1C1C))
            drawRect(Color(0xFFD9A94F), Offset(164f, 134f), Size(36f, 4f))
            drawPath(Path().apply { moveTo(200f, 134f); lineTo(214f, 128f); lineTo(214f, 132f); lineTo(200f, 138f); close() }, Color(0xFFB88A35))
            drawRect(Color(0xFFC9973F), Offset(188f, 148f), Size(7f, 13f))
        }
    }
}

/** Tasbeh belgisi (dizayndagi 7 donali ikonka). */
@Composable
internal fun TasbihIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        viewBox(24f, 24f) {
            val s = Stroke(2f, cap = StrokeCap.Round)
            listOf(12f to 4.5f, 6.5f to 8f, 17.5f to 8f, 6.5f to 14f, 17.5f to 14f, 12f to 17f).forEach { (x, y) ->
                drawCircle(color, 2f, Offset(x, y), style = s)
            }
            drawLine(color, Offset(12f, 19f), Offset(12f, 22f), strokeWidth = 2f, cap = StrokeCap.Round)
        }
    }
}

/** Uzuq-uzuq chegara (belgilanmagan namoz tugmasi). */
internal fun Modifier.dashedBorder(color: Color, radius: Dp = 18.dp, width: Dp = 2.dp): Modifier = drawBehind {
    val w = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(w / 2, w / 2),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}
