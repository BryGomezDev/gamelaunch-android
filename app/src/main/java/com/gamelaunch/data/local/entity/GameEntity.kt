package com.gamelaunch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val coverUrl: String?,
    val genres: String,           // JSON array stored as string
    val rating: Float?,
    val summary: String?,
    val isWishlisted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
