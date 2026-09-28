package com.aaronsedna.hopecards.ui

import android.os.Debug
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReleaseLifecycleQualityTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun repeatedGalleryAndNavigationSettleAfterBackgrounding(): Unit = runBlocking {
        val repository = AppRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        repository.initialize()
        val settings = repository.currentSettings()
        try {
            repository.updateSettings { it.copy(preferredTranslation = Translation.BSB) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var model: HopeCardsViewModel
                scenario.onActivity {
                    it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                }
                compose.waitUntil(15_000) { model.uiState.value.initialized }
                fun cycle() {
                    scenario.onActivity { model.navigate(Destination.VERSE_ART) }
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithTag("art-category-peace").assertIsDisplayed() }.isSuccess
                    }
                    compose.waitForIdle()
                    assertTrue(VerseArtImages.memoryBytes() <= 8 * 1024 * 1024)
                    for (destination in listOf(Destination.FAVORITES, Destination.JOURNAL,
                        Destination.REMOVE_ADS, Destination.SETTINGS, Destination.HOME)) {
                        scenario.onActivity { model.navigate(destination) }
                        compose.waitForIdle()
                    }
                    scenario.moveToState(Lifecycle.State.CREATED)
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitForIdle()
                }
                repeat(2) { cycle() }
                val baseline = heap()
                val samples = (1..6).map { batch ->
                    repeat(4) { cycle() }
                    heap().also { println("Release navigation heap batch=$batch bytes=$it baseline=$baseline cache=${VerseArtImages.memoryBytes()}") }
                }
                assertTrue("Later navigation batch retained over 16 MiB", samples.last() - samples[samples.lastIndex - 1] < 16L * 1024 * 1024)
                scenario.moveToState(Lifecycle.State.CREATED)
                SystemClock.sleep(2_000)
                val cpuStart = android.os.Process.getElapsedCpuTime()
                val timeStart = SystemClock.elapsedRealtime()
                SystemClock.sleep(30_000)
                println("Background observation elapsedMs=${SystemClock.elapsedRealtime() - timeStart} processCpuMs=${android.os.Process.getElapsedCpuTime() - cpuStart}")
            }
        } finally { repository.replaceSettings(settings) }
    }

    private fun heap(): Long {
        SystemClock.sleep(500)
        Runtime.getRuntime().gc()
        System.runFinalization()
        Runtime.getRuntime().gc()
        return Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() } + Debug.getNativeHeapAllocatedSize()
    }
}
