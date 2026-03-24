package com.gamelaunch.domain.usecase

import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WishlistUseCase @Inject constructor(
    private val repository: GameRepository
) {
    fun getWishlist(): Flow<List<Game>> = repository.getWishlist()

    suspend fun add(game: Game) = repository.addToWishlist(game)

    suspend fun remove(gameId: Int) = repository.removeFromWishlist(gameId)

    suspend fun isWishlisted(gameId: Int): Boolean = repository.isInWishlist(gameId)
}
