package com.gamelaunch.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.usecase.SearchGamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20
private const val MAX_HISTORY = 5

data class SearchUiState(
    val query: String = "",
    val results: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val recentSearches: List<String> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGamesUseCase: SearchGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var currentOffset = 0

    init {
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
        if (query.isEmpty()) _uiState.update { it.copy(results = emptyList(), canLoadMore = false) }
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

    private fun addToHistory(query: String) {
        _uiState.update { state ->
            val updated = (listOf(query) + state.recentSearches.filter { it != query })
                .take(MAX_HISTORY)
            state.copy(recentSearches = updated)
        }
    }
}
