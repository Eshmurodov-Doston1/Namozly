package uz.dostonbek.namozly.feature.tasbih.style

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AccentGoldLight
import uz.dostonbek.namozly.core.designsystem.theme.EmeraldDark
import uz.dostonbek.namozly.core.designsystem.theme.EmeraldLight
import uz.dostonbek.namozly.core.designsystem.theme.EmeraldPrimary
import uz.dostonbek.namozly.core.designsystem.theme.GoldOchre
import uz.dostonbek.namozly.core.model.TasbihStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Barcha uslublar uchun umumiy kirish. [count] — umumiy son, [inRound] — joriy aylanadagi son. */
@Composable
fun TasbihCounter(
    style: TasbihStyle,
    count: Int,
    inRound: Int,
    target: Int,
    progress: Float,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (style) {
        TasbihStyle.CLASSIC -> ClassicCounter(count, inRound, target, progress, onTap, modifier)
        TasbihStyle.MINIMAL -> MinimalCounter(count, inRound, target, progress, onTap, modifier)
        TasbihStyle.BEADS -> BeadsCounter(count, inRound, target, onTap, modifier, crescent = false)
        TasbihStyle.CRESCENT -> BeadsCounter(count, inRound, target, onTap, modifier, crescent = true)
        TasbihStyle.DIGITAL -> DigitalCounter(count, inRound, target, onTap, modifier)
    }
}

private fun Modifier.tapArea(onTap: () -> Unit) = clickable(
    interactionSource = MutableInteractionSource(),
    indication = null,
    onClick = onTap,
)

/** Bosilganda "puls" — har bir sanoqda kichik siqilish. */
@Composable
private fun rememberPulse(key: Int): Float {
    val anim = remember { Animatable(1f) }
    LaunchedEffect(key) {
        if (key == 0) return@LaunchedEffect
        anim.snapTo(0.94f)
        anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    return anim.value
}

@Composable
private fun ClassicCounter(count: Int, inRound: Int, target: Int, progress: Float, onTap: () -> Unit, modifier: Modifier) {
    val pulse = rememberPulse(count)
    val animatedProgress by animateFloatAsState(progress, tween(250, easing = FastOutSlowInEasing), label = "p")
    val beadShift = remember { Animatable(0f) }
    LaunchedEffect(count) { beadShift.animateTo(-count * 9f, tween(260, easing = FastOutSlowInEasing)) }
    val breathe = rememberInfiniteTransition(label = "br")
    val glow by breathe.animateFloat(0.55f, 0.95f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "g")
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(270.dp).scale(pulse).tapArea(onTap), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                val k = size.minDimension / 270f
                drawCircle(Brush.radialGradient(listOf(Color(0xFF1F6E5A).copy(alpha = glow), Color.Transparent), c, 150f * k), 150f * k, c)
                drawCircle(Color(0xFFF6F1E6), 117f * k, c)
                drawCircle(Color(0xFFF6F1E6).copy(alpha = 0.12f), 126f * k, c, style = Stroke(10f * k))
                drawArc(
                    Brush.sweepGradient(listOf(AccentGoldLight, AccentGold, GoldOchre, AccentGoldLight)),
                    -90f, 360f * animatedProgress, false,
                    topLeft = Offset(c.x - 126f * k, c.y - 126f * k), size = androidx.compose.ui.geometry.Size(252f * k, 252f * k),
                    style = Stroke(10f * k, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(inRound.toString(), color = Color(0xFF0A3D33), fontSize = 84.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.04).em, lineHeight = 84.sp,
                    fontFamily = uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily)
                Text(if (target > 0) "/ $target" else "∞", color = Color(0xFF4E5A54), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    fontFamily = uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily)
            }
        }
        // Pastdagi tasbeh donalari yoyi — har sanoqda bir dona siljiydi
        Canvas(Modifier.fillMaxWidth().height(90.dp).tapArea(onTap)) {
            val k = size.width / 390f
            val cx = size.width / 2
            val cy = 440f * k + 20f * k
            for (i in 0 until 40) {
                val deg = i * 9f + beadShift.value
                val a = Math.toRadians(deg.toDouble())
                val x = cx + (440f * k * sin(a)).toFloat()
                val y = cy - (440f * k * cos(a)).toFloat()
                if (y > size.height + 20f * k) continue
                val big = i % 11 == 0
                val r = (if (big) 17f else 14f) * k
                drawCircle(Color.Black.copy(alpha = 0.2f), r, Offset(x, y + r * 0.25f))
                drawCircle(if (big) AccentGold else if (i % 2 == 1) Color(0xFF2F8A71) else Color(0xFF3BA083), r, Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.35f), r * 0.3f, Offset(x - r * 0.35f, y - r * 0.35f))
            }
        }
    }
}

@Composable
private fun MinimalCounter(count: Int, inRound: Int, target: Int, progress: Float, onTap: () -> Unit, modifier: Modifier) {
    val pulse = rememberPulse(count)
    val animatedProgress by animateFloatAsState(progress, label = "mp")
    Column(
        modifier.fillMaxWidth().height(380.dp).clip(RoundedCornerShape(32.dp)).tapArea(onTap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            inRound.toString(),
            fontSize = 120.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.scale(pulse),
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(0.6f).height(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline)) {
            Box(Modifier.fillMaxWidth(animatedProgress).height(3.dp).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (target > 0) "$count · /$target" else count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Ip (BEADS) — ellips bo'ylab 33 dona; Hilol (CRESCENT) — yarim oy shaklidagi yoy bo'ylab katta donalar.
 * Har bosishda donalar silliq siljiydi, joriy dona oltin rangda.
 */
@Composable
private fun BeadsCounter(count: Int, inRound: Int, target: Int, onTap: () -> Unit, modifier: Modifier, crescent: Boolean) {
    val beadCount = if (crescent) 19 else 33
    val shift = remember { Animatable(0f) }
    LaunchedEffect(count) {
        shift.animateTo(count.toFloat(), tween(260, easing = FastOutSlowInEasing))
    }
    Box(modifier.fillMaxWidth().aspectRatio(0.95f).tapArea(onTap), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val c = Offset(w / 2, h / 2)
            val beadR = if (crescent) w * 0.045f else w * 0.033f

            fun point(t: Float): Offset = if (crescent) {
                // Hilol: 300° yoy, ichki "kesik" o'ng tomonda
                val start = PI * 0.25
                val angle = start + t * PI * 1.5
                Offset(c.x + (cos(angle) * w * 0.38f).toFloat(), c.y + (sin(angle) * h * 0.38f).toFloat())
            } else {
                val angle = -PI / 2 + t * 2 * PI
                Offset(c.x + (cos(angle) * w * 0.40f).toFloat(), c.y + (sin(angle) * h * 0.36f).toFloat())
            }

            // Ip
            val steps = 120
            for (i in 0 until steps) {
                val a = point(i / steps.toFloat())
                val b = point((i + 1) / steps.toFloat())
                if (crescent && i == steps - 1) break
                drawLine(GoldOchre.copy(alpha = 0.7f), a, b, strokeWidth = 3f)
            }
            if (crescent) drawCrescentMoon(c, w)

            val frac = shift.value - shift.value.toInt()
            for (i in 0 until beadCount) {
                val t = ((i + frac) / beadCount) % 1f
                val p = point(t)
                val isCurrent = i == 0
                drawBead(p, if (isCurrent) beadR * 1.25f else beadR, highlight = isCurrent)
            }
            // Imom donasi (tassel)
            val top = point(0f)
            drawCircle(GoldOchre, beadR * 0.9f, Offset(top.x, top.y - beadR * 2.6f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(inRound.toString(), fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF6F1E6), fontFamily = uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily)
            Text(if (target > 0) "/ $target" else "∞", style = MaterialTheme.typography.titleMedium, color = AccentGold)
        }
    }
}

private fun DrawScope.drawBead(p: Offset, r: Float, highlight: Boolean) {
    drawCircle(Color.Black.copy(alpha = 0.18f), r, p + Offset(0f, r * 0.25f))
    drawCircle(
        Brush.radialGradient(
            if (highlight) listOf(AccentGoldLight, AccentGold, GoldOchre) else listOf(EmeraldLight, EmeraldPrimary, EmeraldDark),
            center = p + Offset(-r * 0.35f, -r * 0.35f),
            radius = r * 1.6f,
        ),
        r, p,
    )
    drawCircle(Color.White.copy(alpha = 0.45f), r * 0.25f, p + Offset(-r * 0.35f, -r * 0.4f))
}

private fun DrawScope.drawCrescentMoon(c: Offset, w: Float) {
    val outer = Path().apply { addOval(Rect(c, w * 0.34f)) }
    val inner = Path().apply { addOval(Rect(c + Offset(w * 0.15f, -w * 0.05f), w * 0.29f)) }
    val crescent = Path.combine(PathOperation.Difference, outer, inner)
    drawPath(crescent, Brush.linearGradient(listOf(AccentGoldLight.copy(alpha = 0.30f), AccentGold.copy(alpha = 0.10f))))
}

/** Raqamli (elektron tasbeh qurilmasi) — LCD ekran + katta tugma. */
@Composable
private fun DigitalCounter(count: Int, inRound: Int, target: Int, onTap: () -> Unit, modifier: Modifier) {
    val pulse = rememberPulse(count)
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 120.dp, bottomEnd = 120.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2B3A36), Color(0xFF141E1B))))
            .border(2.dp, Color(0xFF3E504B), RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 120.dp, bottomEnd = 120.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFB9C9A0))
                .border(3.dp, Color(0xFF0E1412), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (target > 0) "TGT ${target.toString().padStart(4, '0')}" else "TGT ----",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2E3A24).copy(alpha = 0.8f),
                    fontSize = 12.sp,
                )
                Text(
                    inRound.toString().padStart(4, '0'),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp,
                    color = Color(0xFF1C2416),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Σ $count", fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .size(120.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF5E6D68), Color(0xFF26312E))))
                .border(4.dp, Color(0xFF0E1412), CircleShape)
                .tapArea(onTap),
        )
    }
}
