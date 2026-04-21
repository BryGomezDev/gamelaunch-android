package com.gamelaunch.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gamelaunch.presentation.calendar.CalendarScreen
import com.gamelaunch.presentation.day.DayReleasesScreen
import com.gamelaunch.presentation.detail.DetailScreen
import com.gamelaunch.presentation.profile.ProfileScreen
import com.gamelaunch.presentation.search.SearchScreen
import com.gamelaunch.presentation.settings.SettingsScreen
import com.gamelaunch.presentation.wishlist.WishlistScreen
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object Wishlist : Screen("wishlist")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{gameId}") {
        fun createRoute(gameId: Int) = "detail/$gameId"
    }
    object DayReleases : Screen("day_releases/{date}") {
        fun createRoute(date: LocalDate) = "day_releases/$date"
    }
    object Profile : Screen("profile")
}

val tabRoutes = setOf(
    Screen.Calendar.route,
    Screen.Search.route,
    Screen.Wishlist.route,
    Screen.Profile.route
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calendar.route,
        enterTransition = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                // Tabs → tabs: crossfade rápido
                from in tabRoutes && to in tabRoutes ->
                    fadeIn(tween(durationMillis = 180))
                // Cualquier pantalla → Detail: fadeIn + scale (sensación de expansión)
                to?.startsWith("detail/") == true ->
                    fadeIn(tween(durationMillis = 260)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(durationMillis = 260))
                // Cualquier pantalla → DayReleases: slide desde abajo
                to?.startsWith("day_releases/") == true ->
                    slideInVertically(tween(durationMillis = 300)) { it } +
                    fadeIn(tween(durationMillis = 220))
                // Resto: slide horizontal estándar
                else ->
                    slideInHorizontally(tween(220)) { it } + fadeIn(tween(180))
            }
        },
        exitTransition = {
            val from = initialState.destination.route
            val to   = targetState.destination.route
            when {
                // Tabs → tabs: crossfade
                from in tabRoutes && to in tabRoutes ->
                    fadeOut(tween(durationMillis = 180))
                // Salida hacia Detail: ligero fade + scale out de la pantalla origen
                to?.startsWith("detail/") == true ->
                    fadeOut(tween(durationMillis = 200)) +
                    scaleOut(targetScale = 0.97f, animationSpec = tween(durationMillis = 200))
                // Salida hacia DayReleases: la pantalla de fondo se queda quieta
                to?.startsWith("day_releases/") == true ->
                    fadeOut(tween(durationMillis = 150))
                // Resto
                else ->
                    slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(180))
            }
        },
        popEnterTransition = {
            val to = targetState.destination.route
            when {
                // Volviendo desde Detail: fade + scale in del origen
                to?.startsWith("detail/") == true ->
                    fadeIn(tween(260)) + scaleIn(initialScale = 0.97f, animationSpec = tween(260))
                // Volviendo desde DayReleases: baja la pantalla
                to?.startsWith("day_releases/") == true ->
                    fadeIn(tween(220))
                else ->
                    slideInHorizontally(tween(220)) { -it } + fadeIn(tween(180))
            }
        },
        popExitTransition = {
            val from = initialState.destination.route
            when {
                // Cerrando Detail
                from?.startsWith("detail/") == true ->
                    fadeOut(tween(220)) + scaleOut(targetScale = 0.92f, animationSpec = tween(220))
                // Cerrando DayReleases: sale hacia abajo
                from?.startsWith("day_releases/") == true ->
                    slideOutVertically(tween(durationMillis = 280)) { it } +
                    fadeOut(tween(durationMillis = 200))
                else ->
                    slideOutHorizontally(tween(220)) { it } + fadeOut(tween(180))
            }
        }
    ) {
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onGameClick   = { navController.navigate(Screen.Detail.createRoute(it)) },
                onDayClick    = { date -> navController.navigate(Screen.DayReleases.createRoute(date)) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                currentRoute  = "home",
                onNavigate    = { route ->
                    when (route) {
                        "home"     -> { /* ya estamos aquí */ }
                        "timeline" -> { /* pantalla futura */ }
                        "milista"  -> navController.navigate(Screen.Wishlist.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "ajustes"  -> navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(onGameClick = { navController.navigate(Screen.Detail.createRoute(it)) })
        }
        composable(Screen.Wishlist.route) {
            WishlistScreen(onGameClick = { navController.navigate(Screen.Detail.createRoute(it)) })
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStack ->
            val gameId = backStack.arguments!!.getInt("gameId")
            DetailScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.popBackStack(Screen.Calendar.route, inclusive = false)
                },
                onGameClick = { navController.navigate(Screen.Detail.createRoute(it)) }
            )
        }
        composable(
            route = Screen.DayReleases.route,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) {
            DayReleasesScreen(
                onGameClick = { navController.navigate(Screen.Detail.createRoute(it)) },
                onBack      = { navController.popBackStack() }
            )
        }
    }
}
