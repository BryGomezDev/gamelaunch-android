package com.gamelaunch.data.repository

import android.util.Log
import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import com.gamelaunch.data.remote.IgdbApi
import com.gamelaunch.data.remote.IgdbQueryBuilder
import com.gamelaunch.data.remote.dto.GameDto
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import java.time.Instant
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

    override fun getReleasesForMonth(year: Int, month: Int, platformId: Int?, regionId: Int?): Flow<List<Release>> {
        val ym = YearMonth.of(year, month)
        val startEpoch = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val endEpoch = ym.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneOffset.UTC).toEpochSecond()
        return gameDao.getReleasesForRangeFiltered(startEpoch, endEpoch, platformId, regionId)
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

    override suspend fun searchGames(query: String, offset: Int): List<Game> {
        val dtos = igdbApi.searchGames(IgdbQueryBuilder.searchGames(query, offset).asIgdbBody())
        return dtos.map { dto -> dto.toDomainGame() }
    }

    override suspend fun getGameDetail(id: Int): Game? {
        // Always fetch enriched data from API and update Room
        return try {
            val dtos = igdbApi.getGameById(IgdbQueryBuilder.gameById(id).asIgdbBody())
            val dto = dtos.firstOrNull()
            if (dto != null) {
                val wishlisted = gameDao.isWishlisted(id) ?: false
                gameDao.upsertGames(listOf(dto.toGameEntity().copy(isWishlisted = wishlisted)))
                val releaseEpoch = gameDao.getEarliestReleaseEpoch(id)
                val releaseDate = releaseEpoch?.let { Instant.ofEpochSecond(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    ?: dto.firstReleaseDate?.let { Instant.ofEpochSecond(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    ?: LocalDate.now()
                val platforms = gameDao.getPlatformIdsForGame(id)
                    .mapNotNull { pid -> Platform.entries.firstOrNull { it.igdbId == pid } }
                dto.toDomainGame(releaseDate, platforms)
            } else {
                roomFallback(id)
            }
        } catch (e: Exception) {
            roomFallback(id)
        }
    }

    private suspend fun roomFallback(id: Int): Game? {
        val cached = gameDao.getGameById(id) ?: return null
        val releaseEpoch = gameDao.getEarliestReleaseEpoch(id)
        val releaseDate = releaseEpoch?.let { Instant.ofEpochSecond(it).atZone(ZoneOffset.UTC).toLocalDate() }
            ?: LocalDate.now()
        val platforms = gameDao.getPlatformIdsForGame(id)
            .mapNotNull { pid -> Platform.entries.firstOrNull { it.igdbId == pid } }
        return cached.toDomain(releaseDate).copy(platforms = platforms)
    }

    override suspend fun addToWishlist(game: Game) {
        gameDao.upsertGameAndWishlist(game.toEntity())
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

    override suspend fun syncMonth(year: Int, month: Int) {
        syncMonth(LocalDate.of(year, month, 1))
    }

    private suspend fun syncMonth(date: LocalDate) {
        val query = IgdbQueryBuilder.releasesForMonth(date.year, date.monthValue)
        val dtos = igdbApi.getReleaseDates(query.asIgdbBody())
        val rawGames = dtos.mapNotNull { it.game?.toGameEntity() }
        val releases = dtos.mapNotNull { it.toReleaseEntity() }
        if (rawGames.isNotEmpty()) {
            val wishlisted = gameDao.getWishlistedIds().toHashSet()
            val games = rawGames.map { if (it.id in wishlisted) it.copy(isWishlisted = true) else it }
            gameDao.upsertGames(games)
            gameDao.upsertReleases(releases)
            Log.d(TAG, "syncMonth ${date.year}/${date.monthValue}: saved ${rawGames.size} games, ${releases.size} releases")
        } else {
            Log.w(TAG, "syncMonth ${date.year}/${date.monthValue}: API returned ${dtos.size} DTOs but no valid game data")
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

    private fun GameDto.toDomainGame(
        releaseDate: LocalDate = firstReleaseDate?.let {
            Instant.ofEpochSecond(it).atZone(ZoneOffset.UTC).toLocalDate()
        } ?: LocalDate.now(),
        platforms: List<Platform> = emptyList()
    ): Game = Game(
        id = id,
        name = name,
        coverUrl = cover?.url?.let { "https:" + it.replace("t_thumb", "t_cover_big") },
        releaseDate = releaseDate,
        platforms = platforms,
        genres = genres?.mapNotNull { it.name } ?: emptyList(),
        rating = totalRating?.toFloat(),
        summary = summary,
        gameModes = gameModes?.mapNotNull { it.name } ?: emptyList(),
        themes = themes?.mapNotNull { it.name } ?: emptyList(),
        developers = involvedCompanies?.filter { it.developer == true }?.mapNotNull { it.company?.name } ?: emptyList(),
        publishers = involvedCompanies?.filter { it.publisher == true }?.mapNotNull { it.company?.name } ?: emptyList(),
        websiteUrl = websites?.firstOrNull { it.category == 1 }?.url,
        screenshots = screenshots?.mapNotNull { it.url }
            ?.map { "https:" + it.replace("t_thumb", "t_screenshot_big") } ?: emptyList(),
        similarGames = similarGames?.mapNotNull { dto ->
            val name = dto.name ?: return@mapNotNull null
            com.gamelaunch.domain.model.SimilarGame(
                id = dto.id,
                name = name,
                coverUrl = dto.cover?.url?.let { "https:" + it.replace("t_thumb", "t_cover_big") }
            )
        } ?: emptyList()
    )

    override suspend fun saveTranslation(gameId: Int, summaryEs: String) =
        gameDao.updateSummaryEs(gameId, summaryEs)

    private fun Game.toEntity() = GameEntity(
        id = id,
        name = name,
        coverUrl = coverUrl,
        genres = genres.joinToString(","),
        rating = rating,
        summary = summary,
        gameModes = gameModes.joinToString(","),
        themes = themes.joinToString(","),
        developers = developers.joinToString(","),
        publishers = publishers.joinToString(","),
        websiteUrl = websiteUrl,
        screenshots = screenshots.joinToString(","),
        summaryEs = summaryEs
    )
}
