package com.gamelaunch.data.repository

import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import com.gamelaunch.data.local.entity.ReleaseWithGame
import com.gamelaunch.data.remote.dto.CoverDto
import com.gamelaunch.data.remote.dto.GameDto
import com.gamelaunch.data.remote.dto.GenreDto
import com.gamelaunch.data.remote.dto.PlatformDto
import com.gamelaunch.data.remote.dto.ReleaseDateDto
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MappersTest {

    // ── ReleaseDateDto → ReleaseEntity ───────────────────────────────────────

    @Test
    fun `toReleaseEntity returns null when game is null`() {
        val dto = ReleaseDateDto(id = 1, date = 1000L, platform = null, region = null, game = null)
        assertNull(dto.toReleaseEntity())
    }

    @Test
    fun `toReleaseEntity returns null when date is null`() {
        val dto = ReleaseDateDto(
            id = 1, date = null,
            platform = PlatformDto(id = 6, name = "PC"),
            region = 8,
            game = GameDto(id = 10, name = "Test", cover = null, genres = null, totalRating = null, summary = null)
        )
        assertNull(dto.toReleaseEntity())
    }

    @Test
    fun `toReleaseEntity maps all fields correctly`() {
        val dto = ReleaseDateDto(
            id = 42,
            date = 1700000000L,
            platform = PlatformDto(id = 167, name = "PS5"),
            region = 1,
            game = GameDto(id = 99, name = "Game", cover = null, genres = null, totalRating = null, summary = null)
        )
        val entity = dto.toReleaseEntity()!!
        assertEquals(42, entity.id)
        assertEquals(99, entity.gameId)
        assertEquals(167, entity.platformId)
        assertEquals(1, entity.regionId)
        assertEquals(1700000000L, entity.dateEpoch)
    }

    @Test
    fun `toReleaseEntity defaults platformId to -1 when platform is null`() {
        val dto = ReleaseDateDto(
            id = 1, date = 1000L, platform = null, region = 8,
            game = GameDto(id = 1, name = "G", cover = null, genres = null, totalRating = null, summary = null)
        )
        assertEquals(-1, dto.toReleaseEntity()!!.platformId)
    }

    @Test
    fun `toReleaseEntity defaults regionId to WORLDWIDE when region is null`() {
        val dto = ReleaseDateDto(
            id = 1, date = 1000L, platform = PlatformDto(6, "PC"), region = null,
            game = GameDto(id = 1, name = "G", cover = null, genres = null, totalRating = null, summary = null)
        )
        assertEquals(Region.WORLDWIDE.igdbId, dto.toReleaseEntity()!!.regionId)
    }

    // ── GameDto → GameEntity ─────────────────────────────────────────────────

    @Test
    fun `toGameEntity normalizes cover URL from thumb to cover_big`() {
        val dto = GameDto(
            id = 1, name = "Test",
            cover = CoverDto(id = 1, url = "//images.igdb.com/igdb/image/upload/t_thumb/abc.jpg"),
            genres = null, totalRating = null, summary = null
        )
        val entity = dto.toGameEntity()
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big_2x/abc.jpg", entity.coverUrl)
    }

    @Test
    fun `toGameEntity joins genres as comma-separated string`() {
        val dto = GameDto(
            id = 1, name = "Test", cover = null,
            genres = listOf(GenreDto(1, "RPG"), GenreDto(2, "Action")),
            totalRating = null, summary = null
        )
        assertEquals("RPG,Action", dto.toGameEntity().genres)
    }

    @Test
    fun `toGameEntity handles null cover and genres`() {
        val dto = GameDto(id = 1, name = "Test", cover = null, genres = null, totalRating = null, summary = null)
        val entity = dto.toGameEntity()
        assertNull(entity.coverUrl)
        assertEquals("", entity.genres)
    }

    // ── GameEntity → Domain ──────────────────────────────────────────────────

    @Test
    fun `GameEntity toDomain splits genres correctly`() {
        val entity = GameEntity(id = 1, name = "Game", coverUrl = null, genres = "RPG,Action,Adventure", rating = null, summary = null)
        val game = entity.toDomain()
        assertEquals(listOf("RPG", "Action", "Adventure"), game.genres)
    }

    @Test
    fun `GameEntity toDomain returns empty genres list when genres blank`() {
        val entity = GameEntity(id = 1, name = "Game", coverUrl = null, genres = "", rating = null, summary = null)
        assertEquals(emptyList<String>(), entity.toDomain().genres)
    }

    @Test
    fun `GameEntity toDomain uses provided releaseDate`() {
        val entity = GameEntity(id = 1, name = "Game", coverUrl = null, genres = "", rating = null, summary = null)
        val date = LocalDate.of(2025, 6, 15)
        assertEquals(date, entity.toDomain(date).releaseDate)
    }

    // ── ReleaseWithGame → Domain ─────────────────────────────────────────────

    @Test
    fun `ReleaseWithGame toDomain converts epoch to LocalDate correctly`() {
        // 2024-01-15 00:00:00 UTC = 1705276800
        val epochSecond = 1705276800L
        val release = ReleaseWithGame(
            release = ReleaseEntity(id = 1, gameId = 10, platformId = 167, regionId = 8, dateEpoch = epochSecond),
            game = GameEntity(id = 10, name = "Game", coverUrl = null, genres = "", rating = null, summary = null)
        )
        val domain = release.toDomain()
        assertEquals(LocalDate.of(2024, 1, 15), domain.date)
    }

    @Test
    fun `ReleaseWithGame toDomain resolves known platform`() {
        val release = ReleaseWithGame(
            release = ReleaseEntity(id = 1, gameId = 10, platformId = 167, regionId = 8, dateEpoch = 1000L),
            game = GameEntity(id = 10, name = "Game", coverUrl = null, genres = "", rating = null, summary = null)
        )
        assertEquals(Platform.PLAYSTATION_5, release.toDomain().platform)
    }

    @Test
    fun `ReleaseWithGame toDomain falls back to STEAM for unknown platformId`() {
        val release = ReleaseWithGame(
            release = ReleaseEntity(id = 1, gameId = 10, platformId = 9999, regionId = 8, dateEpoch = 1000L),
            game = GameEntity(id = 10, name = "Game", coverUrl = null, genres = "", rating = null, summary = null)
        )
        assertEquals(Platform.STEAM, release.toDomain().platform)
    }
}
