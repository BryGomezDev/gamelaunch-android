package com.gamelaunch.domain.usecase

import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetReleasesUseCase @Inject constructor(
    private val repository: GameRepository
) {
    fun forMonth(year: Int, month: Int, platform: Platform? = null, region: Region? = null): Flow<List<Release>> =
        repository.getReleasesForMonth(year, month, platform?.igdbId, region?.igdbId)

    fun forDay(date: LocalDate): Flow<List<Release>> =
        repository.getReleasesForDay(date)
}
