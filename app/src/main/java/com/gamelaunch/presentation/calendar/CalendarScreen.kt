package com.gamelaunch.presentation.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.BuildConfig
import com.gamelaunch.R
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.ui.components.HeroCard
import com.gamelaunch.ui.components.HeroCardSkeleton
import com.gamelaunch.ui.components.WeekCard
import com.gamelaunch.ui.components.WeekCardSkeleton
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
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) { Text("Test Sentry") }
                    }
                }

                // ── Platform filter ───────────────────────────────────────
                item {
                    PlatformFilterRow(
                        selected = state.platformFilters,
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
                if (state.isRefreshing || state.weekReleases.isNotEmpty()) {
                    item { SectionDivider() }
                    item { SectionHeader(stringResource(R.string.this_week)) }
                    item {
                        if (state.isRefreshing && state.weekReleases.isEmpty()) {
                            WeekSkeletonRow()
                        } else {
                            WeekRow(
                                releases = state.weekReleases,
                                onGameClick = onGameClick
                            )
                        }
                    }
                }

                // ── Próximos destacados ───────────────────────────────────
                if (state.isRefreshing || state.featuredReleases.isNotEmpty()) {
                    item { SectionDivider() }
                    item { SectionHeader(stringResource(R.string.featured)) }
                    item {
                        if (state.isRefreshing && state.featuredReleases.isEmpty()) {
                            FeaturedSkeletonRow()
                        } else {
                            FeaturedRow(
                                releases = state.featuredReleases,
                                wishlistedIds = state.wishlistedIds,
                                onWishlistToggle = viewModel::toggleWishlist,
                                onGameClick = onGameClick
                            )
                        }
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
        // ── Brand ─────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Text(
                text = "KRONOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = Color.White
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "· Gamer's Planner",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF888888)
            )
        }

        // ── Search row ────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                Text(text = "Buscar juegos…", fontSize = 14.sp, color = TextHint)
            }
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
private fun PlatformFilterRow(selected: Set<Platform>, onSelect: (Platform?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { PlatformFilterChip("Todos", isSelected = selected.isEmpty(), iconRes = null) { onSelect(null) } }
        items(Platform.entries) { platform ->
            PlatformFilterChip(
                label = platform.displayName,
                isSelected = platform in selected,
                iconRes = platform.iconRes
            ) { onSelect(platform) }
        }
    }
}

@Composable
private fun PlatformFilterChip(
    label: String,
    isSelected: Boolean,
    iconRes: Int?,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) TextPrimary else TextSecondary
    Row(
        modifier = Modifier
            .background(
                if (isSelected) Accent else SurfaceVariant,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

// ── Month header ──────────────────────────────────────────────────────────────

@Composable
private fun MonthHeader(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    val locale = Locale.getDefault()
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
            text = "${month.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }} ${month.year}",
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
    val locale = Locale.getDefault()
    val daysOfWeek = if (locale.language == "es")
        listOf("L", "M", "X", "J", "V", "S", "D")
    else
        listOf("M", "T", "W", "T", "F", "S", "S")

    // Monday-first offset: MONDAY=1→0, TUESDAY=2→1, ..., SUNDAY=7→6
    val firstDayOffset = (month.atDay(1).dayOfWeek.value - 1) % 7
    val daysInMonth = month.lengthOfMonth()
    val releasesByDay = remember(releases) { releases.groupBy { it.date.dayOfMonth } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { label ->
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
    // ── Animaciones ──────────────────────────────────────────────────────────
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> Accent
            else       -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "dayCellBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isToday && !isSelected -> Accent.copy(alpha = 0.75f)
            else                   -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "dayCellBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dayCellScale"
    )
    val textColor = when {
        isSelected          -> TextPrimary
        isToday             -> Accent
        releases.isNotEmpty() -> TextPrimary
        else                -> TextSecondary
    }
    val fontWeight = when {
        isSelected || isToday -> FontWeight.Bold
        releases.isNotEmpty() -> FontWeight.SemiBold
        else                  -> FontWeight.Normal
    }

    val distinctPlatforms = remember(releases) { releases.map { it.platform }.distinct() }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .border(
                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 12.sp,
            fontWeight = fontWeight,
            color = textColor,
            textAlign = TextAlign.Center
        )

        // ── Indicadores de plataforma ─────────────────────────────────────
        if (distinctPlatforms.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            if (distinctPlatforms.size <= 3) {
                // Puntos de color por plataforma
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    distinctPlatforms.forEach { platform ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        platform.toCalendarDotColor().copy(alpha = 0.9f)
                                    else
                                        platform.toCalendarDotColor()
                                )
                        )
                    }
                }
            } else {
                // Más de 3 plataformas: mostrar solo las 3 primeras como puntos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    distinctPlatforms.take(3).forEach { platform ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected)
                                        platform.toCalendarDotColor().copy(alpha = 0.9f)
                                    else
                                        platform.toCalendarDotColor()
                                )
                        )
                    }
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

// ── Skeleton rows ─────────────────────────────────────────────────────────────

@Composable
private fun WeekSkeletonRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(5) {
            com.gamelaunch.ui.components.WeekCardSkeleton()
        }
    }
}

@Composable
private fun FeaturedSkeletonRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(4) {
            com.gamelaunch.ui.components.HeroCardSkeleton()
        }
    }
}

// ── Platform dot color helper ─────────────────────────────────────────────────

fun Platform.toCalendarDotColor(): Color = when (this) {
    Platform.STEAM           -> PlatformSteam
    Platform.PLAYSTATION_5   -> PlatformPS
    Platform.XBOX_SERIES     -> PlatformXbox
    Platform.NINTENDO_SWITCH -> PlatformSwitch
}
