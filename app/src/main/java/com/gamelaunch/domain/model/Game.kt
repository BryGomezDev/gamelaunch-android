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
    val summary: String?
)
