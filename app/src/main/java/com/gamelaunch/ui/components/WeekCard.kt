package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val weekShape = RoundedCornerShape(12.dp)

@Composable
fun WeekCard(
    release: Release,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platforms = release.game.platforms.ifEmpty { listOf(release.platform) }

    Column(
        modifier = modifier
            .width(110.dp)
            .background(Surface, weekShape)
            .border(0.5.dp, BorderSubtle, weekShape)
            .clip(weekShape)
            .clickable(onClick = onClick)
    ) {
        // ── Cover with platform chips overlay ─────────────────────────────────
        Box {
            AsyncImage(
                model = release.game.coverUrl,
                contentDescription = release.game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                platforms.take(2).forEach { platform ->
                    PlatformChip(platform = platform, small = true)
                }
                if (platforms.size > 2) {
                    Text(
                        text = "+${platforms.size - 2}",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .background(SurfaceVariant.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // ── Info ──────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 5.dp, bottom = 10.dp)
        ) {
            Text(
                text = release.game.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
