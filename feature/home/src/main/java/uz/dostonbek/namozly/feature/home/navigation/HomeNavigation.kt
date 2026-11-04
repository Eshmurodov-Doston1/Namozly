package uz.dostonbek.namozly.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.feature.home.HomeDestination
import uz.dostonbek.namozly.feature.home.HomeRoute as HomeScreenRoute

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) = navigate(HomeRoute, navOptions)

fun NavGraphBuilder.homeScreen(onNavigate: (HomeDestination) -> Unit) {
    composable<HomeRoute> { HomeScreenRoute(onNavigate = onNavigate) }
}
