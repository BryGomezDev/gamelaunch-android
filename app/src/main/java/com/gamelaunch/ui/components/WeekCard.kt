package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        modifier = modifier
            .width(110.dp)
            .background(Surface, weekShape)
            .border(0.5.dp, BorderSubtle, weekShape)
            .clip(weekShape)
            .clickable(onClick = onClick)
    ) {
        // ── Cover ─────────────────────────────────────────────────────────
        AsyncImage(
            model = release.game.coverUrl,
            contentDescription = release.game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )

        // ── Info ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = release.game.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            PlatformChip(platform = release.platform, small = true)
        }
    }
}
