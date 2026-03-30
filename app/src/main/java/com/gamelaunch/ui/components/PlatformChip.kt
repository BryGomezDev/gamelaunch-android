package com.gamelaunch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.ui.theme.PlatformPS
import com.gamelaunch.ui.theme.PlatformPSBg
import com.gamelaunch.ui.theme.PlatformSteam
import com.gamelaunch.ui.theme.PlatformSteamBg
import com.gamelaunch.ui.theme.PlatformSwitch
import com.gamelaunch.ui.theme.PlatformSwitchBg
import com.gamelaunch.ui.theme.PlatformXbox
import com.gamelaunch.ui.theme.PlatformXboxBg
import com.gamelaunch.ui.theme.SurfaceVariant
import com.gamelaunch.ui.theme.TextSecondary

private data class PlatformColors(val bg: Color, val text: Color)

private fun platformColors(platform: Platform): PlatformColors = when (platform) {
    Platform.STEAM                          -> PlatformColors(PlatformSteamBg, PlatformSteam)
    Platform.PLAYSTATION_5,
    Platform.PLAYSTATION_4                  -> PlatformColors(PlatformPSBg, PlatformPS)
    Platform.XBOX_SERIES,
    Platform.XBOX_ONE                       -> PlatformColors(PlatformXboxBg, PlatformXbox)
    Platform.NINTENDO_SWITCH                -> PlatformColors(PlatformSwitchBg, PlatformSwitch)
}

@Composable
fun PlatformChip(
    platform: Platform,
    modifier: Modifier = Modifier,
    small: Boolean = false
) {
    val colors = platformColors(platform)
    val hPad = if (small) 6.dp else 10.dp
    val vPad = if (small) 2.dp else 4.dp
    val textSize = if (small) 9.sp else 11.sp

    Box(
        modifier = modifier
            .background(colors.bg, RoundedCornerShape(20.dp))
            .padding(horizontal = hPad, vertical = vPad)
    ) {
        Text(
            text = platform.displayName,
            color = colors.text,
            fontSize = textSize,
            maxLines = 1
        )
    }
}

@Composable
fun UnknownPlatformChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SurfaceVariant, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
