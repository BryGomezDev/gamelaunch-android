package com.gamelaunch.presentation.search

import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.usecase.SearchGamesUseCase
import com.gamelaunch.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchGamesUseCase: SearchGamesUseCase = mockk()
    private lateinit var viewModel: SearchViewModel

    private fun fakeGame(id: Int) = Game(
        id = id, name = "Game $id", coverUrl = null,
        releaseDate = LocalDate.now(), platforms = emptyList(),
        genres = listOf("RPG"), rating = null, summary = null
    )

    @Before
    fun setup() {
        viewModel = SearchViewModel(searchGamesUseCase)
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `query shorter than 2 chars does not trigger search`() = runTest {
        viewModel.onQueryChange("a")
        advanceTimeBy(500)
        coVerify(exactly = 0) { searchGamesUseCase(any(), any()) }
    }

    @Test
    fun `valid query triggers search and shows results`() = runTest {
        val games = listOf(fakeGame(1), fakeGame(2))
        coEvery { searchGamesUseCase("mario", 0) } returns games

        viewModel.onQueryChange("mario")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(games, state.results)
        assertNull(state.error)
    }

    @Test
    fun `error from use case sets error state`() = runTest {
        coEvery { searchGamesUseCase("fail", 0) } throws RuntimeException("Network error")

        viewModel.onQueryChange("fail")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `clearing query resets results`() = runTest {
        val games = listOf(fakeGame(1))
        coEvery { searchGamesUseCase("mario", 0) } returns games

        viewModel.onQueryChange("mario")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.onQueryChange("")

        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertFalse(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun `loadMore appends results to existing list`() = runTest {
        val page1 = List(20) { fakeGame(it) }
        val page2 = listOf(fakeGame(20), fakeGame(21))
        coEvery { searchGamesUseCase("mario", 0) } returns page1
        coEvery { searchGamesUseCase("mario", 20) } returns page2

        viewModel.onQueryChange("mario")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(22, state.results.size)
        assertFalse(state.canLoadMore) // page2 < 20 items
    }

    @Test
    fun `successful search adds query to history`() = runTest {
        coEvery { searchGamesUseCase("zelda", 0) } returns listOf(fakeGame(1))

        viewModel.onQueryChange("zelda")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals(listOf("zelda"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `history keeps at most 5 entries and deduplicates`() = runTest {
        val queries = listOf("game1", "game2", "game3", "game4", "game5", "game6")
        queries.forEach { query ->
            coEvery { searchGamesUseCase(query, 0) } returns listOf(fakeGame(1))
            viewModel.onQueryChange(query)
            advanceTimeBy(500)
            advanceUntilIdle()
        }

        val history = viewModel.uiState.value.recentSearches
        assertEquals(5, history.size)
        assertEquals("game6", history.first())
        assertFalse(history.contains("game1"))
    }

    @Test
    fun `onHistorySelected sets query and triggers search`() = runTest {
        coEvery { searchGamesUseCase("zelda", 0) } returns listOf(fakeGame(1))

        viewModel.onHistorySelected("zelda")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals("zelda", viewModel.uiState.value.query)
        assertEquals(1, viewModel.uiState.value.results.size)
    }
}
