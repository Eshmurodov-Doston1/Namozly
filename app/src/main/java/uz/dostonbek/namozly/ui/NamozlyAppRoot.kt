package uz.dostonbek.namozly.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.dostonbek.namozly.core.designsystem.component.NamozlyNavBar
import uz.dostonbek.namozly.core.designsystem.component.NavBarItem
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.ui.sensor.isAlignedToQibla
import uz.dostonbek.namozly.core.ui.sensor.magneticDeclination
import uz.dostonbek.namozly.core.ui.sensor.rememberDeviceHeading
import uz.dostonbek.namozly.core.ui.sensor.trueHeading
import uz.dostonbek.namozly.feature.qibla.navigation.QiblaRoute
import uz.dostonbek.namozly.feature.qibla.navigation.navigateToQibla
import uz.dostonbek.namozly.navigation.NamozlyNavHost
import uz.dostonbek.namozly.navigation.TopLevelDestination
import uz.dostonbek.namozly.navigation.navigateToTopLevel
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(locationRepository: LocationRepository) : ViewModel() {
    val point: StateFlow<GeoPoint> = locationRepository.location.map { it.point }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserLocation.Default.point)
}

@Composable
fun NamozlyAppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val point by viewModel.point.collectAsStateWithLifecycle()

    val showBar = destination.isTopLevel()
    // State obyekti o'zi uzatiladi (qiymati shu yerda o'qilmaydi) — aks holda har sensor o'zgarishida
    // butun ilova (Scaffold + NavHost) qayta chiziladi va kompas sekinlashadi.
    val headingState = rememberDeviceHeading()
    val bearing = remember(point) { QiblaCalculator.bearing(point) }
    val declination = remember(point) { magneticDeclination(point.latitude, point.longitude) }
    val trueH: () -> Float? = remember(declination) { { headingState.value?.let { trueHeading(it.azimuth, declination) } } }
    val aligned by remember(bearing, declination) { derivedStateOf { isAlignedToQibla(bearing, trueH()) } }

    val items = TopLevelDestination.entries.map {
        NavBarItem(key = it.name, label = stringResource(it.labelRes), icon = it.icon, selectedIcon = it.selectedIcon)
    }
    val selectedKey = TopLevelDestination.entries.firstOrNull { destination?.hasRoute(it.routeClass) == true }?.name

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBar) {
                NamozlyNavBar(
                    items = items,
                    selectedKey = selectedKey,
                    onSelect = { item -> navController.navigateToTopLevel(TopLevelDestination.valueOf(item.key)) },
                    qiblaLabel = stringResource(uz.dostonbek.namozly.core.ui.R.string.qibla_title),
                    qiblaSelected = destination?.hasRoute(QiblaRoute::class) == true,
                    heading = trueH,
                    qiblaBearing = bearing.toFloat(),
                    compassAligned = aligned,
                    onCompassClick = {
                        navController.navigateToQibla(navOptions { launchSingleTop = true })
                    },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(PaddingValues(bottom = padding.calculateBottomPadding()))) {
            NamozlyNavHost(navController)
        }
    }
}

private fun NavDestination?.isTopLevel(): Boolean {
    if (this == null) return true
    return TopLevelDestination.entries.any { hasRoute(it.routeClass) } || hasRoute(QiblaRoute::class)
}
