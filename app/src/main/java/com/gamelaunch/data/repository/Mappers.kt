package com.gamelaunch.data.repository

import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import com.gamelaunch.data.local.entity.ReleaseWithGame
import com.gamelaunch.data.remote.dto.GameDto
import com.gamelaunch.data.remote.dto.ReleaseDateDto
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// ── Remote → Entity ─────────────────────────────────────────────────────────

fun ReleaseDateDto.toReleaseEntity(): ReleaseEntity? {
    val gameId = game?.id ?: return null
    val dateEpoch = date ?: return null
    return ReleaseEntity(
        id = id,
        gameId = gameId,
        platformId = platform?.id ?: -1,
        regionId = region ?: Region.WORLDWIDE.igdbId,
        dateEpoch = dateEpoch
    )
}

fun GameDto.toGameEntity(): GameEntity = GameEntity(
    id = id,
    name = name,
    coverUrl = cover?.url?.let { normalizeIgdbImageUrl(it) },
    genres = genres?.mapNotNull { it.name }?.joinToString(",") ?: "",
    rating = totalRating?.toFloat(),
    summary = summary
)

private fun normalizeIgdbImageUrl(url: String): String =
    "https:" + url.replace("t_thumb", "t_cover_big")

// ── Entity → Domain ─────────────────────────────────────────────────────────

fun ReleaseWithGame.toDomain(): Release {
    val localDate = Instant.ofEpochSecond(release.dateEpoch)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
    return Release(
        id = release.id,
        game = game.toDomain(localDate),
        platform = Platform.entries.firstOrNull { it.igdbId == release.platformId }
            ?: Platform.STEAM,
        region = Region.fromId(release.regionId),
        date = localDate
    )
}

fun GameEntity.toDomain(releaseDate: LocalDate = LocalDate.now()): Game = Game(
    id = id,
    name = name,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    platforms = emptyList(),   // populated from releases at use-site
    genres = if (genres.isBlank()) emptyList() else genres.split(","),
    rating = rating,
    summary = summary
)
