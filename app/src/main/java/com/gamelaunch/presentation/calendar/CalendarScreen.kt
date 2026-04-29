package com.gamelaunch.presentation.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gamelaunch.BuildConfig
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.ui.components.CalendarFab
import com.gamelaunch.ui.components.GameCardLarge
import com.gamelaunch.ui.components.GameCardSmall
import com.gamelaunch.ui.components.HeroCardSkeleton
import com.gamelaunch.ui.components.KronosBottomNav
import com.gamelaunch.ui.components.KronosDrawer
import com.gamelaunch.ui.components.KronosTopAppBar
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.OnPrimary
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerHigh
import com.gamelaunch.ui.theme.SurfaceContainerLowest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Extensión: agrupa releases en Map<LocalDate, List<Game>> para los 14 días ──

private fun List<Release>.toTimelineGames(today: LocalDate): Map<LocalDate, List<Game>> {
    val end = today.plusDays(13)
    return this
        .filter { it.date in today..end }
        .groupBy { it.date }
        .mapValues { (_, releases) ->
            releases
                .groupBy { it.game.id }
                .map { (_, gameReleases) ->
                    val merged = gameReleases.map { it.platform }.distinct()
                    gameReleases.first().game.copy(platforms = merged)
                }
                .sortedByDescending { it.rating ?: 0f }
        }
        .toSortedMap()
}

// ── Pantalla principal ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    onGameClick: (Int) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    onSearchClick: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onAvatarClick: () -> Unit = {},
    currentRoute: String = "home",
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val today = remember { LocalDate.now() }
    var showCalendar by remember { mutableStateOf(false) }

    val timelineDays = remember { (0L..13L).map { today.plusDays(it) } }
    val timelineGames = remember(state.timelineReleases) { state.timelineReleases.toTimelineGames(today) }

    // Pre-calculamos los índices de scroll para cada día con juegos
    val dayScrollIndices: Map<LocalDate, Int> = remember(timelineGames, state.error) {
        var idx = if (BuildConfig.DEBUG) 1 else 0   // debug banner
        idx += 2                                     // featured label + featured row
        if (state.error != null) idx++
        buildMap {
            for (date in timelineDays) {
                val games = timelineGames[date]
                if (!games.isNullOrEmpty()) {
                    put(date, idx)
                    idx += 2  // header + games
                }
            }
        }
    }

    KronosDrawer(drawerState = drawerState, currentRoute = currentRoute, onNavigate = onNavigate) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
            topBar = {
                KronosTopAppBar(
                    onMenuClick = { coroutineScope.launch { drawerState.open() } },
                    onSearchClick = onSearchClick,
                    onAvatarClick = onAvatarClick
                )
            },
            ) { padding ->
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // ── Debug ─────────────────────────────────────────────
                    if (BuildConfig.DEBUG) {
                        item(key = "debug") {
                            DebugInfoBanner(state.syncDebugInfo, state.releases.size)
                        }
                        item(key = "debug_seed") {
                            androidx.compose.material3.TextButton(
                                onClick = viewModel::seedData,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) { Text("Seed Data") }
                        }
                    }

                    // ── Sección: Grandes Lanzamientos ─────────────────────
                    item(key = "featured_label") {
                        Text(
                            text = "DESTACADOS · ÚLTIMOS 7 DÍAS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryFixed.copy(alpha = 0.70f),
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    item(key = "featured_row") {
                        if (state.isRefreshing && state.featuredReleases.isEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(3) { HeroCardSkeleton() }
                            }
                        } else {
                            BigHeroCardsRow(
                                releases = state.featuredReleases,
                                onGameClick = onGameClick,
                                language = state.language
                            )
                        }
                    }

                    // ── Error ─────────────────────────────────────────────
                    state.error?.let { err ->
                        item(key = "error") {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // ── Timeline 14 días ──────────────────────────────────
                    timelineDays.forEach { date ->
                        val games = timelineGames[date]
                        if (games.isNullOrEmpty()) return@forEach
                        val isToday = date == today

                        item(key = "header_$date") {
                            TimelineDayHeader(date = date, isToday = isToday)
                        }
                        item(key = "games_$date") {
                            TimelineDayGames(
                                games = games,
                                isToday = isToday,
                                onGameClick = onGameClick
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }

        // Pill flotante — posicionado por la pantalla
        KronosBottomNav(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        CalendarFab(
            onClick = { showCalendar = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
        )
    }
    }

    // ── BottomSheet de calendario ──────────────────────────────────────────────
    if (showCalendar) {
        CalendarBottomSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = { showCalendar = false },
            onDateSelected = { date ->
                showCalendar = false
                val scrollIdx = dayScrollIndices[date]
                if (scrollIdx != null) {
                    coroutineScope.launch { listState.animateScrollToItem(scrollIdx) }
                } else {
                    onDayClick(date)
                }
            }
        )
    }
}

// ── Big Hero Cards (Grandes Lanzamientos) ─────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BigHeroCardsRow(
    releases: List<Release>,
    onGameClick: (Int) -> Unit,
    language: String
) {
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp * 0.85).dp
    val heroListState = rememberLazyListState()
    val snappingLayout = rememberSnapFlingBehavior(heroListState)

    LazyRow(
        state = heroListState,
        flingBehavior = snappingLayout,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(releases, key = { it.id }) { release ->
            BigHeroCard(
                release = release,
                onClick = { onGameClick(release.game.id) },
                modifier = Modifier.width(cardWidth),
                language = language
            )
        }
    }
}

@Composable
private fun BigHeroCard(
    release: Release,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    language: String = "es"
) {
    val today = LocalDate.now()
    val game = release.game

    val statusBadge = release.date
        .format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
        .uppercase(Locale.getDefault())
    val platformBadge = when (game.platforms.size) {
        0    -> null
        1    -> game.platforms.first().displayName
        else -> "${game.platforms.size} plataformas"
    }
    val description = if (language == "es") game.summaryEs else game.summary

    Box(
        modifier = modifier
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceContainerHigh))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(game.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f))
                    )
                )
        )

        // Bottom content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (statusBadge != null) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryFixed, RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusBadge,
                            color = OnPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                if (platformBadge != null) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = platformBadge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Text(
                text = game.name,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (description != null) {
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Timeline ──────────────────────────────────────────────────────────────────

@Composable
private fun TimelineDayHeader(
    date: LocalDate,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot area (40dp wide — alineado con el indent de los juegos)
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isToday) {
                // Aro exterior semitransparente
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, PrimaryFixed.copy(alpha = 0.20f), CircleShape)
                )
                // Punto central sólido
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(PrimaryFixed, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF4B5563), CircleShape)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        if (isToday) {
            Text(
                text = "HOY",
                color = PrimaryFixed,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = date.format(
                    DateTimeFormatter.ofPattern("d 'DE' MMMM", locale)
                ).uppercase(locale),
                color = OnSurfaceVariant,
                fontSize = 14.sp
            )
        } else {
            Text(
                text = date.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, locale)
                    .uppercase(locale),
                color = Color(0xFF9CA3AF),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("d MMM", locale)),
                color = OnSurfaceVariant.copy(alpha = 0.60f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TimelineDayGames(
    games: List<Game>,
    isToday: Boolean,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp)
    ) {
        GameCardLarge(
            game = games.first(),
            onClick = { onGameClick(games.first().id) },
            isFuture = !isToday,
            statusLabel = if (isToday) "HOY" else null
        )

        if (games.size > 1) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(games.drop(1)) { game ->
                    GameCardSmall(
                        game = game,
                        onClick = { onGameClick(game.id) },
                        isFuture = !isToday
                    )
                }
            }
        }
    }
}

// ── Calendar BottomSheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarBottomSheet(
    state: CalendarUiState,
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceContainerLowest
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            MonthHeader(
                month = state.currentMonth,
                onPrev = { viewModel.onMonthChange(state.currentMonth.minusMonths(1)) },
                onNext = { viewModel.onMonthChange(state.currentMonth.plusMonths(1)) }
            )
            CalendarGrid(
                month = state.currentMonth,
                releases = state.releases,
                selectedDay = state.selectedDay,
                onDayClick = { date ->
                    viewModel.onDaySelected(date)
                    onDateSelected(date)
                }
            )
        }
    }
}

// ── Month header (también usado en BottomSheet) ───────────────────────────────

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
            Icon(Icons.Default.ChevronLeft, "Mes anterior", tint = OnSurfaceVariant)
        }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }} ${month.year}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.gamelaunch.ui.theme.OnSurface
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "Mes siguiente", tint = OnSurfaceVariant)
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
                    color = OnSurfaceVariant.copy(alpha = 0.60f)
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
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryFixed else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "dayCellBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isToday && !isSelected) PrimaryFixed.copy(alpha = 0.75f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "dayCellBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dayCellScale"
    )
    val textColor = when {
        isSelected            -> com.gamelaunch.ui.theme.OnSurface
        isToday               -> PrimaryFixed
        releases.isNotEmpty() -> com.gamelaunch.ui.theme.OnSurface
        else                  -> OnSurfaceVariant
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
        if (distinctPlatforms.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
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
                                if (isSelected) platform.toCalendarDotColor().copy(alpha = 0.9f)
                                else platform.toCalendarDotColor()
                            )
                    )
                }
            }
        }
    }
}

// ── Debug banner ──────────────────────────────────────────────────────────────

@Composable
private fun DebugInfoBanner(info: String, releaseCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "DEBUG | $info | releases: $releaseCount",
            fontSize = 11.sp,
            color = OnSurfaceVariant
        )
    }
}

// ── Platform dot color ─────────────────────────────────────────────────────────

fun Platform.toCalendarDotColor(): Color = when (this) {
    Platform.STEAM           -> com.gamelaunch.ui.theme.PlatformSteam
    Platform.PLAYSTATION_5   -> com.gamelaunch.ui.theme.PlatformPS
    Platform.XBOX_SERIES     -> com.gamelaunch.ui.theme.PlatformXbox
    Platform.NINTENDO_SWITCH -> com.gamelaunch.ui.theme.PlatformSwitch
}
