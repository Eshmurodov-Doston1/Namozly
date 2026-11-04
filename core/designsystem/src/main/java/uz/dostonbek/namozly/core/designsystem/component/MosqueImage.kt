package uz.dostonbek.namozly.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import kotlin.math.abs

/** Masjid rasmi: URL bo'lsa yuklaydi, bo'lmasa (yoki xato bo'lsa) o'zimizning illyustratsiya. */
@Composable
fun MosqueImage(
    photoUrl: String?,
    seed: Long,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    var failed by remember(photoUrl) { mutableStateOf(false) }
    Box(modifier) {
        MosqueIllustration(seed = seed, modifier = Modifier.fillMaxSize())
        if (photoUrl != null && !failed) {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { failed = true },
            )
        }
    }
}

private val palettes = listOf(
    listOf(Color(0xFF0A3D33), Color(0xFF167B66), Color(0xFFF3C775)),
    listOf(Color(0xFF13294B), Color(0xFF3D6FA8), Color(0xFFF6C89F)),
    listOf(Color(0xFF3B1E3F), Color(0xFFB0587A), Color(0xFFFFD39B)),
    listOf(Color(0xFF1F3B2C), Color(0xFF7FA36B), Color(0xFFF5E6B3)),
)

/** Gumbaz + minoralar — rasm topilmaganda chiziladigan illyustratsiya. */
@Composable
fun MosqueIllustration(seed: Long, modifier: Modifier = Modifier) {
    val palette = palettes[(abs(seed) % palettes.size).toInt()]
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(palette[0], palette[1])))
        // quyosh/oy
        drawCircle(palette[2].copy(alpha = 0.9f), radius = size.minDimension * 0.08f, center = Offset(size.width * 0.8f, size.height * 0.22f))
        drawMosque(Color.Black.copy(alpha = 0.28f))
    }
}

private fun DrawScope.drawMosque(color: Color) {
    val w = size.width
    val h = size.height
    val base = h * 0.92f
    // asosiy bino
    drawRect(color, topLeft = Offset(w * 0.28f, h * 0.58f), size = Size(w * 0.44f, base - h * 0.58f))
    // gumbaz
    val dome = Path().apply {
        moveTo(w * 0.32f, h * 0.60f)
        cubicTo(w * 0.30f, h * 0.30f, w * 0.70f, h * 0.30f, w * 0.68f, h * 0.60f)
        close()
    }
    drawPath(dome, color)
    // gumbaz uchidagi hilol
    drawCircle(AccentGold, radius = w * 0.018f, center = Offset(w * 0.5f, h * 0.30f))
    drawLine(AccentGold, Offset(w * 0.5f, h * 0.31f), Offset(w * 0.5f, h * 0.37f), strokeWidth = w * 0.006f)
    // minoralar
    listOf(0.16f, 0.80f).forEach { x ->
        drawRect(color, topLeft = Offset(w * x, h * 0.30f), size = Size(w * 0.05f, base - h * 0.30f))
        val cap = Path().apply {
            moveTo(w * x - w * 0.005f, h * 0.30f)
            lineTo(w * (x + 0.025f), h * 0.20f)
            lineTo(w * (x + 0.055f), h * 0.30f)
            close()
        }
        drawPath(cap, color)
    }
    // eshik (ark)
    val door = Path().apply {
        moveTo(w * 0.45f, base)
        lineTo(w * 0.45f, h * 0.74f)
        quadraticTo(w * 0.5f, h * 0.66f, w * 0.55f, h * 0.74f)
        lineTo(w * 0.55f, base)
        close()
    }
    drawPath(door, palette0Dim)
    drawRect(color, topLeft = Offset(0f, base), size = Size(w, h - base))
}

private val palette0Dim = Color(0x55FFE9B0)
