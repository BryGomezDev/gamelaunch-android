package com.gamelaunch.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.usecase.GetReleasesUseCase
import com.gamelaunch.domain.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate? = null,
    val releases: List<Release> = emptyList(),
    val selectedDayReleases: List<Release> = emptyList(),
    val platformFilter: Platform? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getReleasesUseCase: GetReleasesUseCase,
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        val month = _uiState.value.currentMonth
        loadMonth(month)
        // On first launch Room is empty — trigger a sync automatically
        viewModelScope.launch {
            val initial = getReleasesUseCase.forMonth(month.year, month.monthValue).first()
            if (initial.isEmpty()) refresh()
        }
    }

    private fun loadMonth(month: YearMonth) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getReleasesUseCase.forMonth(month.year, month.monthValue)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { releases ->
                    _uiState.update { state ->
                        val filtered = releases.applyFilter(state.platformFilter)
                        state.copy(
                            releases = filtered,
                            selectedDayReleases = state.selectedDay
                                ?.let { day -> filtered.filter { it.date == day } }
                                ?: emptyList()
                        )
                    }
                }
        }
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(currentMonth = month, selectedDay = null) }
        loadMonth(month)
    }

    fun onDaySelected(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                selectedDay = date,
                selectedDayReleases = state.releases.filter { it.date == date }
            )
        }
    }

    fun onPlatformFilter(platform: Platform?) {
        _uiState.update { state ->
            val filtered = state.releases.applyFilter(platform)
            state.copy(
                platformFilter = platform,
                selectedDayReleases = state.selectedDay
                    ?.let { day -> filtered.filter { it.date == day } }
                    ?: emptyList()
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.syncReleases()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun List<Release>.applyFilter(platform: Platform?) =
        if (platform == null) this else filter { it.platform == platform }
}
