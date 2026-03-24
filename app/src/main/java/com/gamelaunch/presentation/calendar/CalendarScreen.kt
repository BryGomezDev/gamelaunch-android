package com.gamelaunch.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.domain.model.Platform
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
            TopAppBar(title = { Text("GameLaunch") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PlatformFilterRow(
                selected = state.platformFilter,
                onSelect = viewModel::onPlatformFilter
            )
            MonthHeader(
                month = state.currentMonth,
                onPrev = { viewModel.onMonthChange(state.currentMonth.minusMonths(1)) },
                onNext = { viewModel.onMonthChange(state.currentMonth.plusMonths(1)) }
            )
            CalendarGrid(
                month = state.currentMonth,
                releases = state.releases,
                selectedDay = state.selectedDay,
                onDayClick = viewModel::onDaySelected
            )
            if (state.selectedDay != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = state.selectedDay!!.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                state.selectedDayReleases.forEach { release ->
                    ReleaseListItem(release = release, onClick = { onGameClick(release.game.id) })
                }
            }
        }
    }
}

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

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ChevronLeft, "Previous") }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, "Next") }
    }
}

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

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        // Day-of-week headers
        items(7) { index ->
            val labels = listOf("D", "L", "M", "X", "J", "V", "S")
            Text(
                text = labels[index],
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(4.dp)
            )
        }
        // Empty cells before first day
        items(firstDayOffset) { Spacer(Modifier.size(40.dp)) }
        // Day cells
        items(daysInMonth) { index ->
            val day = index + 1
            val date = month.atDay(day)
            val dayReleases = releasesByDay[day] ?: emptyList()
            val isSelected = selectedDay == date
            DayCell(
                day = day,
                releases = dayReleases,
                isSelected = isSelected,
                onClick = { onDayClick(date) }
            )
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    releases: List<Release>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        if (releases.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.Center) {
                releases.map { it.platform }.distinct().take(3).forEach { platform ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(platform.toColor())
                    )
                    Spacer(Modifier.width(1.dp))
                }
            }
        }
    }
}

@Composable
private fun ReleaseListItem(release: Release, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(release.game.name) },
        supportingContent = { Text(release.platform.displayName) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun Platform.toColor(): Color = when (this) {
    Platform.STEAM -> Color(0xFF1B2838)
    Platform.PLAYSTATION_5, Platform.PLAYSTATION_4 -> Color(0xFF003087)
    Platform.XBOX_SERIES, Platform.XBOX_ONE -> Color(0xFF107C10)
    Platform.NINTENDO_SWITCH -> Color(0xFFE4000F)
    Platform.PC_EPIC -> Color(0xFF2F3136)
}
