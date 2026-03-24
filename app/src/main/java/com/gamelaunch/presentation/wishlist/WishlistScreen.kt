package com.gamelaunch.presentation.wishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    onGameClick: (Int) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val wishlist by viewModel.wishlist.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi lista") }) }
    ) { padding ->
        if (wishlist.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Añade juegos desde el detalle", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(wishlist) { game ->
                    ListItem(
                        headlineContent = { Text(game.name) },
                        supportingContent = { Text(game.releaseDate.toString()) },
                        leadingContent = {
                            AsyncImage(
                                model = game.coverUrl,
                                contentDescription = game.name,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        modifier = Modifier.clickable { onGameClick(game.id) }
                    )
                }
            }
        }
    }
}
