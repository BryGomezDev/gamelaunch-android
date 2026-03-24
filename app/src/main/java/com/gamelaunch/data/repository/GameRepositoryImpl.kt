package com.gamelaunch.data.repository

import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.remote.IgdbApi
import com.gamelaunch.data.remote.IgdbQueryBuilder
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

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
        val dtos = igdbApi.searchGames(IgdbQueryBuilder.searchGames(query))
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
        val dtos = igdbApi.getGameById(IgdbQueryBuilder.gameById(id))
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
        val now = LocalDate.now()
        val query = IgdbQueryBuilder.releasesForMonth(now.year, now.monthValue)
        val releaseDtos = igdbApi.getReleaseDates(query)

        val gameEntities = releaseDtos.mapNotNull { it.game?.toGameEntity() }
        val releaseEntities = releaseDtos.mapNotNull { it.toReleaseEntity() }

        if (gameEntities.isNotEmpty()) {
            gameDao.upsertGames(gameEntities)
            gameDao.upsertReleases(releaseEntities)
        }

        // Also sync next month proactively
        val next = now.plusMonths(1)
        val nextQuery = IgdbQueryBuilder.releasesForMonth(next.year, next.monthValue)
        val nextDtos = igdbApi.getReleaseDates(nextQuery)
        val nextGames = nextDtos.mapNotNull { it.game?.toGameEntity() }
        val nextReleases = nextDtos.mapNotNull { it.toReleaseEntity() }
        if (nextGames.isNotEmpty()) {
            gameDao.upsertGames(nextGames)
            gameDao.upsertReleases(nextReleases)
        }
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
