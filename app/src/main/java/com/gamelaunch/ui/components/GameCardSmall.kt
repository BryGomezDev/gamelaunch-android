package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gamelaunch.domain.model.Game
import com.gamelaunch.ui.theme.SurfaceContainerHigh

@Composable
fun GameCardSmall(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFuture: Boolean = false
) {
    val cardShape = RoundedCornerShape(16.dp)
    val grayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

    Box(
        modifier = modifier
            .size(160.dp)
            .clip(cardShape)
            .clickable(onClick = onClick)
    ) {
        // Shimmer placeholder + image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isFuture) 0.6f else 1f }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(SurfaceContainerHigh))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(game.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                colorFilter = if (isFuture) grayscaleFilter else null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.4f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.60f)
                        )
                    )
                )
        )

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = game.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (game.platforms.isNotEmpty()) {
                PlatformIcon(
                    platform = game.platforms.first(),
                    size = 14.dp,
                    tint = Color.White.copy(alpha = 0.70f)
                )
            }
        }
    }
}
