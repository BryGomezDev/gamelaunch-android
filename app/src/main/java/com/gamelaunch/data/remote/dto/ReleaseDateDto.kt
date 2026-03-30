package com.gamelaunch.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReleaseDateDto(
    @Json(name = "id") val id: Int,
    @Json(name = "date") val date: Long?,          // Unix timestamp
    @Json(name = "platform") val platform: PlatformDto?,
    @Json(name = "region") val region: Int?,
    @Json(name = "game") val game: GameDto?
)

@JsonClass(generateAdapter = true)
data class GameDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "cover") val cover: CoverDto?,
    @Json(name = "genres") val genres: List<GenreDto>?,
    @Json(name = "total_rating") val totalRating: Double?,
    @Json(name = "summary") val summary: String?,
    @Json(name = "first_release_date") val firstReleaseDate: Long? = null,
    @Json(name = "game_modes") val gameModes: List<GameModeDto>? = null,
    @Json(name = "themes") val themes: List<ThemeDto>? = null,
    @Json(name = "involved_companies") val involvedCompanies: List<InvolvedCompanyDto>? = null,
    @Json(name = "websites") val websites: List<WebsiteDto>? = null,
    @Json(name = "screenshots") val screenshots: List<ScreenshotDto>? = null,
    @Json(name = "similar_games") val similarGames: List<SimilarGameDto>? = null
)

@JsonClass(generateAdapter = true)
data class GameModeDto(@Json(name = "name") val name: String?)

@JsonClass(generateAdapter = true)
data class ThemeDto(@Json(name = "name") val name: String?)

@JsonClass(generateAdapter = true)
data class CompanyDto(@Json(name = "name") val name: String?)

@JsonClass(generateAdapter = true)
data class InvolvedCompanyDto(
    @Json(name = "company") val company: CompanyDto?,
    @Json(name = "developer") val developer: Boolean?,
    @Json(name = "publisher") val publisher: Boolean?
)

@JsonClass(generateAdapter = true)
data class WebsiteDto(
    @Json(name = "url") val url: String?,
    @Json(name = "category") val category: Int?
)

@JsonClass(generateAdapter = true)
data class ScreenshotDto(@Json(name = "url") val url: String?)

@JsonClass(generateAdapter = true)
data class SimilarGameDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "cover") val cover: CoverDto?
)

@JsonClass(generateAdapter = true)
data class CoverDto(
    @Json(name = "id") val id: Int?,
    @Json(name = "url") val url: String?
)

@JsonClass(generateAdapter = true)
data class PlatformDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?
)
