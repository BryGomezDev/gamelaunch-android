package com.gamelaunch.domain.model

import java.time.LocalDate

data class Release(
    val id: Int,
    val game: Game,
    val platform: Platform,
    val region: Region,
    val date: LocalDate
)
