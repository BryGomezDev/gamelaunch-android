package com.gamelaunch.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.SimilarGame
import com.gamelaunch.ui.components.PlatformChip
import com.gamelaunch.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DetailScreen(
    gameId: Int,
    onBack: () -> Unit,
    onGameClick: (Int) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(gameId) { viewModel.loadGame(gameId) }
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Accent) }

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
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
                GameDetailContent(
                    game = state.game!!,
                    isWishlisted = state.isWishlisted,
                    notifyDaysAhead = state.notifyDaysAhead,
                    onNotifyDaysSelected = viewModel::setNotifyDaysAhead,
                    isTranslating = state.isTranslating,
                    showOriginalSummary = state.showOriginalSummary,
                    onToggleSummaryLanguage = viewModel::toggleSummaryLanguage,
                    onGameClick = onGameClick
                )
                // Overlaid nav buttons — always on top
                OverlayNavButtons(
                    isWishlisted = state.isWishlisted,
                    onBack = onBack,
                    onToggleWishlist = viewModel::toggleWishlist
                )
            }
        }
    }
}

// ── Overlay back + wishlist buttons ──────────────────────────────────────────

@Composable
private fun OverlayNavButtons(
    isWishlisted: Boolean,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onToggleWishlist),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isWishlisted) "Quitar de lista" else "Añadir a lista",
                tint = if (isWishlisted) Accent else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Main scrollable content ───────────────────────────────────────────────────

@Composable
private fun GameDetailContent(
    game: Game,
    isWishlisted: Boolean,
    notifyDaysAhead: Int?,
    onNotifyDaysSelected: (Int) -> Unit,
    isTranslating: Boolean,
    showOriginalSummary: Boolean,
    onToggleSummaryLanguage: () -> Unit,
    onGameClick: (Int) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es")) }
    var showNotifyOptions by remember { mutableStateOf(notifyDaysAhead != null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero area ─────────────────────────────────────────────────────
        HeroArea(game = game, dateFormatter = dateFormatter)

        // ── Body ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Platform chips
            if (game.platforms.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(game.platforms) { platform ->
                        PlatformChip(platform = platform)
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Mi lista — outline
                OutlinedButton(
                    onClick = { /* wishlist handled by overlay button */ },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(if (isWishlisted) "En mi lista" else "Mi lista", fontSize = 13.sp)
                }
                // Notificarme — filled
                Button(
                    onClick = { showNotifyOptions = !showNotifyOptions },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text(
                        if (notifyDaysAhead != null) "Notif. $notifyDaysAhead d." else "Notificarme",
                        fontSize = 13.sp
                    )
                }
            }

            // Notify day chips (expandable)
            if (showNotifyOptions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 3, 7).forEach { days ->
                        val isSelected = notifyDaysAhead == days
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Accent else SurfaceVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onNotifyDaysSelected(days) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                "$days día${if (days > 1) "s" else ""} antes",
                                fontSize = 12.sp,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = BorderSubtle)

            // Description
            if (!game.summary.isNullOrBlank()) {
                DescriptionSection(
                    game = game,
                    isTranslating = isTranslating,
                    showOriginalSummary = showOriginalSummary,
                    onToggleSummaryLanguage = onToggleSummaryLanguage
                )
            }

            // Info: desarrollador, distribuidor
            if (game.developers.isNotEmpty() || game.publishers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (game.developers.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Default.Code,
                            label = if (game.developers.size > 1) "Desarrolladores" else "Desarrolladora",
                            value = game.developers.joinToString(", ")
                        )
                    }
                    if (game.publishers.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Default.Business,
                            label = if (game.publishers.size > 1) "Distribuidoras" else "Distribuidora",
                            value = game.publishers.joinToString(", ")
                        )
                    }
                }
            }

            // Tags: modo de juego / géneros + temas
            if (game.gameModes.isNotEmpty()) {
                TagsSection(label = "Modo de juego", tags = game.gameModes)
            }
            val etiquetas = (game.genres + game.themes).distinct()
            if (etiquetas.isNotEmpty()) {
                TagsSection(label = "Etiquetas", tags = etiquetas)
            }

            // Website link
            game.websiteUrl?.let { url ->
                Row(
                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sitio web oficial →", fontSize = 13.sp, color = Accent)
                }
            }
        }

        // Related games LazyRow
        if (game.similarGames.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionLabel("Juegos relacionados", modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            RelatedGamesRow(games = game.similarGames, onGameClick = onGameClick)
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Hero area (auto-sliding carousel) ────────────────────────────────────────

@Composable
private fun HeroArea(
    game: Game,
    dateFormatter: DateTimeFormatter
) {
    val images = game.screenshots.ifEmpty { listOfNotNull(game.coverUrl) }
    val pagerState = rememberPagerState { images.size }

    if (images.size > 1) {
        LaunchedEffect(pagerState) {
            while (true) {
                delay(4_000)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % images.size)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Sliding screenshots
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                    )
                )
        )

        // Page dots
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 7.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }

        // Info row at bottom of hero
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mini cover
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, Accent, RoundedCornerShape(8.dp))
            )

            // Title + info
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val studio = game.developers.firstOrNull() ?: game.publishers.firstOrNull()
                val dateStr = game.releaseDate.format(dateFormatter)
                Text(
                    text = if (studio != null) "$dateStr · $studio" else dateStr,
                    fontSize = 11.sp,
                    color = TextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                game.rating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = StarColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "%.1f".format(rating / 10f),
                            fontSize = 12.sp,
                            color = StarColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/ 10",
                            fontSize = 11.sp,
                            color = TextHint
                        )
                    }
                }
            }
        }
    }
}

// ── Description section ───────────────────────────────────────────────────────

@Composable
private fun DescriptionSection(
    game: Game,
    isTranslating: Boolean,
    showOriginalSummary: Boolean,
    onToggleSummaryLanguage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionLabel("Descripción")
            when {
                isTranslating -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Accent)
                    Text("Traduciendo…", fontSize = 11.sp, color = TextHint)
                }
                game.summaryEs != null -> TextButton(
                    onClick = onToggleSummaryLanguage,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        if (showOriginalSummary) "Ver en español" else "Ver original",
                        fontSize = 11.sp,
                        color = Accent
                    )
                }
            }
        }
        val text = if (!showOriginalSummary && game.summaryEs != null) game.summaryEs else game.summary
        Text(
            text = text ?: "",
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

// ── Info row (icon + label + value) ──────────────────────────────────────────

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextHint,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Text(
            text = "$label: ",
            fontSize = 12.sp,
            color = TextHint
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Tags section (label + chips) ─────────────────────────────────────────────

@Composable
private fun TagsSection(label: String, tags: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(label, modifier = Modifier.padding(horizontal = 0.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tags) { tag ->
                Box(
                    modifier = Modifier
                        .background(SurfaceVariant, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(text = tag, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

// ── Related games LazyRow ─────────────────────────────────────────────────────

@Composable
private fun RelatedGamesRow(games: List<SimilarGame>, onGameClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(games) { game ->
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .clickable { onGameClick(game.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AsyncImage(
                    model = game.coverUrl,
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(100.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceVariant)
                )
                Text(
                    text = game.name,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextHint,
        letterSpacing = 0.08.em,
        modifier = modifier
    )
}
