package com.gamelaunch.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.ui.components.KronosBottomNav
import com.gamelaunch.ui.components.PlatformIcon
import com.gamelaunch.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    gameId: Int,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onGameClick: (Int) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(gameId) { viewModel.loadGame(gameId) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A).copy(alpha = 0.40f),
                    titleContentColor = PrimaryFixed
                ),
                title = {
                    Text(
                        text = "Game Detail",
                        fontFamily = ManropeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryFixed
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleWishlist) {
                        Icon(
                            imageVector = if (state.isWishlisted) Icons.Filled.Bookmark
                                          else Icons.Outlined.BookmarkAdd,
                            contentDescription = if (state.isWishlisted) "Quitar bookmark" else "Guardar",
                            tint = PrimaryFixed
                        )
                    }
                }
            )
        },
        bottomBar = {
            KronosBottomNav(
                currentRoute = "",
                onNavigate = { route -> if (route == "home") onNavigateHome() }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryFixed)
            }

            state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            state.game != null -> {
                val game = state.game!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentPadding = PaddingValues(
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    )
                ) {
                    item { HeroSection(game = game) }

                    item {
                        BentoStatsGrid(
                            game = game,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp)
                        )
                    }

                    item {
                        SobreElJuego(
                            game = game,
                            state = state,
                            onToggleTranslation = viewModel::toggleSummaryLanguage,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 24.dp)
                        )
                    }

                    if (game.screenshots.isNotEmpty()) {
                        item {
                            CapturasSection(
                                screenshots = game.screenshots,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Hero section ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(game: Game) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        AsyncImage(
            model = game.coverUrl,
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient vertical (bottom fade)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF121414).copy(alpha = 0.40f),
                            Color(0xFF121414)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Gradient horizontal (left fade)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF121414).copy(alpha = 0.80f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rating badge
            val hasRating = (game.rating ?: 0f) > 0f
            if (hasRating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = PrimaryFixed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "%.1f".format((game.rating ?: 0f) / 10f),
                        color = PrimaryFixed,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Title — primera palabra blanca, resto PrimaryFixed italic
            val words = game.name.split(" ")
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) {
                        append(words.first())
                    }
                    if (words.size > 1) {
                        append(" ")
                        withStyle(
                            SpanStyle(
                                color = PrimaryFixed,
                                fontStyle = FontStyle.Italic
                            )
                        ) {
                            append(words.drop(1).joinToString(" "))
                        }
                    }
                },
                fontFamily = ManropeFamily,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                lineHeight = 52.sp,
                maxLines = 2
            )

            // Genre + date chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                game.genres.firstOrNull()?.let { genre ->
                    DetailChip(text = genre)
                }
                DetailChip(text = game.releaseDate.format(dateFormatter))
            }
        }
    }
}

// ── Bento stats grid ─────────────────────────────────────────────────────────

@Composable
private fun BentoStatsGrid(
    game: Game,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Platforms — full width
        if (game.platforms.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PLATAFORMAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        game.platforms.forEach { platform ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, OutlineVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PlatformIcon(
                                        platform = platform,
                                        size = 24.dp,
                                        tint = OnSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = platform.displayName,
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metascore + Release date
        val hasRating = (game.rating ?: 0f) > 0f
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (hasRating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "METASCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = ((game.rating ?: 0f) / 10f).toInt().toString(),
                            fontFamily = ManropeFamily,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryFixed
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryFixed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LANZAMIENTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnPrimary.copy(alpha = 0.60f),
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    val month = game.releaseDate
                        .format(DateTimeFormatter.ofPattern("MMM", Locale("es")))
                        .uppercase()
                    val year = game.releaseDate.year.toString()
                    Text(
                        text = "$month\n$year",
                        fontFamily = ManropeFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = OnPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

// ── Sobre el juego ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SobreElJuego(
    game: Game,
    state: DetailUiState,
    onToggleTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = if (!state.showOriginalSummary && game.summaryEs != null) {
        game.summaryEs
    } else {
        game.summary ?: ""
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SOBRE EL JUEGO",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryFixed,
            letterSpacing = 3.sp
        )

        Text(
            text = description,
            fontFamily = ManropeFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = OnSurface.copy(alpha = 0.70f),
            lineHeight = 22.sp
        )

        if (state.language == "es" && game.summary != null) {
            TextButton(onClick = onToggleTranslation) {
                Text(
                    text = if (!state.showOriginalSummary) "Ver original" else "Ver traducido",
                    color = PrimaryFixed,
                    fontSize = 12.sp
                )
            }
        }

        if (game.genres.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                game.genres.forEach { genre ->
                    Text(
                        text = genre.uppercase(),
                        fontFamily = ManropeFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryFixed,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(SurfaceContainer, RoundedCornerShape(50.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ── Capturas de pantalla ─────────────────────────────────────────────────────

@Composable
private fun CapturasSection(
    screenshots: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "CAPTURAS DE PANTALLA",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            letterSpacing = 3.sp
        )

        AsyncImage(
            model = screenshots[0],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
        )

        if (screenshots.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                screenshots.drop(1).take(2).forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    }
}

// ── Chip helper ──────────────────────────────────────────────────────────────

@Composable
private fun DetailChip(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = OnSurfaceVariant,
        modifier = Modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}
