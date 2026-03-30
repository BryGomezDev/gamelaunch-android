package com.gamelaunch.presentation.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.usecase.GetReleasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class DayReleasesUiState(
    val date: LocalDate = LocalDate.now(),
    val allReleases: List<Release> = emptyList(),
    val platformFilter: Platform? = null,
    val isLoading: Boolean = true
) {
    /** One entry per unique game, with all its platforms aggregated. */
    val groupedReleases: List<Release>
        get() {
            val source = if (platformFilter == null) allReleases
                         else allReleases.filter { it.platform == platformFilter }
            return source
                .groupBy { it.game.id }
                .values
                .map { group ->
                    val allPlatforms = group.map { it.platform }.distinct()
                    group.first().copy(game = group.first().game.copy(platforms = allPlatforms))
                }
                .sortedBy { it.game.name }
        }

    val availablePlatforms: List<Platform>
        get() = allReleases.map { it.platform }.distinct().sortedBy { it.displayName }
}

@HiltViewModel
class DayReleasesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReleasesUseCase: GetReleasesUseCase
) : ViewModel() {

    private val date: LocalDate =
        LocalDate.parse(checkNotNull(savedStateHandle.get<String>("date")))

    private val _uiState = MutableStateFlow(DayReleasesUiState(date = date))
    val uiState: StateFlow<DayReleasesUiState> = _uiState.asStateFlow()

    init {
        getReleasesUseCase.forDay(date)
            .onEach { releases ->
                _uiState.update { it.copy(allReleases = releases, isLoading = false) }
            }
            .catch { _uiState.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)
    }

    fun onPlatformFilter(platform: Platform?) {
        _uiState.update { it.copy(platformFilter = platform) }
    }
}
