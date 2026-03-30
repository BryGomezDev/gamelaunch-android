package com.gamelaunch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val coverUrl: String?,
    val genres: String,           // comma-separated
    val rating: Float?,
    val summary: String?,
    val isWishlisted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    // Enriched detail fields (populated on detail screen load)
    val gameModes: String = "",   // comma-separated
    val themes: String = "",      // comma-separated
    val developers: String = "",  // comma-separated
    val publishers: String = "",  // comma-separated
    val websiteUrl: String? = null,
    val screenshots: String = "",  // comma-separated URLs
    val summaryEs: String? = null  // traducción al español (DeepL)
)
