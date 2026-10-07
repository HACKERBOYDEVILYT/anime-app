package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AnimeRepository
import com.example.data.repository.LocalLicensedMediaProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName.isNotBlank())
    }

    @Test
    fun `anime repository returns catalog and search works`() = runTest {
        val provider = LocalLicensedMediaProvider()
        val repo = AnimeRepository(provider)

        val trending = repo.getTrending()
        assertTrue("Trending list should not be empty", trending.isNotEmpty())

        val frieren = repo.search("Frieren")
        assertTrue("Search for Frieren should return results", frieren.isNotEmpty())
        assertEquals("Frieren: Beyond Journey's End", frieren.first().titleEnglish)

        val episodes = repo.getEpisodes(frieren.first().id)
        assertTrue("Should return episodes with HLS sources", episodes.isNotEmpty())
        assertNotNull(episodes.first().sources.firstOrNull())
    }

    @Test
    fun `continue watching returns last watched anime ordered by local watch history with progress bar percentage`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.KuroDatabase::class.java
        ).allowMainThreadQueries().build()

        val watchRepo = com.example.data.repository.WatchRepository(
            watchDao = db.watchDao(),
            watchlistDao = db.watchlistDao(),
            socialDao = db.socialDao()
        )

        watchRepo.saveWatchProgress(
            animeId = "anime_1",
            animeTitle = "Frieren: Beyond Journey's End",
            episodeId = "anime_1_ep_7",
            episodeNumber = 7,
            episodeTitle = "Like a Fairy Tale",
            posterUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            progressMs = 720_000L,
            durationMs = 1440_000L
        )

        val list = watchRepo.getContinueWatching().first()
        assertTrue("Continue watching list should not be empty", list.isNotEmpty())
        val latest = list.first()
        assertEquals("Frieren: Beyond Journey's End", latest.animeTitle)
        assertEquals(0.5f, latest.percentage, 0.01f)
        db.close()
    }
}
