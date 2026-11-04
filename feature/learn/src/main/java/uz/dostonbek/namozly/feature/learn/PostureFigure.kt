package uz.dostonbek.namozly.feature.learn

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import uz.dostonbek.namozly.core.model.Posture

/** Namoz holatlarining soddalashtirilgan illyustratsiyasi (yon tomondan, qibla — o'ngda). */
@Composable
internal fun PostureFigure(posture: Posture, color: Color, modifier: Modifier, accent: Color = Color(0xFFD9A94F), female: Boolean = false) {
    Canvas(modifier) {
        val k = minOf(size.width, size.height) / 100f
        translate((size.width - 100f * k) / 2f, (size.height - 100f * k) / 2f) {
            scale(k, k, pivot = Offset.Zero) {
                if (!female || !drawFemalePosture(posture, color, accent)) drawPosture(posture, color, accent)
            }
        }
    }
}

// ─────────────── Ayol figurasi: ro'mol, uzun keng libos, ayollarga xos holatlar ───────────────

/** Yopiq ko'pburchak: [xy] — ketma-ket x, y koordinatalar. */
private fun DrawScope.fill(color: Color, vararg xy: Float) {
    val p = Path().apply {
        moveTo(xy[0], xy[1])
        var i = 2
        while (i + 1 < xy.size) { lineTo(xy[i], xy[i + 1]); i += 2 }
        close()
    }
    drawPath(p, color)
    drawPath(p, color, style = Stroke(3f, join = StrokeJoin.Round))
}

/** Ro'molli bosh: yuz oval + yelkagacha tushgan ro'mol. */
private fun DrawScope.hijabHead(c: Color, x: Float, y: Float, faceRight: Boolean = true) {
    val dx = if (faceRight) 1f else -1f
    fill(c.copy(alpha = 0.55f), x - 10f * dx, y - 2f, x - 6f * dx, y - 9f, x + 3f * dx, y - 10f, x + 9f * dx, y - 4f, x + 8f * dx, y + 8f, x + 12f * dx, y + 16f, x - 12f * dx, y + 16f, x - 11f * dx, y + 6f)
    drawCircle(c, 6f, Offset(x + 1.5f * dx, y))
}

/** @return true agar ayol uchun alohida chizilgan bo'lsa. */
private fun DrawScope.drawFemalePosture(p: Posture, c: Color, accent: Color): Boolean {
    val dress = c.copy(alpha = 0.55f)
    when (p) {
        Posture.NIYAT, Posture.QIYAM -> {
            rug(accent); hijabHead(c, 50f, 16f)
            fill(dress, 44f, 30f, 56f, 30f, 62f, 92f, 38f, 92f)
            // Qo'llar ko'krak ustida
            limb(c, 45f to 34f, 52f to 42f, 57f to 40f); limb(c, 55f to 34f, 56f to 40f)
        }
        Posture.TAKBIR -> {
            rug(accent); hijabHead(c, 50f, 16f)
            fill(dress, 44f, 30f, 56f, 30f, 62f, 92f, 38f, 92f)
            // Qo'llar yelka baravarida (quloqqacha emas)
            limb(c, 45f to 34f, 39f to 36f, 40f to 27f); limb(c, 55f to 34f, 61f to 36f, 60f to 27f)
        }
        Posture.QUNUT -> {
            rug(accent); hijabHead(c, 50f, 16f)
            fill(dress, 44f, 30f, 56f, 30f, 62f, 92f, 38f, 92f)
            limb(c, 55f to 34f, 62f to 38f, 62f to 28f)
        }
        Posture.QAVMA -> {
            rug(accent); hijabHead(c, 50f, 16f)
            fill(dress, 44f, 30f, 56f, 30f, 62f, 92f, 38f, 92f)
            limb(c, 45f to 34f, 46f to 56f); limb(c, 55f to 34f, 54f to 56f)
        }
        Posture.RUKU -> {
            // Biroz egilgan: bel to'liq tekis emas, tizzalar sal bukilgan
            rug(accent); hijabHead(c, 74f, 38f)
            fill(dress, 64f, 44f, 70f, 52f, 46f, 64f, 48f, 92f, 34f, 92f, 36f, 58f)
            limb(c, 64f to 50f, 52f to 66f)
        }
        Posture.SAJDA -> {
            // Jipslashgan sajda: qorin sonlarda, bilaklar yerda, tirsaklar biqinda
            rug(accent)
            fill(dress, 26f, 92f, 30f, 74f, 44f, 70f, 62f, 78f, 66f, 92f)
            hijabHead(c, 72f, 78f)
            limb(c, 54f to 82f, 62f to 91f, 78f to 91f)
        }
        Posture.QADA, Posture.SALAM -> {
            // Tavarruk: yerga o'tirib, oyoqlar o'ng tomonga chiqarilgan
            rug(accent); hijabHead(c, if (p == Posture.SALAM) 44f else 40f, 30f, faceRight = true)
            fill(dress, 34f, 44f, 46f, 44f, 50f, 80f, 74f, 84f, 76f, 92f, 26f, 92f, 30f, 80f)
            limb(c, 44f to 50f, 50f to 70f, 58f to 74f)
        }
        else -> return false
    }
    return true
}

private fun DrawScope.limb(color: Color, vararg pts: Pair<Float, Float>) {
    val p = Path().apply {
        moveTo(pts[0].first, pts[0].second)
        for (i in 1 until pts.size) lineTo(pts[i].first, pts[i].second)
    }
    drawPath(p, color, style = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.head(color: Color, x: Float, y: Float) = drawCircle(color, 7.5f, Offset(x, y))

private fun DrawScope.rug(accent: Color) =
    drawRoundRect(accent.copy(alpha = 0.55f), Offset(12f, 94f), Size(76f, 4f), androidx.compose.ui.geometry.CornerRadius(2f))

private fun DrawScope.drop(color: Color, x: Float, y: Float, r: Float) {
    val p = Path().apply {
        moveTo(x, y - r * 1.6f)
        cubicTo(x + r, y - r * 0.4f, x + r, y + r, x, y + r)
        cubicTo(x - r, y + r, x - r, y - r * 0.4f, x, y - r * 1.6f)
        close()
    }
    drawPath(p, color)
}

private fun DrawScope.drawPosture(p: Posture, c: Color, accent: Color) {
    when (p) {
        Posture.NIYAT, Posture.QIYAM -> {
            rug(accent); head(c, 50f, 16f)
            limb(c, 50f to 27f, 50f to 60f); limb(c, 50f to 60f, 47f to 92f); limb(c, 50f to 60f, 54f to 92f)
            limb(c, 50f to 34f, 58f to 46f, 50f to 50f)
        }
        Posture.TAKBIR -> {
            rug(accent); head(c, 50f, 16f)
            limb(c, 50f to 27f, 50f to 60f); limb(c, 50f to 60f, 47f to 92f); limb(c, 50f to 60f, 54f to 92f)
            limb(c, 50f to 33f, 40f to 30f, 41f to 16f); limb(c, 50f to 33f, 60f to 30f, 59f to 16f)
        }
        Posture.QUNUT -> {
            rug(accent); head(c, 50f, 16f)
            limb(c, 50f to 27f, 50f to 60f); limb(c, 50f to 60f, 47f to 92f); limb(c, 50f to 60f, 54f to 92f)
            limb(c, 50f to 33f, 60f to 38f, 62f to 26f)
        }
        Posture.QAVMA -> {
            rug(accent); head(c, 50f, 16f)
            limb(c, 50f to 27f, 50f to 60f); limb(c, 50f to 60f, 47f to 92f); limb(c, 50f to 60f, 54f to 92f)
            limb(c, 50f to 33f, 52f to 58f)
        }
        Posture.RUKU -> {
            rug(accent); head(c, 80f, 52f)
            limb(c, 38f to 56f, 71f to 54f); limb(c, 38f to 56f, 36f to 92f); limb(c, 38f to 56f, 42f to 92f)
            limb(c, 62f to 56f, 44f to 74f)
        }
        Posture.SAJDA -> {
            rug(accent); head(c, 78f, 86f)
            limb(c, 22f to 91f, 42f to 91f, 38f to 66f, 70f to 82f); limb(c, 62f to 76f, 70f to 91f)
        }
        Posture.QADA, Posture.SALAM -> {
            rug(accent); head(c, if (p == Posture.SALAM) 46f else 42f, 30f)
            limb(c, 26f to 91f, 58f to 91f); limb(c, 30f to 88f, 34f to 74f, 40f to 41f)
            limb(c, 39f to 50f, 52f to 72f, 60f to 76f)
        }
        Posture.WUDU_HANDS, Posture.WUDU_ARMS -> {
            limb(c, 22f to 70f, 50f to 54f); limb(c, 78f to 70f, 50f to 54f)
            drop(accent, 50f, 30f, 7f); drop(accent.copy(alpha = 0.7f), 38f, 40f, 4f); drop(accent.copy(alpha = 0.7f), 62f, 40f, 4f)
        }
        Posture.WUDU_MOUTH, Posture.WUDU_FACE -> {
            drawCircle(c, 24f, Offset(50f, 52f), style = Stroke(7f))
            drop(accent, 50f, 20f, 6f); drop(accent.copy(alpha = 0.7f), 30f, 30f, 4f); drop(accent.copy(alpha = 0.7f), 70f, 30f, 4f)
        }
        Posture.WUDU_HEAD -> {
            drawCircle(c, 22f, Offset(50f, 58f), style = Stroke(7f))
            limb(c, 26f to 40f, 50f to 30f, 74f to 40f)
            drop(accent, 50f, 16f, 5f)
        }
        Posture.WUDU_FEET -> {
            limb(c, 30f to 30f, 30f to 72f, 52f to 74f); limb(c, 62f to 30f, 62f to 72f, 84f to 74f)
            drop(accent, 44f, 56f, 5f); drop(accent, 76f, 56f, 5f)
        }
        Posture.GHUSL -> {
            head(c, 50f, 34f); limb(c, 50f to 45f, 50f to 78f); limb(c, 50f to 52f, 36f to 64f); limb(c, 50f to 52f, 64f to 64f)
            for (i in 0 until 5) drop(accent, 30f + i * 10f, 14f + (i % 2) * 6f, 3.5f)
        }
        Posture.TAYAMMUM -> {
            drawRoundRect(accent.copy(alpha = 0.45f), Offset(18f, 72f), Size(64f, 16f), androidx.compose.ui.geometry.CornerRadius(4f))
            limb(c, 30f to 70f, 40f to 50f); limb(c, 70f to 70f, 60f to 50f)
        }
        Posture.BOOK -> {
            limb(c, 50f to 32f, 50f to 78f)
            limb(c, 50f to 32f, 22f to 28f, 22f to 74f, 50f to 78f); limb(c, 50f to 32f, 78f to 28f, 78f to 74f, 50f to 78f)
            limb(accent, 30f to 42f, 42f to 44f); limb(accent, 58f to 44f, 70f to 42f)
        }
    }
}
