package uz.dostonbek.namozly.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import uz.dostonbek.namozly.feature.books.booksScreens
import uz.dostonbek.namozly.feature.books.navigateToBookReader
import uz.dostonbek.namozly.feature.books.navigateToBooks
import uz.dostonbek.namozly.feature.calendar.calendarScreen
import uz.dostonbek.namozly.feature.calendar.navigateToCalendar
import uz.dostonbek.namozly.feature.duas.duasScreen
import uz.dostonbek.namozly.feature.duas.navigateToDuas
import uz.dostonbek.namozly.feature.home.HomeDestination
import uz.dostonbek.namozly.feature.home.navigation.HomeRoute
import uz.dostonbek.namozly.feature.home.navigation.homeScreen
import uz.dostonbek.namozly.feature.learn.learnScreen
import uz.dostonbek.namozly.feature.learn.navigateToLearn
import uz.dostonbek.namozly.feature.learn.navigateToLesson
import uz.dostonbek.namozly.feature.live.liveScreens
import uz.dostonbek.namozly.feature.live.navigateToLive
import uz.dostonbek.namozly.feature.live.navigateToLivePlayer
import uz.dostonbek.namozly.feature.mosques.mosquesScreen
import uz.dostonbek.namozly.feature.mosques.navigateToMosques
import uz.dostonbek.namozly.feature.mosques.navigateToWorldMosques
import uz.dostonbek.namozly.feature.names.namesScreen
import uz.dostonbek.namozly.feature.names.navigateToNames
import uz.dostonbek.namozly.feature.qaza.navigateToQaza
import uz.dostonbek.namozly.feature.qaza.qazaScreen
import uz.dostonbek.namozly.feature.qibla.navigation.navigateToQibla
import uz.dostonbek.namozly.feature.qibla.navigation.qiblaScreen
import uz.dostonbek.namozly.feature.quran.navigation.navigateToSurah
import uz.dostonbek.namozly.feature.quran.navigation.quranScreens
import uz.dostonbek.namozly.feature.ramadan.navigateToRamadan
import uz.dostonbek.namozly.feature.ramadan.ramadanScreen
import uz.dostonbek.namozly.feature.settings.navigateToLocation
import uz.dostonbek.namozly.feature.settings.navigateToNotifications
import uz.dostonbek.namozly.feature.settings.settingsScreens
import uz.dostonbek.namozly.feature.tasbih.navigation.navigateToLeaderboard
import uz.dostonbek.namozly.feature.tasbih.navigation.navigateToProfile
import uz.dostonbek.namozly.feature.tasbih.navigation.navigateToRegister
import uz.dostonbek.namozly.feature.tasbih.navigation.navigateToTasbih
import uz.dostonbek.namozly.feature.tasbih.navigation.tasbihScreens

/**
 * Ilovaning yagona navigatsiya grafi. Feature modullar bir-birini bilmaydi —
 * o'tishlar faqat shu yerda (app qatlamida) ulanadi.
 */
@Composable
fun NamozlyNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val back: () -> Unit = { navController.popBackStack() }
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { slideInHorizontally { it / 6 } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 6 } + fadeOut() },
    ) {
        homeScreen(onNavigate = { d ->
            when (d) {
                HomeDestination.Quran -> navController.navigateToTopLevel(TopLevelDestination.QURAN)
                is HomeDestination.Surah -> navController.navigateToSurah(d.number, d.ayah ?: 0)
                HomeDestination.Tasbih -> navController.navigateToTasbih()
                HomeDestination.Duas -> navController.navigateToDuas()
                HomeDestination.Names -> navController.navigateToNames()
                HomeDestination.Qaza -> navController.navigateToQaza()
                HomeDestination.Ramadan -> navController.navigateToRamadan()
                HomeDestination.Mosques -> navController.navigateToMosques()
                HomeDestination.Live -> navController.navigateToLive()
                HomeDestination.Learn -> navController.navigateToLearn()
                HomeDestination.Calendar -> navController.navigateToTopLevel(TopLevelDestination.CALENDAR)
                HomeDestination.Notifications -> navController.navigateToNotifications()
                HomeDestination.Location -> navController.navigateToLocation()
                HomeDestination.Qibla -> navController.navigateToQibla(androidx.navigation.navOptions { launchSingleTop = true })
                HomeDestination.Profile -> navController.navigateToProfile()
                HomeDestination.Books -> navController.navigateToBooks()
            }
        })
        quranScreens(onOpenSurah = { n, a -> navController.navigateToSurah(n, a) }, onBack = back)
        qiblaScreen(onOpenLocation = { navController.navigateToLocation() })
        calendarScreen()
        settingsScreens(
            onBack = back,
            onOpenLocation = { navController.navigateToLocation() },
            onOpenNotifications = { navController.navigateToNotifications() },
        )
        tasbihScreens(
            onBack = back,
            onOpenLeaderboard = { navController.navigateToLeaderboard() },
            onOpenRegister = { navController.navigateToRegister() },
            onOpenLocation = { navController.navigateToLocation() },
            onOpenNotifications = { navController.navigateToNotifications() },
            onOpenSettings = { navController.navigateToTopLevel(TopLevelDestination.SETTINGS) },
        )
        mosquesScreen(onBack = back, onOpenWorld = { navController.navigateToWorldMosques() })
        namesScreen(onBack = back)
        duasScreen(onBack = back)
        qazaScreen(onBack = back)
        ramadanScreen(onBack = back)
        learnScreen(onBack = back, onOpenLesson = { navController.navigateToLesson(it) })
        booksScreens(onBack = back, onOpen = { id, n -> navController.navigateToBookReader(id, n) })
        liveScreens(onBack = back, onOpen = { navController.navigateToLivePlayer(it) })
    }
}

fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun androidx.navigation.NavGraph.findStartDestination(): androidx.navigation.NavDestination {
    var d: androidx.navigation.NavDestination = this
    while (d is androidx.navigation.NavGraph) d = d.findNode(d.startDestinationId) ?: break
    return d
}
