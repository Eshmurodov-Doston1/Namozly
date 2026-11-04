package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

/** Muqaddas joylar illyustratsiyasi (jonli efir kartalari uchun). */
enum class HolyPlace { MAKKAH, MADINAH, QUDS }

@Composable
fun HolyScene(place: HolyPlace, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = 360f; val h = 202f
        val k = maxOf(size.width / w, size.height / h)
        val dx = (size.width - w * k) / 2f
        val dy = (size.height - h * k) / 2f
        drawContext.transform.translate(dx, dy)
        scale(k, k, pivot = Offset.Zero) {
            when (place) {
                HolyPlace.MAKKAH -> drawMakkah()
                HolyPlace.MADINAH -> drawMadinah()
                HolyPlace.QUDS -> drawQuds()
            }
        }
        drawContext.transform.translate(-dx, -dy)
    }
}

private fun DrawScope.sky(top: Color) {
    drawRect(top, size = Size(360f, 202f))
    listOf(30f to 18f, 120f to 12f, 230f to 26f, 340f to 10f, 180f to 30f).forEach { (x, y) -> drawCircle(Color(0xB3F6F1E6), 1.2f, Offset(x, y)) }
}

private fun DrawScope.minaret(x: Float, top: Float, base: Float, body: Color) {
    drawRect(body, Offset(x, top), Size(7f, base - top))
    drawPath(Path().apply { moveTo(x - 1, top + 1); lineTo(x + 3.5f, top - 10); lineTo(x + 8, top + 1); close() }, Color(0xFFD9A94F))
}

private fun DrawScope.drawMakkah() {
    sky(Color(0xFF0B1F2A))
    drawRect(Color(0xFF183242), Offset(266f, 14f), Size(30f, 110f))
    drawRect(Color(0xFF183242), Offset(274f, 4f), Size(14f, 12f))
    drawCircle(Color(0xE6F6E7C4), 10f, Offset(281f, 40f))
    drawRect(Color(0xFFD9A94F), Offset(280.3f, 0f), Size(1.5f, 5f))
    minaret(34f, 40f, 124f, Color(0xFF20404F)); minaret(72f, 52f, 124f, Color(0xFF20404F)); minaret(318f, 46f, 124f, Color(0xFF20404F))
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

private fun DrawScope.drawMadinah() {
    sky(Color(0xFF0E2433))
    minaret(40f, 30f, 140f, Color(0xFFE9E2D0)); minaret(312f, 30f, 140f, Color(0xFFE9E2D0)); minaret(120f, 50f, 140f, Color(0xFFE9E2D0))
    drawRect(Color(0xFFE9E2D0), Offset(0f, 110f), Size(360f, 50f))
    for (i in 0 until 14) drawRoundRect(Color(0x99C9973F), Offset(10f + i * 26f, 120f), Size(10f, 18f), CornerRadius(5f))
    // Yashil gumbaz
    drawRect(Color(0xFFDCD3BD), Offset(178f, 78f), Size(56f, 34f))
    drawPath(Path().apply { moveTo(180f, 80f); cubicTo(180f, 52f, 232f, 52f, 232f, 80f); close() }, Color(0xFF2E8B57))
    drawRect(Color(0xFFD9A94F), Offset(205f, 44f), Size(2f, 14f))
    drawCircle(Color(0xFFD9A94F), 3f, Offset(206f, 44f))
    drawRect(Color(0xFFCFC8B4), Offset(0f, 160f), Size(360f, 42f))
    for (i in 0 until 8) drawCircle(Color(0x33FFFFFF), 14f, Offset(22f + i * 46f, 176f))
}

private fun DrawScope.drawQuds() {
    sky(Color(0xFF1A2440))
    drawRect(Color(0xFF2C3A55), Offset(0f, 120f), Size(360f, 82f))
    // Sakhra (oltin gumbaz) sakkiz qirrali bino
    drawRect(Color(0xFF2E5E8C), Offset(130f, 100f), Size(100f, 46f))
    for (i in 0 until 6) drawRect(Color(0xFF5B8CC0), Offset(138f + i * 15f, 108f), Size(8f, 26f))
    drawRect(Color(0xFFE8E0C8), Offset(150f, 88f), Size(60f, 14f))
    drawPath(Path().apply { moveTo(152f, 90f); cubicTo(152f, 50f, 208f, 50f, 208f, 90f); close() }, Color(0xFFE2B45B))
    drawRect(Color(0xFFD9A94F), Offset(179f, 50f), Size(2f, 12f))
    // Aqso masjidining kumush gumbazi (uzoqda)
    drawRect(Color(0xFF3A4A66), Offset(268f, 120f), Size(70f, 26f))
    drawPath(Path().apply { moveTo(286f, 122f); cubicTo(286f, 104f, 320f, 104f, 320f, 122f); close() }, Color(0xFF8A94A6))
    minaret(52f, 70f, 146f, Color(0xFF3A4A66))
    drawRect(Color(0xFFBFB8A4), Offset(0f, 146f), Size(360f, 56f))
}
