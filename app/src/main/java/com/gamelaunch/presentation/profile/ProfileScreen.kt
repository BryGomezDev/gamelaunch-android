package com.gamelaunch.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.ui.components.KronosBottomNav
import com.gamelaunch.ui.components.KronosDrawer
import com.gamelaunch.ui.components.KronosTopAppBar
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.ManropeFamily
import com.gamelaunch.ui.theme.OnSurface
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.SurfaceContainerHigh
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    currentRoute: String = "",
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    KronosDrawer(drawerState = drawerState, currentRoute = currentRoute, onNavigate = onNavigate) {
        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Scaffold(containerColor = Background, topBar = {
                KronosTopAppBar(
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onSearchClick = { onNavigate("search") },
                    onAvatarClick = onAvatarClick
                )
            }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(SurfaceContainerHigh, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Próximamente",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = OnSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tu perfil y lista sincronizada",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant
                    )
                }
            }

            KronosBottomNav(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}
