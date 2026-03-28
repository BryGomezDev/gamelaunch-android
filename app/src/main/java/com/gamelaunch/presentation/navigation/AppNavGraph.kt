package com.gamelaunch.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gamelaunch.presentation.calendar.CalendarScreen
import com.gamelaunch.presentation.detail.DetailScreen
import com.gamelaunch.presentation.search.SearchScreen
import com.gamelaunch.presentation.settings.SettingsScreen
import com.gamelaunch.presentation.wishlist.WishlistScreen

sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object Wishlist : Screen("wishlist")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{gameId}") {
        fun createRoute(gameId: Int) = "detail/$gameId"
    }
}

private val tabRoutes = setOf(
    Screen.Calendar.route,
    Screen.Search.route,
    Screen.Wishlist.route,
    Screen.Settings.route
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Calendar.route,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Screen.Calendar.route) {
            CalendarScreen(onGameClick = { navController.navigate(Screen.Detail.createRoute(it)) })
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
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType }),
            enterTransition = {
                slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
            }
        ) { backStack ->
            val gameId = backStack.arguments!!.getInt("gameId")
            DetailScreen(gameId = gameId, onBack = { navController.popBackStack() })
        }
    }
}
