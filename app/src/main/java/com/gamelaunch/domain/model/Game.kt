package com.gamelaunch.domain.model

import java.time.LocalDate

data class Game(
    val id: Int,
    val name: String,
    val coverUrl: String?,
    val releaseDate: LocalDate,
    val platforms: List<Platform>,
    val genres: List<String>,
    val rating: Float?,
    val summary: String?,
    // Enriched detail fields
    val gameModes: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val developers: List<String> = emptyList(),
    val publishers: List<String> = emptyList(),
    val websiteUrl: String? = null,
    val screenshots: List<String> = emptyList()
)
