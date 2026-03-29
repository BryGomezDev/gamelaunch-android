package com.gamelaunch.presentation.calendar

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.gamelaunch.domain.model.Game
import com.gamelaunch.domain.model.Platform
import com.gamelaunch.domain.model.Region
import com.gamelaunch.domain.model.Release
import com.gamelaunch.domain.repository.GameRepository
import com.gamelaunch.domain.usecase.GetReleasesUseCase
import com.gamelaunch.util.MainDispatcherRule
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@ExperimentalCoroutinesApi
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getReleasesUseCase: GetReleasesUseCase = mockk()
    private val repository: GameRepository = mockk()
    private val dataStore: DataStore<Preferences> = mockk()

    private lateinit var viewModel: CalendarViewModel

    private fun fakeRelease(
        id: Int,
        date: LocalDate = LocalDate.now(),
        platform: Platform = Platform.STEAM
    ) = Release(
        id = id,
        game = Game(id, "Game $id", null, date, listOf(platform), emptyList(), null, null),
        date = date,
        platform = platform,
        region = Region.WORLDWIDE
    )

    @Before
    fun setup() {
        every { dataStore.data } returns flowOf(preferencesOf())
        coEvery { dataStore.updateData(any()) } returns preferencesOf()
        every { getReleasesUseCase.forMonth(any(), any(), any(), any()) } returns flowOf(emptyList())
        coEvery { repository.syncMonth(any(), any()) } just Runs

        viewModel = CalendarViewModel(getReleasesUseCase, repository, dataStore)
    }

    @Test
    fun `initial state has current month`() {
        assertEquals(YearMonth.now(), viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `initial state has no selected day`() {
        assertNull(viewModel.uiState.value.selectedDay)
    }

    @Test
    fun `releases from use case populate state`() = runTest {
        val today = LocalDate.now()
        val releases = listOf(fakeRelease(1, today), fakeRelease(2, today))
        every { getReleasesUseCase.forMonth(any(), any(), any(), any()) } returns flowOf(releases)

        viewModel = CalendarViewModel(getReleasesUseCase, repository, dataStore)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.releases.size)
    }

    @Test
    fun `onMonthChange updates currentMonth and resets selectedDay`() = runTest {
        val today = LocalDate.now()
        viewModel.onDaySelected(today)

        val nextMonth = YearMonth.now().plusMonths(1)
        viewModel.onMonthChange(nextMonth)
        advanceUntilIdle()

        assertEquals(nextMonth, viewModel.uiState.value.currentMonth)
        assertNull(viewModel.uiState.value.selectedDay)
    }

    @Test
    fun `onDaySelected populates selectedDayReleases`() = runTest {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val releases = listOf(fakeRelease(1, today), fakeRelease(2, tomorrow))
        every { getReleasesUseCase.forMonth(any(), any(), any(), any()) } returns flowOf(releases)

        viewModel = CalendarViewModel(getReleasesUseCase, repository, dataStore)
        advanceUntilIdle()

        viewModel.onDaySelected(today)

        val state = viewModel.uiState.value
        assertEquals(today, state.selectedDay)
        assertEquals(1, state.selectedDayReleases.size)
        assertEquals(1, state.selectedDayReleases.first().id)
    }

    @Test
    fun `onDaySelected with no releases shows empty selectedDayReleases`() = runTest {
        viewModel.onDaySelected(LocalDate.now())
        assertTrue(viewModel.uiState.value.selectedDayReleases.isEmpty())
    }

    @Test
    fun `onPlatformFilter updates platformFilter and reloads`() = runTest {
        viewModel.onPlatformFilter(Platform.STEAM)
        advanceUntilIdle()

        assertEquals(Platform.STEAM, viewModel.uiState.value.platformFilter)
    }

    @Test
    fun `onPlatformFilter with null clears filter`() = runTest {
        viewModel.onPlatformFilter(Platform.STEAM)
        viewModel.onPlatformFilter(null)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.platformFilter)
    }

    @Test
    fun `onRegionFilter updates regionFilter`() = runTest {
        viewModel.onRegionFilter(Region.EUROPE)
        advanceUntilIdle()

        assertEquals(Region.EUROPE, viewModel.uiState.value.regionFilter)
    }

    @Test
    fun `fetchMonthIfEmpty triggers syncMonth when Room is empty`() = runTest {
        advanceUntilIdle()
        coVerify { repository.syncMonth(any(), any()) }
    }

    @Test
    fun `fetchMonthIfEmpty skips sync when Room has data`() = runTest {
        val releases = listOf(fakeRelease(1, LocalDate.now()))
        every { getReleasesUseCase.forMonth(any(), any(), any(), any()) } returns flowOf(releases)

        clearMocks(repository, answers = false)
        viewModel = CalendarViewModel(getReleasesUseCase, repository, dataStore)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.syncMonth(any(), any()) }
    }

    @Test
    fun `refresh calls syncMonth and clears isRefreshing`() = runTest {
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertNull(viewModel.uiState.value.error)
        coVerify(atLeast = 1) { repository.syncMonth(any(), any()) }
    }

    @Test
    fun `refresh sets error state on network failure`() = runTest {
        coEvery { repository.syncMonth(any(), any()) } throws RuntimeException("Network failure")
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network failure"))
    }

    @Test
    fun `flow error sets error state`() = runTest {
        every { getReleasesUseCase.forMonth(any(), any(), any(), any()) } returns
                kotlinx.coroutines.flow.flow { throw RuntimeException("DB error") }

        viewModel = CalendarViewModel(getReleasesUseCase, repository, dataStore)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }
}
