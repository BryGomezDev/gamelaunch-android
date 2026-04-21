package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gamelaunch.domain.model.Game
import com.gamelaunch.ui.theme.Background
import com.gamelaunch.ui.theme.OnPrimary
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerHigh

@Composable
fun GameCardLarge(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFuture: Boolean = false,
    statusLabel: String? = null,
    releaseTime: String? = null
) {
    val cardShape = RoundedCornerShape(16.dp)
    val grayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .clip(cardShape)
            .clickable(onClick = onClick)
    ) {
        // Shimmer placeholder + image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isFuture) 0.6f else 1f }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainerHigh)
            )
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
                            0.0f to Color.Transparent,
                            0.3f to Color.Transparent,
                            1.0f to Background
                        )
                    )
                )
        )

        // Platform icons — top-right
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            game.platforms.forEach { platform ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0A0A0A).copy(alpha = 0.60f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PlatformIcon(platform = platform, size = 18.dp, tint = Color.White)
                }
            }
        }

        // Bottom info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (statusLabel != null) {
                Box(
                    modifier = Modifier
                        .background(PrimaryFixed, RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = OnPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Text(
                text = game.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            if (releaseTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = releaseTime,
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
