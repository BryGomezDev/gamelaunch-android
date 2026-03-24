package com.gamelaunch.data.repository

import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.remote.IgdbApi
import com.gamelaunch.data.remote.IgdbQueryBuilder
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

private val PLAIN_TEXT = "text/plain".toMediaType()
private fun String.asIgdbBody() = trimIndent().toRequestBody(PLAIN_TEXT)

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
        val dtos = igdbApi.getReleaseDates(
            IgdbQueryBuilder.releasesForMonth(date.year, date.monthValue).asIgdbBody()
        )
        val games = dtos.mapNotNull { it.game?.toGameEntity() }
        val releases = dtos.mapNotNull { it.toReleaseEntity() }
        if (games.isNotEmpty()) {
            gameDao.upsertGames(games)
            gameDao.upsertReleases(releases)
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
