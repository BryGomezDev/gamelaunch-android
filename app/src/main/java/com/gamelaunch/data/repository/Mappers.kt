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
    coverUrl = cover?.url?.let { normalizeCoverUrl(it) },
    genres = genres?.mapNotNull { it.name }?.joinToString(",") ?: "",
    rating = totalRating?.toFloat(),
    summary = summary,
    gameModes = gameModes?.mapNotNull { it.name }?.joinToString(",") ?: "",
    themes = themes?.mapNotNull { it.name }?.joinToString(",") ?: "",
    developers = involvedCompanies
        ?.filter { it.developer == true }
        ?.mapNotNull { it.company?.name }
        ?.joinToString(",") ?: "",
    publishers = involvedCompanies
        ?.filter { it.publisher == true }
        ?.mapNotNull { it.company?.name }
        ?.joinToString(",") ?: "",
    websiteUrl = websites?.firstOrNull { it.category == WEBSITE_CATEGORY_OFFICIAL }?.url,
    screenshots = screenshots
        ?.mapNotNull { it.url }
        ?.map { normalizeScreenshotUrl(it) }
        ?.joinToString(",") ?: ""
)

private fun normalizeCoverUrl(url: String): String =
    "https:" + url.replace("t_thumb", "t_cover_big_2x")

private fun normalizeScreenshotUrl(url: String): String =
    "https:" + url.replace("t_thumb", "t_screenshot_big")

private const val WEBSITE_CATEGORY_OFFICIAL = 1

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
    genres = genres.splitIfNotBlank(),
    rating = rating,
    summary = summary,
    gameModes = gameModes.splitIfNotBlank(),
    themes = themes.splitIfNotBlank(),
    developers = developers.splitIfNotBlank(),
    publishers = publishers.splitIfNotBlank(),
    websiteUrl = websiteUrl,
    screenshots = screenshots.splitIfNotBlank(),
    summaryEs = summaryEs
)

private fun String.splitIfNotBlank(): List<String> =
    if (isBlank()) emptyList() else split(",")
