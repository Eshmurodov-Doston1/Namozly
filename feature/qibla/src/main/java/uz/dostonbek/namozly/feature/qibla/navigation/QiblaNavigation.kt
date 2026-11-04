package uz.dostonbek.namozly.feature.qibla.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.feature.qibla.QiblaRoute as QiblaScreenRoute

@Serializable
data object QiblaRoute

fun NavController.navigateToQibla(navOptions: NavOptions? = null) = navigate(QiblaRoute, navOptions)

fun NavGraphBuilder.qiblaScreen(onOpenLocation: () -> Unit = {}) {
    composable<QiblaRoute> { QiblaScreenRoute(onOpenLocation = onOpenLocation) }
}
