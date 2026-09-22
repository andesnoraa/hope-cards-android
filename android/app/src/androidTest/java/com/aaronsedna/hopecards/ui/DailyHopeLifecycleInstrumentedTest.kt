package com.aaronsedna.hopecards.ui

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.os.Build
import android.media.AudioManager
import android.os.Debug
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.Destination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DailyHopeLifecycleInstrumentedTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun repeatedDailyHopeNavigationReleasesMusicAndSettlesMemory(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val audio = context.getSystemService(AudioManager::class.java)
        try {
            repository.updateSettings { it.copy(dailyHopeMusicEnabled = true) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var model: HopeCardsViewModel
                scenario.onActivity {
                    it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                }
                compose.waitUntil(15_000) { model.uiState.value.initialized }
                fun cycle() {
                    scenario.onActivity { model.navigate(Destination.DAILY) }
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    scenario.moveToState(Lifecycle.State.CREATED)
                    compose.waitUntil(5_000) { !audio.isMusicActive }
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    scenario.onActivity { model.navigate(Destination.HOME) }
                    compose.waitUntil(5_000) { !audio.isMusicActive }
                    compose.waitForIdle()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    scenario.onActivity { model.navigate(Destination.DAILY) }
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    val interruption = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build())
                        .setOnAudioFocusChangeListener { }
                        .build()
                    try {
                        scenario.onActivity {
                            org.junit.Assert.assertEquals(AudioManager.AUDIOFOCUS_REQUEST_GRANTED, audio.requestAudioFocus(interruption))
                        }
                        compose.waitUntil(5_000) { !audio.isMusicActive }
                    } finally { scenario.onActivity { audio.abandonAudioFocusRequest(interruption) } }
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    scenario.onActivity { model.navigate(Destination.HOME) }
                    compose.waitUntil(5_000) { !audio.isMusicActive }
                    println("Transient audio interruption paused music; focus gain resumed it.")
                }
                repeat(2) { cycle() } // Warm the app and media framework before comparing.
                val before = settledHeapBytes()
                val samples = (1..3).map { batch ->
                    repeat(8) { cycle() }
                    settledHeapBytes().also { println("Daily Hope lifecycle: after ${batch * 8} cycles heap=$it baseline=$before bytes") }
                }
                // A later batch should settle after media/UI/JIT warm-up, not grow each cycle.
                assertTrue("Later navigation batch retained over 16 MiB", samples.last() - samples[1] < 16L * 1024 * 1024)
            }
            compose.waitUntil(5_000) { !audio.isMusicActive }
        } finally {
            repository.replaceSettings(original)
        }
    }

    private fun settledHeapBytes(): Long {
        // Let asynchronous platform media teardown finish before sampling native allocations.
        android.os.SystemClock.sleep(500)
        Runtime.getRuntime().gc()
        System.runFinalization()
        Runtime.getRuntime().gc()
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory() + Debug.getNativeHeapAllocatedSize()
    }
}
