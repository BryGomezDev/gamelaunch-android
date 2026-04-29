package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.ui.theme.ManropeFamily
import com.gamelaunch.ui.theme.OnSurface
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.OutlineVariant
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch

private data class DrawerItem(val route: String, val icon: ImageVector, val label: String)

private val kronosDrawerItems = listOf(
    DrawerItem("home",    Icons.Filled.Home,      "Inicio"),
    DrawerItem("search",  Icons.Filled.Search,    "Buscar"),
    DrawerItem("milista", Icons.Filled.Bookmarks, "Mi Lista"),
    DrawerItem("ajustes", Icons.Filled.Settings,  "Ajustes"),
)

@Composable
fun KronosDrawer(
    drawerState: DrawerState,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceContainerLowest,
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "KRONOS",
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                    color = PrimaryFixed,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = "Gamer's Planner",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 20.dp)
                )
                HorizontalDivider(
                    color = OutlineVariant.copy(alpha = 0.30f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                kronosDrawerItems.forEach { item ->
                    val isActive = currentRoute == item.route
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .background(
                                if (isActive) PrimaryFixed.copy(alpha = 0.12f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                scope.launch { drawerState.close() }
                                onNavigate(item.route)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isActive) PrimaryFixed else OnSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 15.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isActive) PrimaryFixed else OnSurface
                        )
                    }
                }
            }
        },
        content = content
    )
}
