package uz.dostonbek.namozly.feature.calendar

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object CalendarRoute

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) = navigate(CalendarRoute, navOptions)

fun NavGraphBuilder.calendarScreen() {
    composable<CalendarRoute> { CalendarScreenRoute() }
}
