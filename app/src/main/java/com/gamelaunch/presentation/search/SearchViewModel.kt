package com.gamelaunch.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.usecase.SearchGamesUseCase
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val MAX_HISTORY = 5
private val HISTORY_KEY = stringPreferencesKey("search_history")

data class SearchUiState(
    val query: String = "",
    val results: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val error: String? = null,
    val genreFilter: String? = null,
    val gameModeFilter: String? = null
) {
    val availableGenres: List<String>
        get() = results.flatMap { it.genres }.distinct().sorted()

    val availableGameModes: List<String>
        get() = results.flatMap { it.gameModes }.distinct().sorted()

    val filteredResults: List<Game>
        get() = results
            .filter { genreFilter == null || genreFilter in it.genres }
            .filter { gameModeFilter == null || gameModeFilter in it.gameModes }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGamesUseCase: SearchGamesUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var currentOffset = 0

    init {
        // Load persisted history on start
        viewModelScope.launch {
            val saved = dataStore.data.first()[HISTORY_KEY]
            if (!saved.isNullOrEmpty()) {
                _uiState.update { it.copy(recentSearches = saved.split(",")) }
            }
        }

        queryFlow
            .debounce(400)
            .filter { it.length >= 2 }
            .distinctUntilChanged()
            .onEach { query ->
                currentOffset = 0
                _uiState.update { it.copy(isLoading = true, error = null, results = emptyList()) }
                try {
                    val results = searchGamesUseCase(query, offset = 0)
                    if (results.isNotEmpty()) addToHistory(query)
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            canLoadMore = results.size == PAGE_SIZE
                        )
                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
        if (query.isEmpty()) _uiState.update { it.copy(results = emptyList(), canLoadMore = false, genreFilter = null, gameModeFilter = null) }
    }

    fun onHistorySelected(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    fun loadMore() {
        val query = _uiState.value.query
        if (!_uiState.value.canLoadMore || _uiState.value.isLoadingMore || query.length < 2) return
        viewModelScope.launch {
            currentOffset += PAGE_SIZE
            _uiState.update { it.copy(isLoadingMore = true) }
            try {
                val more = searchGamesUseCase(query, offset = currentOffset)
                _uiState.update { state ->
                    state.copy(
                        results = state.results + more,
                        isLoadingMore = false,
                        canLoadMore = more.size == PAGE_SIZE
                    )
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                currentOffset -= PAGE_SIZE
                _uiState.update { it.copy(isLoadingMore = false, error = e.message) }
            }
        }
    }

    fun onGenreFilter(genre: String?) { _uiState.update { it.copy(genreFilter = genre) } }
    fun onGameModeFilter(mode: String?) { _uiState.update { it.copy(gameModeFilter = mode) } }

    private fun addToHistory(query: String) {
        val updated = (listOf(query) + _uiState.value.recentSearches.filter { it != query })
            .take(MAX_HISTORY)
        _uiState.update { it.copy(recentSearches = updated) }
        viewModelScope.launch {
            dataStore.edit { it[HISTORY_KEY] = updated.joinToString(",") }
        }
    }
}
