package com.gamelaunch.presentation.wishlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.ui.components.GameListCard
import com.gamelaunch.ui.components.KronosBottomNav
import com.gamelaunch.ui.components.KronosDrawer
import com.gamelaunch.ui.components.KronosTopAppBar
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.ManropeFamily
import com.gamelaunch.ui.theme.OnSurface
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerHigh
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onGameClick: (Int) -> Unit,
    onNavigate: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    currentRoute: String = "milista",
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val wishlist by viewModel.wishlist.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    KronosDrawer(
        drawerState = drawerState,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Background,
                topBar = {
                    KronosTopAppBar(
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onSearchClick = {},
                        onAvatarClick = onAvatarClick
                    )
                }
            ) { padding ->
                if (wishlist.isEmpty()) {
                    EmptyWishlistState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background)
                            .padding(padding),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "header") {
                            Text(
                                text = "MI LISTA · ${wishlist.size} JUEGOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryFixed.copy(alpha = 0.70f),
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        items(wishlist, key = { it.id }) { game ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.removeFromWishlist(game.id)
                                        true
                                    } else false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val bgColor by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                                            MaterialTheme.colorScheme.errorContainer
                                        else Background,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "swipe_bg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgColor, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.BookmarkRemove,
                                            contentDescription = "Quitar de lista",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(end = 24.dp)
                                        )
                                    }
                                }
                            ) {
                                GameListCard(
                                    game = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
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

@Composable
private fun EmptyWishlistState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
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
                imageVector = Icons.Outlined.BookmarkAdd,
                contentDescription = null,
                tint = PrimaryFixed.copy(alpha = 0.50f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Tu lista está vacía",
            fontFamily = ManropeFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = OnSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Añade juegos desde el detalle",
            fontSize = 14.sp,
            color = OnSurfaceVariant
        )
    }
}
