package uz.dostonbek.namozly.feature.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object SettingsRoute
@Serializable data object NotificationsRoute
@Serializable data object LocationRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)
fun NavController.navigateToNotifications() = navigate(NotificationsRoute)
fun NavController.navigateToLocation() = navigate(LocationRoute)

fun NavGraphBuilder.settingsScreens(
    onBack: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    composable<SettingsRoute> { SettingsScreen(onOpenLocation = onOpenLocation, onOpenNotifications = onOpenNotifications) }
    composable<NotificationsRoute> { NotificationsScreen(onBack) }
    composable<LocationRoute> { LocationScreen(onBack) }
}
