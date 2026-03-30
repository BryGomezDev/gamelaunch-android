package com.gamelaunch.presentation.detail

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.data.remote.TranslationService
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.repository.GameRepository
import com.gamelaunch.domain.usecase.WishlistUseCase
import com.gamelaunch.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val game: Game? = null,
    val isWishlisted: Boolean = false,
    val notifyDaysAhead: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isTranslating: Boolean = false,
    val showOriginalSummary: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: GameRepository,
    private val wishlistUseCase: WishlistUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val dataStore: DataStore<Preferences>,
    private val translationService: TranslationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadGame(gameId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val game = repository.getGameDetail(gameId)
                val wishlisted = repository.isInWishlist(gameId)
                val savedDays = dataStore.data.first()[intPreferencesKey("notify_days_$gameId")]
                _uiState.update {
                    it.copy(game = game, isWishlisted = wishlisted, isLoading = false, notifyDaysAhead = savedDays)
                }
                // Auto-translate on first load if no cached translation exists
                if (game != null && game.summaryEs == null && !game.summary.isNullOrBlank()) {
                    translateSummary(game)
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar el juego: ${e.message}") }
            }
        }
    }

    fun translateSummary(game: Game? = _uiState.value.game) {
        if (game == null || !translationService.isConfigured || game.summary.isNullOrBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true) }
            try {
                val translated = translationService.translateToSpanish(game.summary!!)
                if (translated != null) {
                    repository.saveTranslation(game.id, translated)
                    _uiState.update { state ->
                        state.copy(
                            game = state.game?.copy(summaryEs = translated),
                            isTranslating = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isTranslating = false) }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.update { it.copy(isTranslating = false) }
            }
        }
    }

    fun toggleSummaryLanguage() {
        _uiState.update { it.copy(showOriginalSummary = !it.showOriginalSummary) }
    }

    fun toggleWishlist() {
        val game = _uiState.value.game ?: return
        viewModelScope.launch {
            if (_uiState.value.isWishlisted) {
                wishlistUseCase.remove(game.id)
                _uiState.update { it.copy(isWishlisted = false) }
            } else {
                wishlistUseCase.add(game)
                _uiState.update { it.copy(isWishlisted = true) }
            }
        }
    }

    fun setNotifyDaysAhead(days: Int) {
        val game = _uiState.value.game ?: return
        notificationScheduler.scheduleReleaseNotification(game, days)
        _uiState.update { it.copy(notifyDaysAhead = days) }
        viewModelScope.launch {
            dataStore.edit { it[intPreferencesKey("notify_days_${game.id}")] = days }
        }
    }
}
