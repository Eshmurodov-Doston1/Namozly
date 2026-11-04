package uz.dostonbek.namozly.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Dizayn tokenlari — Namozly dizayn fayllaridagi `t` obyekti bilan 1:1 (light / dark).
 */
@Immutable
data class NamozlyTokens(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    val strong: Color,
    val hero: Color,
    val icon: Color,
    val chip: Color,
    val chipText: Color,
    val primary: Color,
    val primarySoft: Color,
    val accent: Color,
    val dash: Color,
    val line: Color,
    val nav: Color,
    val navOff: Color,
    val goldSoft: Color,
    val goldText: Color,
    val goldStrong: Color,
    val isDark: Boolean,
) {
    // Eski komponentlar bilan moslik
    val gold: Color get() = AccentGold
    val heroStart: Color get() = hero
    val heroEnd: Color get() = hero
}

val LightTokens = NamozlyTokens(
    bg = Color(0xFFF6F1E6), surface = Color(0xFFFFFFFF), text = Color(0xFF1C2420), muted = Color(0xFF5E625C),
    faint = Color(0xFF9AA096), strong = Color(0xFF0A3D33), hero = Color(0xFF0E5A4A), icon = Color(0xFF0E5A4A),
    chip = Color(0xFFE3EEE8), chipText = Color(0xFF0A4D3E), primary = Color(0xFF0E5A4A), primarySoft = Color(0xFFDCEBE3),
    accent = Color(0xFF8A6420), dash = Color(0xFFC9C2B0), line = Color(0xFFECE6D8), nav = Color(0xFFFFFFFF),
    navOff = Color(0xFF5E625C), goldSoft = Color(0xFFF6E7C4), goldText = Color(0xFF6B4C14), goldStrong = Color(0xFF4A330A),
    isDark = false,
)

val DarkTokens = NamozlyTokens(
    bg = Color(0xFF0B1714), surface = Color(0xFF13221E), text = Color(0xFFECE7DC), muted = Color(0xFFA3ADA7),
    faint = Color(0xFF5E6B65), strong = Color(0xFFF6F1E6), hero = Color(0xFF124D41), icon = Color(0xFF8FD6BC),
    chip = Color(0xFF1E3A32), chipText = Color(0xFFB5E7D4), primary = Color(0xFF1F6E5A), primarySoft = Color(0xFF16332B),
    accent = Color(0xFFD9A94F), dash = Color(0xFF3A4A44), line = Color(0xFF1E2E29), nav = Color(0xFF101D19),
    navOff = Color(0xFFA3ADA7), goldSoft = Color(0xFF3A2F17), goldText = Color(0xFFE7CF97), goldStrong = Color(0xFFF6E7C4),
    isDark = true,
)

private fun scheme(t: NamozlyTokens) = if (t.isDark) {
    darkColorScheme(
        primary = t.primary, onPrimary = Color.White, primaryContainer = t.chip, onPrimaryContainer = t.chipText,
        secondary = AccentGold, onSecondary = Color(0xFF2B1F06), secondaryContainer = t.goldSoft, onSecondaryContainer = t.goldText,
        tertiary = t.icon, background = t.bg, onBackground = t.text, surface = t.surface, onSurface = t.text,
        surfaceVariant = t.primarySoft, onSurfaceVariant = t.muted, outline = t.line, outlineVariant = t.dash,
        error = Color(0xFFEF9A9A),
    )
} else {
    lightColorScheme(
        primary = t.primary, onPrimary = Color.White, primaryContainer = t.chip, onPrimaryContainer = t.chipText,
        secondary = AccentGold, onSecondary = Color(0xFF2B1F06), secondaryContainer = t.goldSoft, onSecondaryContainer = t.goldText,
        tertiary = t.icon, background = t.bg, onBackground = t.text, surface = t.surface, onSurface = t.text,
        surfaceVariant = t.primarySoft, onSurfaceVariant = t.muted, outline = t.line, outlineVariant = t.dash,
        error = ErrorRed,
    )
}

val LocalNamozlyTokens = staticCompositionLocalOf { LightTokens }

private val NamozlyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun NamozlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) DarkTokens else LightTokens
    CompositionLocalProvider(LocalNamozlyTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme(tokens),
            typography = NamozlyTypography,
            shapes = NamozlyShapes,
            content = content,
        )
    }
}

object NamozlyThemeTokens {
    /** Dizayn tokenlari: `NamozlyThemeTokens.t.chip` va h.k. */
    val t: NamozlyTokens
        @Composable get() = LocalNamozlyTokens.current

    /** Eski nom (moslik uchun). */
    val extra: NamozlyTokens
        @Composable get() = LocalNamozlyTokens.current
}
