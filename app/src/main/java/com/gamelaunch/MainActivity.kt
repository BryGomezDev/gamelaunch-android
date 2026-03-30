package com.gamelaunch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gamelaunch.presentation.navigation.AppNavGraph
import com.gamelaunch.presentation.navigation.Screen
import com.gamelaunch.presentation.navigation.tabRoutes
import com.gamelaunch.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameLaunchTheme {
                MainScaffold()
            }
        }
    }
}

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomItems = listOf(
        NavItem(Screen.Calendar.route, "Inicio",   Icons.Default.Home),
        NavItem(Screen.Search.route,   "Buscar",   Icons.Default.Search),
        NavItem(Screen.Wishlist.route, "Mi lista", Icons.Default.Favorite),
        NavItem(Screen.Profile.route,  "Perfil",   Icons.Default.Person)
    )

    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = BorderSubtle)
                    NavigationBar(
                        containerColor = Background,
                        tonalElevation = 0.dp
                    ) {
                        bottomItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor   = Accent,
                                    selectedTextColor   = Accent,
                                    unselectedIconColor = TextHint,
                                    unselectedTextColor = TextHint,
                                    indicatorColor      = AccentDim
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(padding)
        ) {
            AppNavGraph(navController = navController)
        }
    }
}
