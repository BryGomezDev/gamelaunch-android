package com.gamelaunch.domain.usecase

import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetReleasesUseCase @Inject constructor(
    private val repository: GameRepository
) {
    fun forMonth(year: Int, month: Int): Flow<List<Release>> =
        repository.getReleasesForMonth(year, month)

    fun forDay(date: LocalDate): Flow<List<Release>> =
        repository.getReleasesForDay(date)
}
