package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AnimeRepository
import com.example.data.repository.LocalLicensedMediaProvider
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
}
