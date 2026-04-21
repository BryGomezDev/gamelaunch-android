package com.gamelaunch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KronosColorScheme = darkColorScheme(
    background            = Background,
    surface               = Surface,
    surfaceContainer      = SurfaceContainer,
    surfaceContainerHigh  = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow   = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,
    primary               = Primary,
    primaryContainer      = PrimaryFixed,
    onPrimary             = OnPrimary,
    onPrimaryContainer    = OnPrimary,
    onSurface             = OnSurface,
    onSurfaceVariant      = OnSurfaceVariant,
    outlineVariant        = OutlineVariant,
    onBackground          = OnSurface,
)

@Composable
fun GameLaunchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KronosColorScheme,
        typography  = KronosTypography,
        content     = content
    )
}
