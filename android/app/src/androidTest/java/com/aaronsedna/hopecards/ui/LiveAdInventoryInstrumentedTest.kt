package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.ads.AdsManager
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.InterstitialPolicy
import com.aaronsedna.hopecards.model.Destination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Network-dependent integration check. Only Google's official sample ad unit is allowed. */
class LiveAdInventoryInstrumentedTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun officialTestInterstitialShowsAtQuizBreakAndNeverOnDailyHope(): Unit = runBlocking {
        assertTrue(BuildConfig.DEBUG)
        assertEquals("ca-app-pub-3940256099942544/1033173712", BuildConfig.INTERSTITIAL_AD_UNIT_ID)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val repository = AppRepository(instrumentation.targetContext)
        repository.initialize()
        // This is the disposable debug package; do not touch production entitlement or counters.
        repository.recordInterstitialShown(System.currentTimeMillis() - InterstitialPolicy.MIN_INTERVAL_MS - 1_000)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var model: HopeCardsViewModel
            scenario.onActivity {
                it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
            }
            compose.waitUntil(15_000) { model.uiState.value.initialized }
            // Observe the cached SDK object in this unminified test build without adding a
            // production testing API or replacing the real network/ad presentation path.
            val cachedAd = AdsManager::class.java.getDeclaredField("interstitial").apply { isAccessible = true }
            compose.waitUntil(90_000) {
                var loaded = false
                scenario.onActivity { loaded = cachedAd.get(model.ads) != null }
                loaded
            }
            var dailyContinued = false
            scenario.onActivity {
                model.navigate(Destination.DAILY)
                model.ads.completeQuiz(it, false, { true }) { dailyContinued = true }
            }
            assertTrue(dailyContinued)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
            scenario.onActivity {
                assertNotNull(cachedAd.get(model.ads))
                model.navigate(Destination.BIBLE_QUIZ)
            }
            compose.waitForIdle()
            scenario.onActivity {
                model.ads.completeQuiz(it, false, { true }) { model.navigate(Destination.HOME) }
            }
            compose.waitUntil(10_000) { scenario.state != Lifecycle.State.RESUMED }
            SystemClock.sleep(2_000)
            val output = File(instrumentation.targetContext.getExternalFilesDir(null), "quality-ad-test.png")
            val capture = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
            output.outputStream().use { capture.compress(Bitmap.CompressFormat.PNG, 100, it) }
            capture.recycle()
            println("Official test interstitial presented. Screenshot=${output.name}. No ad click performed.")
            // Video test inventory can delay its close button and ignore Android Back.
            // Operate only the SDK's accessible close control, never the ad creative.
            fun closeControl(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
                if (node == null) return null
                val label = (node.contentDescription ?: node.text)?.toString()?.trim().orEmpty()
                if (node.isVisibleToUser && node.isEnabled && node.isClickable &&
                    label.lowercase() in setOf("close", "close ad", "close button")) return node
                for (index in 0 until node.childCount) closeControl(node.getChild(index))?.let { return it }
                return null
            }
            val deadline = SystemClock.elapsedRealtime() + 60_000
            var closed = false
            while (scenario.state != Lifecycle.State.RESUMED && SystemClock.elapsedRealtime() < deadline) {
                closeControl(instrumentation.uiAutomation.rootInActiveWindow)?.let {
                    println("Dismiss official test ad using accessible control: ${it.contentDescription ?: it.text}")
                    val bounds = android.graphics.Rect().apply { it.getBoundsInScreen(this) }
                    instrumentation.uiAutomation.executeShellCommand("input tap ${bounds.centerX()} ${bounds.centerY()}").use { descriptor ->
                        java.io.FileInputStream(descriptor.fileDescriptor).use { input -> input.readBytes() }
                    }
                    closed = true
                }
                // Some inventory has a second close control on its end card.
                SystemClock.sleep(2_000)
            }
            assertTrue("The official test ad did not expose an accessible close control", closed)
            compose.waitUntil(15_000) { scenario.state == Lifecycle.State.RESUMED }
            assertEquals(Destination.HOME, model.uiState.value.destination)
        }
    }
}
