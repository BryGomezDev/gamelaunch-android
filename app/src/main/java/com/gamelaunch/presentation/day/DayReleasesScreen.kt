package com.gamelaunch.presentation.day

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.ui.components.GameCard
import com.gamelaunch.ui.components.PlatformChip
import com.gamelaunch.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.gamelaunch.R
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
                EmptyDayState()
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
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
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
                    text = "$releaseCount ${stringResource(R.string.releases_today)}",
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
                            showLabel = false,
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

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyDayState() {
    // Anillo animado decorativo (pulso sutil)
    val infiniteTransition = rememberInfiniteTransition(label = "emptyPulse")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyArc"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        // Icono vectorial compuesto: calendario vacío con arco animado
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp)
        ) {
            // Círculo de fondo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(AccentDim, CircleShape)
            )
            // Arco animado decorativo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .drawBehind {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Accent.copy(alpha = 0f),
                                    Accent.copy(alpha = 0.6f),
                                    Accent.copy(alpha = 0f)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
            )
            // Icono de calendario vectorial (DrawScope API pura de Compose)
            androidx.compose.foundation.Canvas(modifier = Modifier.size(44.dp)) {
                val strokeW = 2.dp.toPx()
                val cornerR = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                val accentColor = Accent

                // Cuerpo del calendario (borde)
                drawRoundRect(
                    color = accentColor,
                    topLeft = androidx.compose.ui.geometry.Offset(strokeW, 8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - strokeW * 2,
                        size.height - 8.dp.toPx() - strokeW
                    ),
                    cornerRadius = cornerR,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )

                // Cabecera rellena (franja superior)
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.35f),
                    topLeft = androidx.compose.ui.geometry.Offset(strokeW, 8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - strokeW * 2,
                        8.dp.toPx()
                    ),
                    cornerRadius = cornerR
                )

                // Asas superiores izquierda y derecha
                drawLine(
                    color = accentColor,
                    start = androidx.compose.ui.geometry.Offset(12.dp.toPx(), 4.dp.toPx()),
                    end   = androidx.compose.ui.geometry.Offset(12.dp.toPx(), 12.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = androidx.compose.ui.geometry.Offset(32.dp.toPx(), 4.dp.toPx()),
                    end   = androidx.compose.ui.geometry.Offset(32.dp.toPx(), 12.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )

                // Guión central "sin datos"
                drawLine(
                    color = accentColor.copy(alpha = 0.7f),
                    start = androidx.compose.ui.geometry.Offset(14.dp.toPx(), 29.dp.toPx()),
                    end   = androidx.compose.ui.geometry.Offset(30.dp.toPx(), 29.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Texto principal
        Text(
            text = "Sin lanzamientos este día",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        // Subtexto
        Text(
            text = "Prueba con otra fecha del calendario",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
