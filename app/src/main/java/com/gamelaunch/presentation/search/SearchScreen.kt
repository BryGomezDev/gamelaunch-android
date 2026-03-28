package com.gamelaunch.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onGameClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val showHistory = state.query.isEmpty() && state.recentSearches.isNotEmpty()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buscar juegos") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Nombre del juego...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            LazyColumn {
                if (showHistory) {
                    item {
                        Text(
                            "Búsquedas recientes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(state.recentSearches) { query ->
                        ListItem(
                            headlineContent = { Text(query) },
                            leadingContent = {
                                Icon(Icons.Default.History, contentDescription = null)
                            },
                            modifier = Modifier.clickable { viewModel.onHistorySelected(query) }
                        )
                    }
                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                items(state.results) { game ->
                    ListItem(
                        headlineContent = { Text(game.name) },
                        supportingContent = { Text(game.genres.take(2).joinToString(", ")) },
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
                if (state.canLoadMore) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isLoadingMore) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                TextButton(onClick = viewModel::loadMore) {
                                    Text("Cargar más")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
