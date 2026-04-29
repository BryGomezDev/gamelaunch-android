package com.gamelaunch.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gamelaunch.ui.components.KronosBottomNav
import com.gamelaunch.ui.components.KronosDrawer
import com.gamelaunch.ui.components.KronosTopAppBar
import com.gamelaunch.ui.theme.Background
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onGameClick: (Int) -> Unit,
    onNavigate: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    currentRoute: String = "search",
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val showHistory = state.query.isEmpty() && state.recentSearches.isNotEmpty()
    val showFilters = state.results.isNotEmpty()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    KronosDrawer(drawerState = drawerState, currentRoute = currentRoute, onNavigate = onNavigate) {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(padding)
                ) {

                    // ── Search field ──────────────────────────────────────────────
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Nombre del juego...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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

                    // ── Filters (game modes) ──────────────────────────────────────
                    if (showFilters && state.availableGameModes.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.gameModeFilter == null,
                                    onClick = { viewModel.onGameModeFilter(null) },
                                    label = { Text("Todos los modos") }
                                )
                            }
                            items(state.availableGameModes) { mode ->
                                FilterChip(
                                    selected = state.gameModeFilter == mode,
                                    onClick = { viewModel.onGameModeFilter(if (state.gameModeFilter == mode) null else mode) },
                                    label = { Text(mode) }
                                )
                            }
                        }
                    }

                    // ── Filters (genres) ──────────────────────────────────────────
                    if (showFilters && state.availableGenres.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.genreFilter == null,
                                    onClick = { viewModel.onGenreFilter(null) },
                                    label = { Text("Todos los géneros") }
                                )
                            }
                            items(state.availableGenres) { genre ->
                                FilterChip(
                                    selected = state.genreFilter == genre,
                                    onClick = { viewModel.onGenreFilter(if (state.genreFilter == genre) null else genre) },
                                    label = { Text(genre) }
                                )
                            }
                        }
                    }

                    // ── Results count when filters active ─────────────────────────
                    if (showFilters && (state.genreFilter != null || state.gameModeFilter != null)) {
                        Text(
                            text = "${state.filteredResults.size} resultado${if (state.filteredResults.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }

                    // ── List ──────────────────────────────────────────────────────
                    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
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
                                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                                    modifier = Modifier.clickable { viewModel.onHistorySelected(query) }
                                )
                            }
                            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                        }

                        items(state.filteredResults, key = { it.id }) { game ->
                            ListItem(
                                headlineContent = {
                                    Text(game.name, fontWeight = FontWeight.Medium)
                                },
                                supportingContent = {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (game.genres.isNotEmpty()) {
                                            Text(
                                                game.genres.take(3).joinToString(" · "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (game.gameModes.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                game.gameModes.take(2).forEach { mode ->
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = MaterialTheme.colorScheme.secondaryContainer
                                                    ) {
                                                        Text(
                                                            text = mode,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                leadingContent = {
                                    AsyncImage(
                                        model = game.coverUrl,
                                        contentDescription = game.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(width = 48.dp, height = 64.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                },
                                trailingContent = game.rating?.let { rating ->
                                    {
                                        Text(
                                            "★ ${"%.0f".format(rating / 10f)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.clickable { onGameClick(game.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        if (state.canLoadMore && state.genreFilter == null && state.gameModeFilter == null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
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
