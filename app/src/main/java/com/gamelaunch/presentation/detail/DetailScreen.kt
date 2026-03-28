package com.gamelaunch.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    gameId: Int,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(gameId) { viewModel.loadGame(gameId) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.game?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleWishlist) {
                        Icon(
                            if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.game != null -> {
                val game = state.game!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .aspectRatio(3f / 4f)
                            .align(Alignment.CenterHorizontally)
                            // clip=false: sombra sin fondo blanco de relleno
                            .shadow(4.dp, RoundedCornerShape(8.dp), clip = false)
                    ) {
                        AsyncImage(
                            model = game.coverUrl,
                            contentDescription = game.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(game.name, style = MaterialTheme.typography.headlineMedium)
                    Text("Lanzamiento: ${game.releaseDate}", style = MaterialTheme.typography.bodyMedium)
                    game.rating?.let {
                        Text("Rating: ${"%.1f".format(it)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    game.summary?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Notificarme:", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 7).forEach { days ->
                            FilterChip(
                                selected = state.notifyDaysAhead == days,
                                onClick = { viewModel.setNotifyDaysAhead(days) },
                                label = { Text("$days día${if (days > 1) "s" else ""} antes") }
                            )
                        }
                    }
                }
            }
        }
    }
}

