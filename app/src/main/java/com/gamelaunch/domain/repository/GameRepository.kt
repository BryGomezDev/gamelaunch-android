package com.gamelaunch.domain.repository

import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Release
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GameRepository {
    fun getReleasesForMonth(year: Int, month: Int): Flow<List<Release>>
    fun getReleasesForDay(date: LocalDate): Flow<List<Release>>
    fun getWishlist(): Flow<List<Game>>
    suspend fun searchGames(query: String, offset: Int = 0): List<Game>
    suspend fun getGameDetail(id: Int): Game?
    suspend fun addToWishlist(game: Game)
    suspend fun removeFromWishlist(gameId: Int)
    suspend fun isInWishlist(gameId: Int): Boolean
    suspend fun syncReleases()
    suspend fun syncMonth(year: Int, month: Int)
    /** Inserts hardcoded releases for the current month — bypasses IGDB entirely. */
    suspend fun seedTestData()
}
