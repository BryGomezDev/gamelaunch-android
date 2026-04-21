package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gamelaunch.domain.model.Game
import com.gamelaunch.ui.theme.OnSurface
import com.gamelaunch.ui.theme.OnSurfaceVariant
import com.gamelaunch.ui.theme.Primary
import com.gamelaunch.ui.theme.PrimaryFixed
import com.gamelaunch.ui.theme.SurfaceContainerHigh
import com.gamelaunch.ui.theme.SurfaceContainerLow
import java.time.format.DateTimeFormatter

@Composable
fun GameListCard(
    game: Game,
    onClick: () -> Unit,
    statusLabel: String? = null,
    modifier: Modifier = Modifier
) {
    val dateText = remember(game.releaseDate) {
        game.releaseDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(game.coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh)
        )

        // Info column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = game.name,
                color = OnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Platform icons row
            if (game.platforms.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    game.platforms.forEach { platform ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(SurfaceContainerHigh, CircleShape)
                                .border(0.5.dp, PrimaryFixed.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            PlatformIcon(platform = platform, size = 12.dp, tint = Primary)
                        }
                    }
                }
            }

            // Date + status badge row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = dateText,
                        color = OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                if (statusLabel != null) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryFixed.copy(alpha = 0.20f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = Primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
