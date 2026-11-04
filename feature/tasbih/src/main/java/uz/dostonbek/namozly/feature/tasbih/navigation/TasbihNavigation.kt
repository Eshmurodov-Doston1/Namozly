package uz.dostonbek.namozly.feature.tasbih.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.feature.tasbih.LeaderboardRoute as LeaderboardScreenRoute
import uz.dostonbek.namozly.feature.tasbih.ProfileScreen
import uz.dostonbek.namozly.feature.tasbih.RegisterScreen
import uz.dostonbek.namozly.feature.tasbih.TasbihRoute as TasbihScreenRoute

@Serializable data object TasbihRoute
@Serializable data object LeaderboardRoute
@Serializable data object ProfileRoute
@Serializable data object RegisterRoute

fun NavController.navigateToTasbih(navOptions: NavOptions? = null) = navigate(TasbihRoute, navOptions)
fun NavController.navigateToLeaderboard() = navigate(LeaderboardRoute)
fun NavController.navigateToProfile() = navigate(ProfileRoute)
fun NavController.navigateToRegister() = navigate(RegisterRoute)

fun NavGraphBuilder.tasbihScreens(
    onBack: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenRegister: () -> Unit = {},
    onOpenLocation: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    composable<TasbihRoute> { TasbihScreenRoute(onBack = onBack, onOpenLeaderboard = onOpenLeaderboard) }
    composable<LeaderboardRoute> { LeaderboardScreenRoute(onBack = onBack, onOpenRegister = onOpenRegister) }
    composable<ProfileRoute> { ProfileScreen(onBack, onOpenRegister, onOpenLeaderboard, onOpenLocation, onOpenNotifications, onOpenSettings) }
    composable<RegisterRoute> { RegisterScreen(onBack = onBack, onDone = onOpenLeaderboard) }
}
