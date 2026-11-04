package uz.dostonbek.namozly.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import uz.dostonbek.namozly.core.designsystem.R

/** Dizayndagi asosiy shrift — Plus Jakarta Sans (OFL). */
val JakartaFamily = FontFamily(
    Font(R.font.plus_jakarta_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_extrabold, FontWeight.ExtraBold),
)

/** Arabcha matn — Amiri (OFL). */
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold),
)

private fun s(size: Int, weight: FontWeight, line: Int = (size * 1.35).toInt(), spacing: Double = 0.0) = TextStyle(
    fontFamily = JakartaFamily,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = spacing.em,
)

val NamozlyTypography = Typography(
    displayLarge = s(52, FontWeight.ExtraBold, 56, -0.03),
    displayMedium = s(40, FontWeight.ExtraBold, 46, -0.02),
    displaySmall = s(30, FontWeight.ExtraBold, 36, -0.01),
    headlineLarge = s(28, FontWeight.ExtraBold, 34, -0.02),
    headlineMedium = s(26, FontWeight.ExtraBold, 32, -0.02),
    headlineSmall = s(22, FontWeight.ExtraBold, 28, -0.01),
    titleLarge = s(19, FontWeight.ExtraBold, 24, -0.02),
    titleMedium = s(15, FontWeight.ExtraBold, 20),
    titleSmall = s(14, FontWeight.Bold, 19),
    bodyLarge = s(15, FontWeight.Normal, 22),
    bodyMedium = s(14, FontWeight.Normal, 21),
    bodySmall = s(12, FontWeight.Medium, 17),
    labelLarge = s(13, FontWeight.Bold, 18),
    labelMedium = s(12, FontWeight.Bold, 16),
    labelSmall = s(11, FontWeight.SemiBold, 14),
)

val ArabicTextStyle = TextStyle(
    fontFamily = AmiriFamily,
    fontSize = 24.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeight.Normal,
)
