package uz.dostonbek.namozly.feature.mosques

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object MosquesRoute
@Serializable data object WorldMosquesRoute

fun NavController.navigateToMosques() = navigate(MosquesRoute)
fun NavController.navigateToWorldMosques() = navigate(WorldMosquesRoute)

fun NavGraphBuilder.mosquesScreen(onBack: () -> Unit, onOpenWorld: () -> Unit = {}) {
    composable<MosquesRoute> { MosquesScreenRoute(onBack = onBack, onOpenWorld = onOpenWorld) }
    composable<WorldMosquesRoute> { WorldMosquesScreen(onBack = onBack) }
}
