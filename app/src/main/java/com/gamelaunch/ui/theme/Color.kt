package com.gamelaunch.ui.theme

import androidx.compose.ui.graphics.Color

// ── Fondos / Superficies ──────────────────────────────────────────────────────
val Background              = Color(0xFF121414)
val Surface                 = Color(0xFF121414)
val SurfaceContainer        = Color(0xFF1E2020)
val SurfaceContainerHigh    = Color(0xFF282A2A)
val SurfaceContainerHighest = Color(0xFF333535)
val SurfaceContainerLow     = Color(0xFF1A1C1C)
val SurfaceContainerLowest  = Color(0xFF0D0F0F)

// ── Primario ──────────────────────────────────────────────────────────────────
val Primary        = Color(0xFF4AF2A1)
val PrimaryFixed   = Color(0xFF59FEAC)
val PrimaryFixedDim = Color(0xFF31E192)
val OnPrimary      = Color(0xFF003920)

// ── Texto / Contenido ─────────────────────────────────────────────────────────
val OnSurface        = Color(0xFFE2E2E2)
val OnSurfaceVariant = Color(0xFFBBCBBD)
val OutlineVariant   = Color(0xFF3C4A40)

// ── Plataformas ───────────────────────────────────────────────────────────────
val PlatformSteam    = Color(0xFF5BB3E8)
val PlatformSteamBg  = Color(0xFF1B3F5C)
val PlatformPS       = Color(0xFF6A9AE8)
val PlatformPSBg     = Color(0xFF001A4D)
val PlatformXbox     = Color(0xFF5DBD5D)
val PlatformXboxBg   = Color(0xFF0A2A0A)
val PlatformSwitch   = Color(0xFFE84040)
val PlatformSwitchBg = Color(0xFF3D0000)

// ── Misc ──────────────────────────────────────────────────────────────────────
val StarColor = Color(0xFFF5C518)

// ── Aliases de compatibilidad (tokens del sistema anterior) ───────────────────
// Se eliminarán en el paso de migración de pantallas
@Deprecated("Usa Primary", ReplaceWith("Primary"))
val Accent = Primary
@Deprecated("Usa PrimaryFixed", ReplaceWith("PrimaryFixed"))
val AccentDim = Color(0xFF0D3D25)
@Deprecated("Usa OutlineVariant", ReplaceWith("OutlineVariant"))
val BorderSubtle = OutlineVariant
@Deprecated("Usa SurfaceContainerHigh", ReplaceWith("SurfaceContainerHigh"))
val SurfaceVariant = SurfaceContainerHigh
@Deprecated("Usa OnSurface", ReplaceWith("OnSurface"))
val TextPrimary = OnSurface
@Deprecated("Usa OnSurfaceVariant", ReplaceWith("OnSurfaceVariant"))
val TextSecondary = OnSurfaceVariant
@Deprecated("Usa OnSurfaceVariant con alpha", ReplaceWith("OnSurfaceVariant"))
val TextHint = Color(0xFF6B7B6E)
