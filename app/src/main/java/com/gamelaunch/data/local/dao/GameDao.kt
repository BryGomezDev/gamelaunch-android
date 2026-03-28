package com.gamelaunch.data.local.dao

import androidx.room.*
import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import com.gamelaunch.data.local.entity.ReleaseWithGame
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // Games
    @Upsert
    suspend fun upsertGames(games: List<GameEntity>)

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Int): GameEntity?

    @Query("SELECT MIN(dateEpoch) FROM releases WHERE gameId = :gameId")
    suspend fun getEarliestReleaseEpoch(gameId: Int): Long?

    @Query("SELECT DISTINCT platformId FROM releases WHERE gameId = :gameId")
    suspend fun getPlatformIdsForGame(gameId: Int): List<Int>

    @Query("SELECT * FROM games WHERE isWishlisted = 1")
    fun getWishlist(): Flow<List<GameEntity>>

    @Query("SELECT id FROM games WHERE isWishlisted = 1")
    suspend fun getWishlistedIds(): List<Int>

    @Transaction
    suspend fun upsertGameAndWishlist(game: GameEntity) {
        upsertGames(listOf(game))
        setWishlisted(game.id, true)
    }

    @Query("UPDATE games SET isWishlisted = :value WHERE id = :id")
    suspend fun setWishlisted(id: Int, value: Boolean)

    @Query("UPDATE games SET isWishlisted = 1 WHERE id = :id")
    suspend fun addToWishlist(id: Int)

    @Query("UPDATE games SET isWishlisted = 0 WHERE id = :id")
    suspend fun removeFromWishlist(id: Int)

    @Query("SELECT isWishlisted FROM games WHERE id = :id")
    suspend fun isWishlisted(id: Int): Boolean?

    // Releases
    @Upsert
    suspend fun upsertReleases(releases: List<ReleaseEntity>)

    @Transaction
    @Query("""
        SELECT * FROM releases
        WHERE dateEpoch >= :startEpoch AND dateEpoch <= :endEpoch
        ORDER BY dateEpoch ASC
    """)
    fun getReleasesForRange(startEpoch: Long, endEpoch: Long): Flow<List<ReleaseWithGame>>

    @Query("DELETE FROM releases WHERE dateEpoch >= :startEpoch AND dateEpoch <= :endEpoch")
    suspend fun deleteReleasesForRange(startEpoch: Long, endEpoch: Long)
}
