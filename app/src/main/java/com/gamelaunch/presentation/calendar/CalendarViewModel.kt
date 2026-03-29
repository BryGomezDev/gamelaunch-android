package com.gamelaunch.presentation.calendar

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.usecase.GetReleasesUseCase
import com.gamelaunch.domain.repository.GameRepository
import com.gamelaunch.presentation.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private const val TAG = "CalendarVM"

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate? = null,
    val releases: List<Release> = emptyList(),
    val selectedDayReleases: List<Release> = emptyList(),
    val platformFilter: Platform? = null,
    val regionFilter: Region? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val syncDebugInfo: String = "Iniciando…"
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getReleasesUseCase: GetReleasesUseCase,
    private val repository: GameRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private val syncedMonths = mutableSetOf<YearMonth>()

    init {
        // Read preferred region from settings and apply as default filter
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val preferredRegion = Region.fromId(
                prefs[SettingsViewModel.REGION_KEY] ?: Region.WORLDWIDE.igdbId
            )
            // Only set filter if user has chosen a specific region (not WORLDWIDE = show all)
            val initialRegion = if (preferredRegion == Region.WORLDWIDE) null else preferredRegion
            _uiState.update { it.copy(regionFilter = initialRegion) }
        }
        val month = _uiState.value.currentMonth
        loadMonth(month)
        fetchMonthIfEmpty(month)
    }

    private fun loadMonth(month: YearMonth) {
        loadJob?.cancel()
        val state = _uiState.value
        loadJob = viewModelScope.launch {
            getReleasesUseCase.forMonth(month.year, month.monthValue, state.platformFilter, state.regionFilter)
                .catch { e ->
                    Log.e(TAG, "Flow error: ${e.message}", e)
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { releases ->
                    _uiState.update { s ->
                        s.copy(
                            releases = releases,
                            syncDebugInfo = "${releases.size} lanzamientos en Room",
                            selectedDayReleases = s.selectedDay
                                ?.let { day -> releases.filter { it.date == day } }
                                ?: emptyList()
                        )
                    }
                }
        }
    }

    fun onMonthChange(month: YearMonth) {
        _uiState.update { it.copy(currentMonth = month, selectedDay = null) }
        loadMonth(month)
        fetchMonthIfEmpty(month)
    }

    private fun fetchMonthIfEmpty(month: YearMonth) {
        if (month in syncedMonths) return
        viewModelScope.launch {
            try {
                val cached = getReleasesUseCase.forMonth(month.year, month.monthValue).first()
                if (cached.isEmpty()) {
                    _uiState.update { it.copy(isRefreshing = true, error = null, syncDebugInfo = "Cargando $month…") }
                    try {
                        repository.syncMonth(month.year, month.monthValue)
                        syncedMonths += month
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        Log.e(TAG, "fetchMonthIfEmpty $month failed: ${e.message}", e)
                        _uiState.update { it.copy(error = "Error al cargar mes: ${e.message}", syncDebugInfo = "Error: ${e.javaClass.simpleName}") }
                    } finally {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                } else {
                    syncedMonths += month
                    _uiState.update { it.copy(syncDebugInfo = "Room: ${cached.size} lanzamientos") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchMonthIfEmpty $month read error: ${e.message}", e)
            }
        }
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
        _uiState.update { it.copy(platformFilter = platform) }
        loadMonth(_uiState.value.currentMonth)
    }

    fun onRegionFilter(region: Region?) {
        _uiState.update { it.copy(regionFilter = region) }
        loadMonth(_uiState.value.currentMonth)
    }

    fun refresh() {
        val month = _uiState.value.currentMonth
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null, syncDebugInfo = "Sincronizando con IGDB…") }
            try {
                repository.syncMonth(month.year, month.monthValue)
                syncedMonths += month
                _uiState.update { it.copy(syncDebugInfo = "Sync OK — ${_uiState.value.releases.size} lanzamientos") }
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e(TAG, "refresh failed: ${e.message}", e)
                _uiState.update { it.copy(error = "Sync error: ${e.message}", syncDebugInfo = "Error: ${e.javaClass.simpleName}") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun seedData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null, syncDebugInfo = "Insertando datos de prueba…") }
            try {
                repository.seedTestData()
            } catch (e: Exception) {
                Log.e(TAG, "seedData failed: ${e.message}", e)
                _uiState.update { it.copy(error = "Seed error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

}
