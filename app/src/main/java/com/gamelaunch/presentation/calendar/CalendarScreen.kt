package com.gamelaunch.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.BuildConfig
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import com.gamelaunch.ui.components.HeroCard
import com.gamelaunch.ui.components.WeekCard
import com.gamelaunch.ui.theme.*
import io.sentry.Sentry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onGameClick: (Int) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            CalendarTopBar(
                onSearchClick = onSearchClick,
                onDebugSeed = if (BuildConfig.DEBUG) viewModel::seedData else null
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                if (BuildConfig.DEBUG) {
                    item {
                        DebugInfoBanner(state.syncDebugInfo, state.releases.size)
                    }
                    item {
                        Button(
                            onClick = {
                                Sentry.captureMessage("Test manual desde GameLaunch")
                                throw RuntimeException("Test crash de Sentry — puedes ignorar esto")
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text("Test Sentry") }
                    }
                }

                // ── Platform filter ───────────────────────────────────────
                item {
                    PlatformFilterRow(
                        selected = state.platformFilter,
                        onSelect = viewModel::onPlatformFilter
                    )
                }

                // ── Month header + calendar ───────────────────────────────
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
                        onDayClick = { date ->
                            viewModel.onDaySelected(date)
                            onDayClick(date)
                        }
                    )
                }

                // ── Error ─────────────────────────────────────────────────
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

                // ── Esta semana ───────────────────────────────────────────
                if (state.weekReleases.isNotEmpty()) {
                    item { SectionDivider() }
                    item {
                        SectionHeader("Esta semana")
                    }
                    item {
                        WeekRow(
                            releases = state.weekReleases,
                            onGameClick = onGameClick
                        )
                    }
                }

                // ── Próximos destacados ───────────────────────────────────
                if (state.featuredReleases.isNotEmpty()) {
                    item { SectionDivider() }
                    item {
                        SectionHeader("Próximos destacados")
                    }
                    item {
                        FeaturedRow(
                            releases = state.featuredReleases,
                            wishlistedIds = state.wishlistedIds,
                            onWishlistToggle = viewModel::toggleWishlist,
                            onGameClick = onGameClick
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    onSearchClick: () -> Unit,
    onDebugSeed: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search bar (tap-only, navigates to SearchScreen)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(SurfaceVariant, RoundedCornerShape(12.dp))
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = TextHint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Buscar juegos…",
                    fontSize = 14.sp,
                    color = TextHint
                )
            }
            // Notifications
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(SurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            // Debug seed button
            if (onDebugSeed != null) {
                IconButton(onClick = onDebugSeed) {
                    Icon(Icons.Default.BugReport, contentDescription = "Seed", tint = TextHint)
                }
            }
        }
    }
}

// ── Platform filter ───────────────────────────────────────────────────────────

@Composable
private fun PlatformFilterRow(selected: Platform?, onSelect: (Platform?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { PlatformFilterChip("Todos", selected == null) { onSelect(null) } }
        items(Platform.entries) { platform ->
            PlatformFilterChip(platform.displayName, selected == platform) { onSelect(platform) }
        }
    }
}

@Composable
private fun PlatformFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Accent else SurfaceVariant,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

// ── Region filter ─────────────────────────────────────────────────────────────

@Composable
private fun RegionFilterRow(selected: Region?, onSelect: (Region?) -> Unit) {
    val regions = listOf(
        Region.WORLDWIDE, Region.EUROPE, Region.NORTH_AMERICA,
        Region.JAPAN, Region.ASIA, Region.AUSTRALIA, Region.BRAZIL, Region.KOREA
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { RegionFilterChip("Todas", selected == null) { onSelect(null) } }
        items(regions) { region ->
            RegionFilterChip(region.displayName, selected == region) { onSelect(region) }
        }
    }
}

@Composable
private fun RegionFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) AccentDim else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .border(0.5.dp, if (isSelected) Accent else BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Accent else TextHint
        )
    }
}

// ── Month header ──────────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = TextSecondary)
        }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${month.year}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "Mes siguiente", tint = TextSecondary)
        }
    }
}

// ── Calendar grid ─────────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    month: YearMonth,
    releases: List<Release>,
    selectedDay: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val releasesByDay = remember(releases) { releases.groupBy { it.date.dayOfMonth } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = TextHint
                )
            }
        }
        Spacer(Modifier.height(4.dp))

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
    val bg = when {
        isToday    -> Accent
        isSelected -> Surface
        else       -> Color.Transparent
    }
    val borderColor = when {
        isSelected && !isToday -> Accent
        else                   -> Color.Transparent
    }
    val textColor = when {
        isToday -> TextPrimary
        else    -> TextSecondary
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .border(0.5.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 12.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
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
                            .background(platform.toCalendarDotColor())
                    )
                    Spacer(Modifier.width(2.dp))
                }
            }
        }
    }
}

// ── Section helpers ───────────────────────────────────────────────────────────

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        thickness = 0.5.dp,
        color = BorderSubtle
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ── Esta semana ───────────────────────────────────────────────────────────────

@Composable
private fun WeekRow(releases: List<Release>, onGameClick: (Int) -> Unit) {
    val today = LocalDate.now()
    val todayIndex = releases.indexOfFirst { it.date >= today }.coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(releases) {
        if (todayIndex > 0) listState.scrollToItem(todayIndex)
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(releases, key = { it.id }) { release ->
            WeekCard(
                release = release,
                onClick = { onGameClick(release.game.id) }
            )
        }
    }
}

// ── Próximos destacados ───────────────────────────────────────────────────────

@Composable
private fun FeaturedRow(
    releases: List<Release>,
    wishlistedIds: Set<Int>,
    onWishlistToggle: (Release) -> Unit,
    onGameClick: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(releases, key = { it.id }) { release ->
            HeroCard(
                release = release,
                isWishlisted = release.game.id in wishlistedIds,
                onWishlistClick = { onWishlistToggle(release) },
                onClick = { onGameClick(release.game.id) }
            )
        }
    }
}

// ── Debug banner ──────────────────────────────────────────────────────────────

@Composable
private fun DebugInfoBanner(info: String, releaseCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "DEBUG | $info | releases en UI: $releaseCount",
            fontSize = 11.sp,
            color = TextHint
        )
    }
}

// ── Platform dot color helper ─────────────────────────────────────────────────

fun Platform.toCalendarDotColor(): Color = when (this) {
    Platform.STEAM                         -> PlatformSteam
    Platform.PLAYSTATION_5,
    Platform.PLAYSTATION_4                 -> PlatformPS
    Platform.XBOX_SERIES,
    Platform.XBOX_ONE                      -> PlatformXbox
    Platform.NINTENDO_SWITCH               -> PlatformSwitch
}
