package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gamelaunch.domain.model.Release
import com.gamelaunch.ui.theme.*

private val cardShape = RoundedCornerShape(12.dp)

@Composable
fun GameCard(
    release: Release,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val game = release.game

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, cardShape)
            .border(0.5.dp, BorderSubtle, cardShape)
            .clip(cardShape)
            .clickable(onClick = onClick)
    ) {
        // ── Cover image with platform badge ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Platform badge(s) — top-left
            // If game has aggregated platforms (grouped view), show up to 2; else single release platform
            val platformsToShow = game.platforms.ifEmpty { listOf(release.platform) }
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                platformsToShow.take(2).forEach { platform ->
                    PlatformChip(platform = platform, showLabel = false)
                }
                if (platformsToShow.size > 2) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("+${platformsToShow.size - 2}", fontSize = 9.sp, color = TextHint)
                    }
                }
            }
        }

        // ── Info ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = game.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val studio = game.developers.firstOrNull() ?: game.publishers.firstOrNull()
            if (studio != null) {
                Text(
                    text = studio,
                    fontSize = 10.sp,
                    color = TextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (game.rating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "%.1f".format(game.rating / 10f),
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
