package com.gamelaunch.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ReleaseWithGame(
    @Embedded val release: ReleaseEntity,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "id"
    )
    val game: GameEntity
)
