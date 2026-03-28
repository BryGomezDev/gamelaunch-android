package com.gamelaunch.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: GameRepository,
    private val wishlistUseCase: WishlistUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadGame(gameId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val game = repository.getGameDetail(gameId)
                val wishlisted = repository.isInWishlist(gameId)
                _uiState.update {
                    it.copy(game = game, isWishlisted = wishlisted, isLoading = false)
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.update { it.copy(isLoading = false, error = "No se pudo cargar el juego: ${e.message}") }
            }
        }
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
    }
}
