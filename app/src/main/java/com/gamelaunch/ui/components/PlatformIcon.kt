package com.gamelaunch.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gamelaunch.domain.model.Platform

@Composable
fun PlatformIcon(
    platform: Platform,
    size: Dp = 24.dp,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(platform.iconRes),
        contentDescription = platform.displayName,
        tint = tint,
        modifier = modifier.size(size)
    )
}
