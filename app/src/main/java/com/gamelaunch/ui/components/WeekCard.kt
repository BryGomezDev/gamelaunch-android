package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
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
        // ── Cover with platform chip overlay ─────────────────────────────
        Box {
            AsyncImage(
                model = release.game.coverUrl,
                contentDescription = release.game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            PlatformChip(
                platform = release.platform,
                small = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            )
        }

        // ── Info ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 5.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
