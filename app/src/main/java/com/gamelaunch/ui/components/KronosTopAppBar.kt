package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.ui.theme.ManropeFamily
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerHigh

@Composable
fun KronosTopAppBar(
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu icon — left
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Menú",
                tint = PrimaryFixed,
                modifier = Modifier.size(24.dp)
            )
        }

        // Brand name — center
        Text(
            text = "KRONOS",
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            letterSpacing = (-0.5).sp,
            color = PrimaryFixed
        )

        // Right actions — search + avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceContainerHigh, CircleShape)
                    .border(1.dp, PrimaryFixed.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    fontFamily = ManropeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryFixed
                )
            }
        }
    }
}
