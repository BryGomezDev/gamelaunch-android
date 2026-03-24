package com.gamelaunch.presentation.wishlist

import androidx.lifecycle.ViewModel
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.usecase.WishlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    wishlistUseCase: WishlistUseCase
) : ViewModel() {

    val wishlist: StateFlow<List<Game>> = wishlistUseCase.getWishlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
