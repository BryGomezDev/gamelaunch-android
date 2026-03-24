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
    @Json(name = "summary") val summary: String?
)

@JsonClass(generateAdapter = true)
data class CoverDto(
    @Json(name = "id") val id: Int,
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
