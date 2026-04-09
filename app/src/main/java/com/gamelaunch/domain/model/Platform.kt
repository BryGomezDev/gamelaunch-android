package com.gamelaunch.domain.model

import androidx.annotation.DrawableRes
import com.gamelaunch.R

enum class Platform(val displayName: String, val igdbId: Int) {
    STEAM("PC / Steam", 6),
    PLAYSTATION_5("PS5", 167),
    XBOX_SERIES("Xbox", 169),
    NINTENDO_SWITCH("Switch", 130);

    val iconRes: Int
        @DrawableRes get() = when (this) {
            STEAM            -> R.drawable.ic_platform_steam
            PLAYSTATION_5    -> R.drawable.ic_platform_ps
            XBOX_SERIES      -> R.drawable.ic_platform_xbox
            NINTENDO_SWITCH  -> R.drawable.ic_platform_switch
        }
}

/** Devuelve la Platform correspondiente a un nombre de plataforma (case-insensitive).
 *  Maneja variantes como "PC (Windows)", "Steam", "Xbox Series X|S", etc. */
fun platformFromName(name: String): Platform? {
    val lower = name.lowercase()
    return when {
        lower.contains("steam") || lower.contains("pc") || lower.contains("windows") -> Platform.STEAM
        lower.contains("ps5") || lower.contains("playstation 5")                     -> Platform.PLAYSTATION_5
        lower.contains("xbox")                                                        -> Platform.XBOX_SERIES
        lower.contains("switch")                                                      -> Platform.NINTENDO_SWITCH
        else                                                                          -> null
    }
}
