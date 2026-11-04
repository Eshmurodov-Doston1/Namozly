package uz.dostonbek.namozly.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.feature.calendar.CalendarRoute
import uz.dostonbek.namozly.feature.home.navigation.HomeRoute
import uz.dostonbek.namozly.feature.quran.navigation.QuranRoute
import uz.dostonbek.namozly.feature.settings.SettingsRoute
import kotlin.reflect.KClass

/** Pastki navigatsiya: 2 chap + markazda Qibla kompasi + 2 o'ng. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    HOME(HomeRoute, HomeRoute::class, R.string.nav_home, Icons.Outlined.Home, Icons.Rounded.Home),
    QURAN(QuranRoute, QuranRoute::class, R.string.nav_quran, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Rounded.MenuBook),
    CALENDAR(CalendarRoute, CalendarRoute::class, R.string.nav_calendar, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth),
    SETTINGS(SettingsRoute, SettingsRoute::class, R.string.nav_settings, Icons.Outlined.Settings, Icons.Rounded.Settings),
}
