package com.gamelaunch.data.repository

import android.util.Log
import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import com.gamelaunch.data.remote.IgdbApi
import com.gamelaunch.data.remote.IgdbQueryBuilder
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

private val PLAIN_TEXT = "text/plain".toMediaType()
private fun String.asIgdbBody() = toRequestBody(PLAIN_TEXT)
private const val TAG = "GameRepository"

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val igdbApi: IgdbApi,
    private val gameDao: GameDao
) : GameRepository {

    override fun getReleasesForMonth(year: Int, month: Int): Flow<List<Release>> {
        val ym = YearMonth.of(year, month)
        val startEpoch = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val endEpoch = ym.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond()
        return gameDao.getReleasesForRange(startEpoch, endEpoch)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getReleasesForDay(date: LocalDate): Flow<List<Release>> {
        val startEpoch = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val endEpoch = date.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond()
        return gameDao.getReleasesForRange(startEpoch, endEpoch)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getWishlist(): Flow<List<Game>> =
        gameDao.getWishlist().map { list -> list.map { it.toDomain() } }

    override suspend fun searchGames(query: String): List<Game> {
        val dtos = igdbApi.searchGames(IgdbQueryBuilder.searchGames(query).asIgdbBody())
        return dtos.map { dto ->
            Game(
                id = dto.id,
                name = dto.name,
                coverUrl = dto.cover?.url?.let { "https:" + it.replace("t_thumb", "t_cover_big") },
                releaseDate = LocalDate.now(),
                platforms = emptyList(),
                genres = dto.genres?.mapNotNull { it.name } ?: emptyList(),
                rating = dto.totalRating?.toFloat(),
                summary = dto.summary
            )
        }
    }

    override suspend fun getGameDetail(id: Int): Game? {
        val dtos = igdbApi.getGameById(IgdbQueryBuilder.gameById(id).asIgdbBody())
        return dtos.firstOrNull()?.let { dto ->
            Game(
                id = dto.id,
                name = dto.name,
                coverUrl = dto.cover?.url?.let { "https:" + it.replace("t_thumb", "t_cover_big") },
                releaseDate = LocalDate.now(),
                platforms = emptyList(),
                genres = dto.genres?.mapNotNull { it.name } ?: emptyList(),
                rating = dto.totalRating?.toFloat(),
                summary = dto.summary
            )
        }
    }

    override suspend fun addToWishlist(game: Game) {
        gameDao.upsertGames(listOf(game.toEntity()))
        gameDao.addToWishlist(game.id)
    }

    override suspend fun removeFromWishlist(gameId: Int) {
        gameDao.removeFromWishlist(gameId)
    }

    override suspend fun isInWishlist(gameId: Int): Boolean =
        gameDao.isWishlisted(gameId) ?: false

    override suspend fun syncReleases() {
        syncMonth(LocalDate.now())
        syncMonth(LocalDate.now().plusMonths(1))
    }

    private suspend fun syncMonth(date: LocalDate) {
        val query = IgdbQueryBuilder.releasesForMonth(date.year, date.monthValue)
        Log.d(TAG, "syncMonth ${date.year}/${date.monthValue} — query: $query")
        val dtos = igdbApi.getReleaseDates(query.asIgdbBody())
        Log.d(TAG, "IGDB returned ${dtos.size} release_date DTOs")
        dtos.take(3).forEach { Log.d(TAG, "  sample dto: id=${it.id} date=${it.date} game=${it.game?.name} platform=${it.platform?.name}") }
        val games = dtos.mapNotNull { it.game?.toGameEntity() }
        val releases = dtos.mapNotNull { it.toReleaseEntity() }
        Log.d(TAG, "Prepared ${games.size} game entities, ${releases.size} release entities")
        if (games.isNotEmpty()) {
            gameDao.upsertGames(games)
            gameDao.upsertReleases(releases)
            Log.d(TAG, "Saved to Room successfully")
        } else {
            Log.w(TAG, "No games to save — API returned ${dtos.size} DTOs but all had null game field")
        }
    }

    override suspend fun seedTestData() {
        val now = LocalDate.now()
        Log.d(TAG, "Seeding hardcoded test data for ${now.year}/${now.monthValue}")
        val seed = listOf(
            Triple(99001, "Seed: Dragon Age: The Veilguard", Platform.PLAYSTATION_5),
            Triple(99002, "Seed: Halo Infinite Season 6",   Platform.XBOX_SERIES),
            Triple(99003, "Seed: Metroid Prime 4",          Platform.NINTENDO_SWITCH),
            Triple(99004, "Seed: Half-Life 3",              Platform.STEAM),
            Triple(99005, "Seed: God of War: Ragnarok PC",  Platform.STEAM),
            Triple(99006, "Seed: Starfield DLC",            Platform.XBOX_SERIES),
            Triple(99007, "Seed: Final Fantasy XVII",       Platform.PLAYSTATION_5),
        )
        val days = listOf(3, 7, 10, 14, 18, 22, 27)
        val gameEntities = seed.map { (id, name, _) ->
            GameEntity(id = id, name = name, coverUrl = null, genres = "RPG", rating = 85f, summary = "Test data — API not yet connected.")
        }
        val releaseEntities = seed.mapIndexed { i, (id, _, platform) ->
            val day = days[i]
            val epoch = LocalDate.of(now.year, now.monthValue, day)
                .atStartOfDay(ZoneOffset.UTC).toEpochSecond()
            ReleaseEntity(id = id, gameId = id, platformId = platform.igdbId, regionId = 8, dateEpoch = epoch)
        }
        gameDao.upsertGames(gameEntities)
        gameDao.upsertReleases(releaseEntities)
        Log.d(TAG, "Seed complete: ${gameEntities.size} games, ${releaseEntities.size} releases")
    }

    private fun Game.toEntity() = com.gamelaunch.data.local.entity.GameEntity(
        id = id,
        name = name,
        coverUrl = coverUrl,
        genres = genres.joinToString(","),
        rating = rating,
        summary = summary
    )
}
