package com.gamelaunch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GameLaunchColorScheme = darkColorScheme(
    background          = Background,
    surface             = Surface,
    surfaceVariant      = SurfaceVariant,
    primary             = Accent,
    onPrimary           = TextPrimary,
    onBackground        = TextPrimary,
    onSurface           = TextPrimary,
    onSurfaceVariant    = TextSecondary,
    outline             = BorderSubtle,
    primaryContainer    = AccentDim,
    onPrimaryContainer  = Accent,
)

@Composable
fun GameLaunchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameLaunchColorScheme,
        typography  = GameLaunchTypography,
        content     = content
    )
}
