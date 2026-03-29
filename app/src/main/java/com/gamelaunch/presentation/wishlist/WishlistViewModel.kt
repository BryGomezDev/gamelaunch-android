package com.gamelaunch.presentation.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.usecase.WishlistUseCase
import com.gamelaunch.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistUseCase: WishlistUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val wishlist: StateFlow<List<Game>> = wishlistUseCase.getWishlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFromWishlist(gameId: Int) {
        listOf(1, 3, 7).forEach { notificationScheduler.cancelReleaseNotification(gameId, it) }
        viewModelScope.launch { wishlistUseCase.remove(gameId) }
    }
}
