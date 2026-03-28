package com.gamelaunch.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gamelaunch.BuildConfig
import com.gamelaunch.domain.model.Platform
import io.sentry.Sentry
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onGameClick: (Int) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GameLaunch") },
                actions = {
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = viewModel::seedData) {
                            Icon(Icons.Default.BugReport, contentDescription = "Seed test data")
                        }
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (BuildConfig.DEBUG) {
                    item {
                        DebugInfoBanner(
                            info = state.syncDebugInfo,
                            releaseCount = state.releases.size
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                Sentry.captureMessage("Test manual desde GameLaunch")
                                throw RuntimeException("Test crash de Sentry — puedes ignorar esto")
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("Test Sentry")
                        }
                    }
                }
                item {
                    PlatformFilterRow(
                        selected = state.platformFilter,
                        onSelect = viewModel::onPlatformFilter
                    )
                }
                item {
                    RegionFilterRow(
                        selected = state.regionFilter,
                        onSelect = viewModel::onRegionFilter
                    )
                }
                item {
                    MonthHeader(
                        month = state.currentMonth,
                        onPrev = { viewModel.onMonthChange(state.currentMonth.minusMonths(1)) },
                        onNext = { viewModel.onMonthChange(state.currentMonth.plusMonths(1)) }
                    )
                }
                item {
                    CalendarGrid(
                        month = state.currentMonth,
                        releases = state.releases,
                        selectedDay = state.selectedDay,
                        onDayClick = viewModel::onDaySelected
                    )
                }

                if (state.selectedDay != null) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        Text(
                            text = formatDayTitle(state.selectedDay!!),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                    if (state.selectedDayReleases.isEmpty()) {
                        item {
                            Text(
                                text = "Sin lanzamientos este día",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.selectedDayReleases, key = { it.id }) { release ->
                            ReleaseCard(
                                release = release,
                                onClick = { onGameClick(release.game.id) }
                            )
                        }
                    }
                }

                state.error?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Release Card ─────────────────────────────────────────────────────────────

@Composable
private fun ReleaseCard(release: Release, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Cover art
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(release.game.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = release.game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 60.dp, height = 80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = release.game.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                PlatformBadge(platform = release.platform)
                release.game.rating?.let { rating ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "★ ${"%.0f".format(rating)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatformBadge(platform: Platform) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = platform.toColor()
    ) {
        Text(
            text = platform.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Filter row ───────────────────────────────────────────────────────────────

@Composable
private fun PlatformFilterRow(selected: Platform?, onSelect: (Platform?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Todos") }
            )
        }
        items(Platform.entries) { platform ->
            FilterChip(
                selected = selected == platform,
                onClick = { onSelect(platform) },
                label = { Text(platform.displayName) }
            )
        }
    }
}

// ── Region filter row ─────────────────────────────────────────────────────────

@Composable
private fun RegionFilterRow(selected: Region?, onSelect: (Region?) -> Unit) {
    val regions = listOf(
        Region.WORLDWIDE, Region.EUROPE, Region.NORTH_AMERICA,
        Region.JAPAN, Region.ASIA, Region.AUSTRALIA, Region.BRAZIL, Region.KOREA
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("Todas") }
            )
        }
        items(regions) { region ->
            FilterChip(
                selected = selected == region,
                onClick = { onSelect(region) },
                label = { Text(region.displayName) }
            )
        }
    }
}

// ── Month header ─────────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, "Mes anterior") }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${month.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, "Mes siguiente") }
    }
}

// ── Calendar grid (non-lazy to avoid nested scroll conflicts) ─────────────────

@Composable
private fun CalendarGrid(
    month: YearMonth,
    releases: List<Release>,
    selectedDay: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val releasesByDay = releases.groupBy { it.date.dayOfMonth }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Build weeks
        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7
        var dayCounter = 1

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    if (cellIndex < firstDayOffset || dayCounter > daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = dayCounter
                        val date = month.atDay(day)
                        DayCell(
                            day = day,
                            releases = releasesByDay[day] ?: emptyList(),
                            isSelected = selectedDay == date,
                            isToday = date == LocalDate.now(),
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    releases: List<Release>,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        if (releases.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                releases.map { it.platform }.distinct().take(3).forEach { platform ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(platform.toColor())
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}

// ── Debug banner ─────────────────────────────────────────────────────────────

@Composable
private fun DebugInfoBanner(info: String, releaseCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "DEBUG | $info | releases en UI: $releaseCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDayTitle(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }
    return "$dayName, ${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}"
}

fun Platform.toColor(): Color = when (this) {
    Platform.STEAM -> Color(0xFF1B2838)
    Platform.PLAYSTATION_5, Platform.PLAYSTATION_4 -> Color(0xFF003791)
    Platform.XBOX_SERIES, Platform.XBOX_ONE -> Color(0xFF107C10)
    Platform.NINTENDO_SWITCH -> Color(0xFFE4000F)
}
