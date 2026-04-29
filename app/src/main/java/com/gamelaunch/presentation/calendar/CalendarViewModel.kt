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
import com.gamelaunch.domain.usecase.WishlistUseCase
import com.gamelaunch.domain.repository.GameRepository
import com.gamelaunch.presentation.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private const val TAG = "CalendarVM"

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate? = null,
    val releases: List<Release> = emptyList(),
    val selectedDayReleases: List<Release> = emptyList(),
    val weekReleases: List<Release> = emptyList(),
    val featuredReleases: List<Release> = emptyList(),
    val timelineReleases: List<Release> = emptyList(),
    val wishlistedIds: Set<Int> = emptySet(),
    val platformFilters: Set<Platform> = emptySet(),  // vacío = Todos
    val regionFilter: Region? = null,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val syncDebugInfo: String = "Iniciando…",
    val language: String = "es"
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getReleasesUseCase: GetReleasesUseCase,
    private val repository: GameRepository,
    private val wishlistUseCase: WishlistUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private val syncedMonths = mutableSetOf<YearMonth>()

    init {
        val month = _uiState.value.currentMonth

        // Carga inmediata para evitar pantalla vacía mientras DataStore lee del disco
        loadMonth(month)
        fetchMonthIfEmpty(month)

        viewModelScope.launch {
            dataStore.data
                .map { prefs -> prefs[SettingsViewModel.LANGUAGE_KEY] ?: "es" }
                .distinctUntilChanged()
                .collect { lang -> _uiState.update { it.copy(language = lang) } }
        }

        // Reactivo: observa cambios en favoritas de DataStore.
        // - Primera emisión: aplica el filtro guardado al arrancar
        // - Emisiones posteriores: actualiza el filtro cuando el usuario
        //   cambia ajustes y vuelve a esta pantalla, sin reiniciar la app.
        // distinctUntilChanged() evita recargar si las favoritas no cambiaron,
        // lo que preserva los cambios manuales del usuario en los chips.
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    prefs[SettingsViewModel.FAVORITE_PLATFORMS_KEY]
                        ?.mapNotNull { name -> runCatching { Platform.valueOf(name) }.getOrNull() }
                        ?.toSet()
                        ?: emptySet()
                }
                .distinctUntilChanged()
                .collect { favorites ->
                    val prevFilters = _uiState.value.platformFilters
                    if (favorites != prevFilters) {
                        _uiState.update { it.copy(platformFilters = favorites) }
                        loadMonth(_uiState.value.currentMonth)
                    }
                }
        }

        // Reactive wishlist ids
        viewModelScope.launch {
            wishlistUseCase.getWishlist().collect { games ->
                _uiState.update { it.copy(wishlistedIds = games.map { g -> g.id }.toSet()) }
            }
        }

        // Week + featured always based on current real month (not the viewed month)
        viewModelScope.launch {
            val today = LocalDate.now()
            val ym = YearMonth.now()
            getReleasesUseCase.forMonth(ym.year, ym.monthValue)
                .catch { /* best-effort */ }
                .collect { monthReleases ->
                    val weekStart = today.with(DayOfWeek.MONDAY)
                    val weekEnd = weekStart.plusDays(6)
                    val week = monthReleases
                        .filter { it.date >= weekStart && it.date <= weekEnd }
                        .groupBy { it.game.id }
                        .map { (_, releases) ->
                            val merged = releases.map { it.platform }.distinct()
                            releases.first().copy(game = releases.first().game.copy(platforms = merged))
                        }
                        .sortedBy { it.date }
                    val sevenDaysAgo = today.minusDays(6)
                    val featured = monthReleases
                        .filter { it.date >= sevenDaysAgo && it.date <= today && it.game.rating != null }
                        .groupBy { it.game.id }
                        .map { (_, releases) ->
                            val merged = releases.map { it.platform }.distinct()
                            val best = releases.maxBy { it.game.rating ?: 0f }
                            best.copy(game = best.game.copy(platforms = merged))
                        }
                        .sortedByDescending { it.game.rating }
                        .take(10)
                    _uiState.update { it.copy(weekReleases = week, featuredReleases = featured) }
                }
        }
    }

    private fun loadMonth(month: YearMonth) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // Cargamos siempre sin filtro de plataforma en BD y aplicamos el multi-filtro en memoria.
            // Así soportamos selección múltiple sin cambiar UseCase ni Room.
            getReleasesUseCase.forMonth(month.year, month.monthValue, null, _uiState.value.regionFilter)
                .catch { e ->
                    Log.e(TAG, "Flow error: ${e.message}", e)
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { allReleases ->
                    val filters = _uiState.value.platformFilters
                    val releases = if (filters.isEmpty()) allReleases
                                   else allReleases.filter { it.platform in filters }
                    val isRealMonth = month == YearMonth.now()
                    _uiState.update { s ->
                        s.copy(
                            releases = releases,
                            timelineReleases = if (isRealMonth) releases else s.timelineReleases,
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
        _uiState.update { state ->
            val newFilters = when {
                platform == null            -> emptySet()          // "Todos" → limpiar todo
                platform in state.platformFilters -> state.platformFilters - platform  // deseleccionar
                else                        -> state.platformFilters + platform        // seleccionar
            }
            state.copy(platformFilters = newFilters)
        }
        loadMonth(_uiState.value.currentMonth)
    }

    fun onRegionFilter(region: Region?) {
        _uiState.update { it.copy(regionFilter = region) }
        loadMonth(_uiState.value.currentMonth)
    }

    fun toggleWishlist(release: Release) {
        viewModelScope.launch {
            val game = release.game
            if (game.id in _uiState.value.wishlistedIds) {
                wishlistUseCase.remove(game.id)
            } else {
                wishlistUseCase.add(game)
            }
        }
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
