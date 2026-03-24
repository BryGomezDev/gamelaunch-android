package com.gamelaunch.presentation.navigation

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

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Calendar.route) {
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
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStack ->
            val gameId = backStack.arguments!!.getInt("gameId")
            DetailScreen(gameId = gameId, onBack = { navController.popBackStack() })
        }
    }
}
