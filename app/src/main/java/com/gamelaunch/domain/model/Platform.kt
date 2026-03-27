package com.gamelaunch.domain.model

enum class Platform(val displayName: String, val igdbId: Int) {
    STEAM("PC / Steam", 6),
    PLAYSTATION_5("PlayStation 5", 167),
    PLAYSTATION_4("PlayStation 4", 48),
    XBOX_SERIES("Xbox Series X|S", 169),
    XBOX_ONE("Xbox One", 49),
    NINTENDO_SWITCH("Nintendo Switch", 130),
}
