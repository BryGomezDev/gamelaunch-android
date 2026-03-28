package com.gamelaunch.domain.usecase

import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.repository.GameRepository
import javax.inject.Inject

class SearchGamesUseCase @Inject constructor(
    private val repository: GameRepository
) {
    suspend operator fun invoke(query: String, offset: Int = 0): List<Game> =
        repository.searchGames(query, offset)
}
