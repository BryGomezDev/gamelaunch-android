package com.gamelaunch.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gamelaunch.data.local.dao.GameDao
import com.gamelaunch.data.local.entity.GameEntity
import com.gamelaunch.data.local.entity.ReleaseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var db: GameDatabase
    private lateinit var dao: GameDao

    private fun game(id: Int, name: String = "Game $id") = GameEntity(
        id = id, name = name, coverUrl = null, genres = "RPG", rating = 85f, summary = null
    )

    private fun release(id: Int, gameId: Int, dateEpoch: Long, platformId: Int = 167, regionId: Int = 8) =
        ReleaseEntity(id = id, gameId = gameId, platformId = platformId, regionId = regionId, dateEpoch = dateEpoch)

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GameDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.gameDao()
    }

    @After
    fun teardown() = db.close()

    // ── Games ────────────────────────────────────────────────────────────────

    @Test
    fun upsertAndRetrieveGame() = runTest {
        dao.upsertGames(listOf(game(1)))
        val result = dao.getGameById(1)
        assertEquals("Game 1", result?.name)
    }

    @Test
    fun upsertUpdatesExistingGame() = runTest {
        dao.upsertGames(listOf(game(1, "Original")))
        dao.upsertGames(listOf(game(1, "Updated")))
        assertEquals("Updated", dao.getGameById(1)?.name)
    }

    @Test
    fun getGameById_returnsNull_whenNotFound() = runTest {
        assertNull(dao.getGameById(999))
    }

    // ── Wishlist ─────────────────────────────────────────────────────────────

    @Test
    fun addToWishlist_marksGame() = runTest {
        dao.upsertGames(listOf(game(1)))
        dao.addToWishlist(1)
        assertTrue(dao.isWishlisted(1) ?: false)
    }

    @Test
    fun removeFromWishlist_unmarksGame() = runTest {
        dao.upsertGames(listOf(game(1)))
        dao.addToWishlist(1)
        dao.removeFromWishlist(1)
        assertFalse(dao.isWishlisted(1) ?: true)
    }

    @Test
    fun getWishlist_returnsOnlyWishlisted() = runTest {
        dao.upsertGames(listOf(game(1), game(2), game(3)))
        dao.addToWishlist(1)
        dao.addToWishlist(3)
        val wishlist = dao.getWishlist().first()
        assertEquals(2, wishlist.size)
        assertTrue(wishlist.all { it.isWishlisted })
    }

    @Test
    fun upsertGameAndWishlist_isAtomic() = runTest {
        val g = game(10)
        dao.upsertGameAndWishlist(g)
        assertTrue(dao.isWishlisted(10) ?: false)
    }

    // ── Releases ─────────────────────────────────────────────────────────────

    @Test
    fun getReleasesForRange_returnsOnlyMatchingDates() = runTest {
        dao.upsertGames(listOf(game(1), game(2), game(3)))
        dao.upsertReleases(listOf(
            release(id = 1, gameId = 1, dateEpoch = 1000L),  // inside range
            release(id = 2, gameId = 2, dateEpoch = 2000L),  // inside range
            release(id = 3, gameId = 3, dateEpoch = 5000L)   // outside range
        ))
        val results = dao.getReleasesForRange(500L, 3000L).first()
        assertEquals(2, results.size)
        assertTrue(results.none { it.release.id == 3 })
    }

    @Test
    fun getReleasesForRange_returnsCorrectGameData() = runTest {
        dao.upsertGames(listOf(game(1, "Mario")))
        dao.upsertReleases(listOf(release(id = 1, gameId = 1, dateEpoch = 1000L)))
        val result = dao.getReleasesForRange(0L, 9999L).first()
        assertEquals("Mario", result.first().game.name)
    }

    @Test
    fun deleteReleasesForRange_removesOnlyMatchingDates() = runTest {
        dao.upsertGames(listOf(game(1), game(2)))
        dao.upsertReleases(listOf(
            release(id = 1, gameId = 1, dateEpoch = 1000L),
            release(id = 2, gameId = 2, dateEpoch = 9000L)
        ))
        dao.deleteReleasesForRange(0L, 5000L)
        val remaining = dao.getReleasesForRange(0L, Long.MAX_VALUE).first()
        assertEquals(1, remaining.size)
        assertEquals(2, remaining.first().release.id)
    }

    @Test
    fun getEarliestReleaseEpoch_returnsMinimum() = runTest {
        dao.upsertGames(listOf(game(1)))
        dao.upsertReleases(listOf(
            release(id = 1, gameId = 1, dateEpoch = 3000L),
            release(id = 2, gameId = 1, dateEpoch = 1000L),
            release(id = 3, gameId = 1, dateEpoch = 2000L)
        ))
        assertEquals(1000L, dao.getEarliestReleaseEpoch(1))
    }

    @Test
    fun getEarliestReleaseEpoch_returnsNull_whenNoReleases() = runTest {
        dao.upsertGames(listOf(game(1)))
        assertNull(dao.getEarliestReleaseEpoch(1))
    }

    @Test
    fun getPlatformIdsForGame_returnsDistinctIds() = runTest {
        dao.upsertGames(listOf(game(1)))
        dao.upsertReleases(listOf(
            release(id = 1, gameId = 1, dateEpoch = 1000L, platformId = 167),
            release(id = 2, gameId = 1, dateEpoch = 2000L, platformId = 167), // duplicate
            release(id = 3, gameId = 1, dateEpoch = 3000L, platformId = 48)
        ))
        val platforms = dao.getPlatformIdsForGame(1)
        assertEquals(2, platforms.size)
        assertTrue(platforms.containsAll(listOf(167, 48)))
    }

    @Test
    fun cascadeDelete_removesReleasesWhenGameDeleted() = runTest {
        // Room CASCADE: deleting a game removes its releases
        dao.upsertGames(listOf(game(1)))
        dao.upsertReleases(listOf(release(id = 1, gameId = 1, dateEpoch = 1000L)))
        dao.deleteReleasesForRange(0L, Long.MAX_VALUE)
        val results = dao.getReleasesForRange(0L, Long.MAX_VALUE).first()
        assertTrue(results.isEmpty())
    }
}
