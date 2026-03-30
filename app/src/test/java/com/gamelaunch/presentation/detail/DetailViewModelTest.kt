package com.gamelaunch.presentation.detail

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import com.gamelaunch.domain.model.Game
import com.gamelaunch.data.remote.TranslationService
import com.gamelaunch.domain.repository.GameRepository
import com.gamelaunch.domain.usecase.WishlistUseCase
import com.gamelaunch.notification.NotificationScheduler
import com.gamelaunch.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: GameRepository = mockk()
    private val wishlistUseCase: WishlistUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)
    private val dataStore: DataStore<Preferences> = mockk()
    private val translationService: TranslationService = mockk(relaxed = true)

    private lateinit var viewModel: DetailViewModel

    private fun fakeGame(id: Int = 1) = Game(
        id = id, name = "Game $id", coverUrl = null,
        releaseDate = LocalDate.now(), platforms = emptyList(),
        genres = listOf("RPG"), rating = null, summary = null
    )

    @Before
    fun setup() {
        every { dataStore.data } returns flowOf(preferencesOf())
        coEvery { dataStore.updateData(any()) } returns preferencesOf()
        viewModel = DetailViewModel(repository, wishlistUseCase, notificationScheduler, dataStore, translationService)
    }

    @Test
    fun `initial state is loading with no game`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.game)
        assertNull(state.error)
    }

    @Test
    fun `loadGame populates game and wishlisted flag on success`() = runTest {
        val game = fakeGame(1)
        coEvery { repository.getGameDetail(1) } returns game
        coEvery { repository.isInWishlist(1) } returns false

        viewModel.loadGame(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(game, state.game)
        assertFalse(state.isWishlisted)
        assertNull(state.error)
    }

    @Test
    fun `loadGame sets isWishlisted true when game is in wishlist`() = runTest {
        coEvery { repository.getGameDetail(1) } returns fakeGame(1)
        coEvery { repository.isInWishlist(1) } returns true

        viewModel.loadGame(1)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isWishlisted)
    }

    @Test
    fun `loadGame restores saved notification days from DataStore`() = runTest {
        coEvery { repository.getGameDetail(1) } returns fakeGame(1)
        coEvery { repository.isInWishlist(1) } returns false
        every { dataStore.data } returns flowOf(preferencesOf(intPreferencesKey("notify_days_1") to 3))

        viewModel.loadGame(1)
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.notifyDaysAhead)
    }

    @Test
    fun `loadGame leaves notifyDaysAhead null when no saved preference`() = runTest {
        coEvery { repository.getGameDetail(1) } returns fakeGame(1)
        coEvery { repository.isInWishlist(1) } returns false
        // prefs is relaxed — returns null for any key by default

        viewModel.loadGame(1)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.notifyDaysAhead)
    }

    @Test
    fun `loadGame sets error state on exception`() = runTest {
        coEvery { repository.getGameDetail(1) } throws RuntimeException("Server error")

        viewModel.loadGame(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.game)
        assertEquals("No se pudo cargar el juego: Server error", state.error)
    }

    @Test
    fun `toggleWishlist adds game when not wishlisted`() = runTest {
        val game = fakeGame(1)
        coEvery { repository.getGameDetail(1) } returns game
        coEvery { repository.isInWishlist(1) } returns false
        coEvery { wishlistUseCase.add(game) } just Runs

        viewModel.loadGame(1)
        advanceUntilIdle()

        viewModel.toggleWishlist()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isWishlisted)
        coVerify { wishlistUseCase.add(game) }
    }

    @Test
    fun `toggleWishlist removes game when already wishlisted`() = runTest {
        val game = fakeGame(1)
        coEvery { repository.getGameDetail(1) } returns game
        coEvery { repository.isInWishlist(1) } returns true
        coEvery { wishlistUseCase.remove(game.id) } just Runs

        viewModel.loadGame(1)
        advanceUntilIdle()

        viewModel.toggleWishlist()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isWishlisted)
        coVerify { wishlistUseCase.remove(game.id) }
    }

    @Test
    fun `toggleWishlist does nothing when game not loaded`() = runTest {
        viewModel.toggleWishlist()
        advanceUntilIdle()

        coVerify(exactly = 0) { wishlistUseCase.add(any()) }
        coVerify(exactly = 0) { wishlistUseCase.remove(any()) }
    }

    @Test
    fun `setNotifyDaysAhead updates state and schedules notification`() = runTest {
        val game = fakeGame(1)
        coEvery { repository.getGameDetail(1) } returns game
        coEvery { repository.isInWishlist(1) } returns false

        viewModel.loadGame(1)
        advanceUntilIdle()

        viewModel.setNotifyDaysAhead(7)
        advanceUntilIdle()

        assertEquals(7, viewModel.uiState.value.notifyDaysAhead)
        verify { notificationScheduler.scheduleReleaseNotification(game, 7) }
    }

    @Test
    fun `setNotifyDaysAhead does nothing when game not loaded`() {
        viewModel.setNotifyDaysAhead(7)

        assertNull(viewModel.uiState.value.notifyDaysAhead)
        verify(exactly = 0) { notificationScheduler.scheduleReleaseNotification(any(), any()) }
    }
}
