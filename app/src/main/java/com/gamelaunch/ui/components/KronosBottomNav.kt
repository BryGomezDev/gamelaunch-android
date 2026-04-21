package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gamelaunch.ui.theme.OnPrimary
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerLowest

private data class BottomNavItem(
    val route: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val contentDescription: String
)

private val kronosNavItems = listOf(
    BottomNavItem("home",     Icons.Filled.Home,      Icons.Outlined.Home,      "Home"),
    BottomNavItem("timeline", Icons.Filled.Timeline,  Icons.Outlined.Timeline,  "Timeline"),
    BottomNavItem("milista",  Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks, "Mi Lista"),
    BottomNavItem("ajustes",  Icons.Filled.Settings,  Icons.Outlined.Settings,  "Ajustes")
)

@Composable
fun KronosBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(50.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(elevation = 16.dp, shape = pillShape, clip = false)
            .background(color = SurfaceContainerLowest.copy(alpha = 0.90f), shape = pillShape)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        kronosNavItems.forEach { item ->
            val isActive = currentRoute == item.route
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive) PrimaryFixed else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onNavigate(item.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) item.iconFilled else item.iconOutlined,
                    contentDescription = item.contentDescription,
                    tint = if (isActive) OnPrimary else Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
