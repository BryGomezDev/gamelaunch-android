package com.gamelaunch.presentation.day

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.ui.components.GameCard
import com.gamelaunch.ui.components.PlatformChip
import com.gamelaunch.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DayReleasesScreen(
    onGameClick: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: DayReleasesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            DayReleasesTopBar(
                date = state.date,
                releaseCount = state.groupedReleases.size,
                availablePlatforms = state.availablePlatforms,
                selectedPlatform = state.platformFilter,
                onPlatformSelect = viewModel::onPlatformFilter,
                onBack = onBack
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (state.groupedReleases.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📭", fontSize = 40.sp)
                    Text(
                        text = "Sin lanzamientos este día",
                        fontSize = 14.sp,
                        color = TextHint
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                items(state.groupedReleases, key = { it.game.id }) { release ->
                    GameCard(
                        release = release,
                        onClick = { onGameClick(release.game.id) }
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun DayReleasesTopBar(
    date: LocalDate,
    releaseCount: Int,
    availablePlatforms: List<Platform>,
    selectedPlatform: Platform?,
    onPlatformSelect: (Platform?) -> Unit,
    onBack: () -> Unit
) {
    val dayName = date.dayOfMonth.toString()
    val monthName = date.month
        .getDisplayName(TextStyle.FULL, Locale("es"))
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Back button — circle
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .background(SurfaceVariant, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Title + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$dayName de $monthName",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$releaseCount lanzamiento${if (releaseCount != 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = TextHint
                )
            }

            // Platform chips — top-right (tappable, border when selected)
            if (availablePlatforms.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    availablePlatforms.take(3).forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        PlatformChip(
                            platform = platform,
                            small = true,
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) Accent else Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    onPlatformSelect(if (isSelected) null else platform)
                                }
                        )
                    }
                }
            }
        }
    }
}
