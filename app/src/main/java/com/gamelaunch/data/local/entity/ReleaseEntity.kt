package com.gamelaunch.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "releases",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("dateEpoch")]
)
data class ReleaseEntity(
    @PrimaryKey val id: Int,
    val gameId: Int,
    val platformId: Int,
    val regionId: Int,
    val dateEpoch: Long       // seconds since epoch (UTC midnight)
)
