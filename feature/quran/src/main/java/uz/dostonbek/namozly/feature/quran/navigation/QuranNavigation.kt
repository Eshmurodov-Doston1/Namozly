package uz.dostonbek.namozly.feature.quran.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.feature.quran.SurahDetailRoute
import uz.dostonbek.namozly.feature.quran.SurahListRoute

@Serializable
data object QuranRoute

/** [ayah] = 0 — boshidan. */
@Serializable
data class SurahRoute(val number: Int, val ayah: Int = 0)

fun NavController.navigateToQuran(navOptions: NavOptions? = null) = navigate(QuranRoute, navOptions)

fun NavController.navigateToSurah(number: Int, ayah: Int = 0) = navigate(SurahRoute(number, ayah))

fun NavGraphBuilder.quranScreens(
    onOpenSurah: (number: Int, ayah: Int) -> Unit,
    onBack: () -> Unit,
) {
    composable<QuranRoute> { SurahListRoute(onOpenSurah = onOpenSurah) }
    composable<SurahRoute> { SurahDetailRoute(onBack = onBack) }
}
