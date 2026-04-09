package com.gamelaunch.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelaunch.R
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

private data class PlatformStyle(
    @DrawableRes val iconRes: Int,
    val fgColor: Color,
    val bgColor: Color,
    val label: String
)

private fun platformStyle(platform: Platform): PlatformStyle = when (platform) {
    Platform.STEAM ->
        PlatformStyle(R.drawable.ic_platform_steam, PlatformSteam, PlatformSteamBg, platform.displayName)
    Platform.PLAYSTATION_5 ->
        PlatformStyle(R.drawable.ic_platform_ps, PlatformPS, PlatformPSBg, platform.displayName)
    Platform.XBOX_SERIES ->
        PlatformStyle(R.drawable.ic_platform_xbox, PlatformXbox, PlatformXboxBg, platform.displayName)
    Platform.NINTENDO_SWITCH ->
        PlatformStyle(R.drawable.ic_platform_switch, PlatformSwitch, PlatformSwitchBg, platform.displayName)
}

@Composable
fun PlatformChip(
    platform: Platform,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val style = platformStyle(platform)

    if (showLabel) {
        Row(
            modifier = modifier
                .background(style.bgColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                painter = painterResource(style.iconRes),
                contentDescription = null,
                tint = style.fgColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = style.label,
                color = style.fgColor,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    } else {
        // Icon-only chip for card overlays — always white on semi-transparent bg
        Box(
            modifier = modifier
                .size(26.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(style.iconRes),
                contentDescription = style.label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
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
