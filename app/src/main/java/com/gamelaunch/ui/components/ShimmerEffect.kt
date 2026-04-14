package com.gamelaunch.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.gamelaunch.ui.theme.BorderSubtle
import com.gamelaunch.ui.theme.Surface
import com.gamelaunch.ui.theme.SurfaceVariant

/**
 * Devuelve un [Brush] de gradiente deslizante que simula el efecto shimmer.
 * Basado en [InfiniteTransition]: no requiere dependencias externas.
 */
@Composable
fun shimmerBrush(
    widthOfShadowBrush: Float = 500f,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1200
): Brush {
    val shimmerColors = listOf(
        SurfaceVariant,
        SurfaceVariant.copy(alpha = 0.9f),
        Surface.copy(alpha = 0.4f),       // highlight central más claro
        SurfaceVariant.copy(alpha = 0.9f),
        SurfaceVariant,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation - widthOfShadowBrush, y = 0f),
        end   = Offset(x = translateAnimation, y = angleOfAxisY),
    )
}

// ── Skeleton para GameCard (2-column grid) ────────────────────────────────────

@Composable
fun GameCardSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface)
    ) {
        // Cover placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(brush)
        )
        // Info area
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title line — ancho completo
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            // Title line 2 — ancho parcial
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            // Studio / rating
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

// ── Skeleton para WeekCard (horizontal row) ──────────────────────────────────

@Composable
fun WeekCardSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .width(110.dp)
            .clip(shape)
            .background(Surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(brush)
        )
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 5.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

// ── Skeleton para HeroCard (featured row) ────────────────────────────────────

@Composable
fun HeroCardSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .width(150.dp)
            .height(200.dp)
            .clip(shape)
            .background(brush)
    )
}

// ── Skeleton para celda del calendario ───────────────────────────────────────

@Composable
fun CalendarDaySkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush(widthOfShadowBrush = 200f, durationMillis = 1000)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(brush)
    )
}
