package com.gamelaunch.data.remote

import com.gamelaunch.domain.model.Platform
import java.time.LocalDate
import java.time.ZoneOffset

object IgdbQueryBuilder {

    private val supportedPlatformIds = Platform.entries
        .map { it.igdbId }
        .distinct()
        .joinToString(",")

    fun releasesForMonth(year: Int, month: Int): String {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        val startEpoch = start.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val endEpoch = end.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond()
        return """
            fields id, date, region,
                   platform.id, platform.name,
                   game.id, game.name,
                   game.cover.url,
                   game.genres.name,
                   game.total_rating,
                   game.summary;
            where date >= $startEpoch
              & date <= $endEpoch
              & platform = ($supportedPlatformIds);
            limit 500;
        """.trimIndent()
    }

    fun searchGames(query: String): String = """
        fields id, name, cover.url, genres.name, total_rating, summary;
        search "$query";
        limit 20;
    """.trimIndent()

    fun gameById(id: Int): String = """
        fields id, name, cover.url, genres.name, total_rating, summary;
        where id = $id;
        limit 1;
    """.trimIndent()
}
