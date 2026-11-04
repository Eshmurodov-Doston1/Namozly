package uz.dostonbek.namozly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material.icons.rounded.Brightness5
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.LocalizedText
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.QazaPrayer
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Kontent (DB/statik) tilini tanlash uchun — UI matnlari esa Android resurslaridan keladi. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.UZ_LATN }

@Composable
@ReadOnlyComposable
fun LocalizedText.localized(): String = this[LocalAppLanguage.current]

fun Prayer.labelRes(): Int = when (this) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}

fun QazaPrayer.labelRes(): Int = when (this) {
    QazaPrayer.FAJR -> R.string.prayer_fajr
    QazaPrayer.DHUHR -> R.string.prayer_dhuhr
    QazaPrayer.ASR -> R.string.prayer_asr
    QazaPrayer.MAGHRIB -> R.string.prayer_maghrib
    QazaPrayer.ISHA -> R.string.prayer_isha
    QazaPrayer.WITR -> R.string.prayer_witr
}

val Prayer.icon: ImageVector
    get() = when (this) {
        Prayer.FAJR -> Icons.Rounded.WbTwilight
        Prayer.SUNRISE -> Icons.Rounded.Brightness5
        Prayer.DHUHR -> Icons.Rounded.WbSunny
        Prayer.ASR -> Icons.Rounded.LightMode
        Prayer.MAGHRIB -> Icons.Rounded.Brightness3
        Prayer.ISHA -> Icons.Rounded.NightsStay
    }

private val HHMM = DateTimeFormatter.ofPattern("HH:mm")

fun LocalTime.hhmm(): String = format(HHMM)

/** 01:23:45 ko'rinishida. */
fun Duration.countdown(): String {
    val s = seconds.coerceAtLeast(0)
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}
