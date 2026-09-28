package com.aaronsedna.hopecards.ui

import android.graphics.Rect
import android.os.Debug
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.abs

/** Network-dependent integration check against the real app and Google's official sample banner. */
class BannerLifecycleInstrumentedTest {
    @get:Rule val compose = createEmptyComposeRule()

    /**
     * Opt in with -e loadedBannerCycles 20 [-e dumpHeapOnFailure true].
     * Add -e settledHeapCheckpoints true for paired heaps after two 310-second background waits.
     */
    @Test fun repeatedSuccessfullyLoadedBannersRemainBounded(): Unit = runBlocking {
        val arguments = InstrumentationRegistry.getArguments()
        val requestedCycles = arguments.getString("loadedBannerCycles")
        assumeTrue("Opt in to loaded-banner QA with loadedBannerCycles=20", requestedCycles != null)
        val cycles = requireNotNull(requestedCycles?.toIntOrNull()) { "loadedBannerCycles must be an integer" }
        require(cycles in 20..100 && cycles % 10 == 0) { "Use 20..100 cycles, divisible by ten" }
        val settledHeapCheckpoints = arguments.getString("settledHeapCheckpoints") == "true"
        assertTrue("Only a debug build may request sample ads", BuildConfig.DEBUG)
        assertFalse("Screenshot mode hides banners", BuildConfig.SCREENSHOT_MODE)
        assertEquals(SAMPLE_BANNER, BuildConfig.BANNER_AD_UNIT_ID)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val settings = repository.currentSettings()
        val retiredBanners = mutableListOf<WeakReference<AdView>>()
        val retiredActivities = mutableListOf<WeakReference<MainActivity>>()
        val samples = mutableListOf<LoadedBannerMemory>()
        val evidence = JSONObject().put("versionCode", BuildConfig.VERSION_CODE)
            .put("adUnitId", SAMPLE_BANNER).put("warmupCycles", 5).put("measuredCycles", cycles)
            .put("loads", JSONArray()).put("samples", JSONArray())
            .put("processId", android.os.Process.myPid())
            .put("settledHeapCheckpointsEnabled", settledHeapCheckpoints)
        val output = File(context.getExternalFilesDir(null) ?: context.filesDir,
            "quality-lifecycle/loaded-banners-${System.currentTimeMillis()}.json")
        fun saveEvidence() {
            check(output.parentFile?.isDirectory == true || output.parentFile?.mkdirs() == true)
            output.writeText(evidence.toString(2) + "\n")
        }
        fun heapCheckpoint(label: String, completedCycles: Int, settleMillis: Long, hostState: String) {
            // Diagnostic telemetry belongs to its own array. Neither these waits nor HPROF work
            // add samples to the raw per-cycle trend or replace measurements taken before them.
            val checkpoint = JSONObject().put("label", label).put("cycles", completedCycles)
                .put("processId", android.os.Process.myPid()).put("hostState", hostState)
                .put("requestedSettleMillis", settleMillis).put("telemetry", JSONArray())
            evidence.getJSONArray("heapCheckpoints").put(checkpoint)
            forceCollection()
            val started = SystemClock.elapsedRealtime()
            checkpoint.put("startedElapsedMillis", started)
            fun telemetry() {
                val measurement = loadedBannerMemory(completedCycles, retainedCount(retiredBanners)).json()
                    .put("elapsedMillis", SystemClock.elapsedRealtime() - started)
                    .put("retainedHosts", retainedCount(retiredActivities))
                checkpoint.getJSONArray("telemetry").put(measurement)
                saveEvidence()
                println("Loaded-banner heap checkpoint $label $measurement")
            }
            telemetry()
            // The observed platform connection-pool keepalive is five minutes. Wait beyond it
            // without changing SDK flags or touching the pool, then collect before comparing heaps.
            while (SystemClock.elapsedRealtime() - started < settleMillis) {
                val remaining = settleMillis - (SystemClock.elapsedRealtime() - started)
                if (remaining > 0) SystemClock.sleep(minOf(30_000L, remaining))
                telemetry()
            }
            forceCollection()
            checkpoint.put("settledElapsedMillis", SystemClock.elapsedRealtime() - started)
                .put("beforeDump", loadedBannerMemory(completedCycles, retainedCount(retiredBanners)).json())
                .put("retainedHostsBeforeDump", retainedCount(retiredActivities))
            val heapFile = File(output.parentFile, "${output.nameWithoutExtension}-$label.hprof")
            checkpoint.put("heapFile", heapFile.absolutePath)
            saveEvidence()
            val dumpStarted = SystemClock.elapsedRealtime()
            Debug.dumpHprofData(heapFile.absolutePath)
            checkpoint.put("dumpDurationMillis", SystemClock.elapsedRealtime() - dumpStarted)
                .put("heapFileBytes", heapFile.length())
            saveEvidence()
            println("Loaded-banner heap checkpoint saved: $heapFile")
        }
        if (settledHeapCheckpoints) evidence.put("heapCheckpoints", JSONArray())
        println("Loaded-banner lifecycle evidence: $output")
        try {
            repository.updateSettings {
                it.copy(preferredTranslation = Translation.BSB, dailyHopeReminderEnabled = false)
            }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var model: HopeCardsViewModel
                scenario.onActivity {
                    it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                    retiredActivities += WeakReference(it)
                }
                compose.waitUntil(15_000) { model.uiState.value.initialized }
                compose.waitUntil(20_000) { !model.billing.state.value.loading }
                assertFalse("An ad-free profile cannot verify a rendered banner", model.billing.state.value.isAdFree)
                compose.waitUntil(30_000) { model.ads.ready.value }

                fun cycle() {
                    scenario.onActivity { model.navigate(Destination.FAVORITES) }
                    val loaded = waitForLoadedBanner(scenario)
                    // This helper never passes a strong View reference to the GC-calling frame.
                    verifyDistinctApplicationBanner(loaded.reference, retiredBanners)
                    retiredBanners += loaded.reference
                    evidence.getJSONArray("loads").put(loaded.json()
                        .put("creation", retiredBanners.size).put("applicationContext", true))
                    scenario.onActivity { model.navigate(Destination.HOME) }
                    compose.waitForIdle()
                    assertDetached(loaded.reference)
                    saveEvidence()
                }
                fun sample(completedCycles: Int): LoadedBannerMemory {
                    scenario.moveToState(Lifecycle.State.CREATED)
                    compose.mainClock.advanceTimeBy(1_000)
                    compose.waitForIdle()
                    SystemClock.sleep(1_500)
                    forceCollection()
                    val measurement = loadedBannerMemory(completedCycles, retainedCount(retiredBanners))
                    evidence.getJSONArray("samples").put(measurement.json())
                    saveEvidence()
                    println("Loaded-banner memory ${measurement.json()}")
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitForIdle()
                    return measurement
                }
                fun settledHeapCheckpoint(label: String, completedCycles: Int) {
                    scenario.moveToState(Lifecycle.State.CREATED)
                    compose.mainClock.advanceTimeBy(1_000)
                    compose.waitForIdle()
                    heapCheckpoint(label, completedCycles, 310_000, "CREATED")
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitForIdle()
                }

                repeat(5) { cycle() }
                sample(0)
                if (settledHeapCheckpoints) settledHeapCheckpoint("baseline", 0)
                repeat(10) { batch ->
                    repeat(cycles / 10) { cycle() }
                    samples += sample((batch + 1) * (cycles / 10))
                }
                // Save the final raw sample before either the settling pause or heap-dump overhead.
                if (settledHeapCheckpoints) settledHeapCheckpoint("end", cycles)
            }
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            retiredBanners.forEach(::assertDetached)
            val deadline = SystemClock.elapsedRealtime() + 20_000
            do {
                forceCollection()
                SystemClock.sleep(500)
            } while ((retainedCount(retiredBanners) > 1 || retainedCount(retiredActivities) > 0) &&
                SystemClock.elapsedRealtime() < deadline)

            val retainedBanners = retainedCount(retiredBanners)
            val retainedActivities = retainedCount(retiredActivities)
            val late = samples.takeLast(6)
            val gain = late.last().heapBytes - late.first().heapBytes
            val slope = loadedHeapSlope(late)
            evidence.put("createdBanners", retiredBanners.size).put("retainedBanners", retainedBanners)
                .put("destroyedHosts", retiredActivities.size).put("retainedDestroyedHosts", retainedActivities)
                .put("lateHeapGainBytes", gain).put("lateHeapSlopeBytesPerCycle", slope)
            saveEvidence()
            if (settledHeapCheckpoints) heapCheckpoint("after-close", cycles, 0, "DESTROYED")
            if (arguments.getString("dumpHeapOnFailure") == "true" &&
                (retainedBanners > 1 || retainedActivities > 0 || gain > 8L * 1024 * 1024 || slope > 128.0 * 1024)) {
                val heapFile = File(output.parentFile, output.nameWithoutExtension + ".hprof")
                Debug.dumpHprofData(heapFile.absolutePath)
                evidence.put("failureHeap", heapFile.absolutePath)
                saveEvidence()
            }
            assertEquals("Every cycle must complete a distinct sample banner load", cycles + 5, retiredBanners.size)
            assertEquals("Loaded banner SDK retained a destroyed host; inspect $output", 0, retainedActivities)
            assertTrue("Loaded banner retention accumulated; inspect $output", retainedBanners <= 1)
            assertTrue("Late loaded-banner heap grew by more than 8 MiB; inspect $output", gain <= 8L * 1024 * 1024)
            assertTrue("Late loaded-banner heap trend exceeds 128 KiB per cycle; inspect $output", slope <= 128.0 * 1024)
            evidence.put("result", "passed")
            saveEvidence()
        } catch (error: Throwable) {
            evidence.put("result", "failed").put("failure", error.toString())
            runCatching { saveEvidence() }
            throw error
        } finally {
            repository.replaceSettings(settings)
            ReminderScheduler(context.applicationContext).let { reminders ->
                if (settings.dailyHopeReminderEnabled) reminders.schedule(
                    settings.dailyHopeReminderHour, settings.dailyHopeReminderMinute,
                    settings.preferredTranslation,
                ) else reminders.cancel()
            }
        }
    }

    @Test fun officialSampleBannerLoadsResumesAndReleasesItsHost(): Unit = runBlocking {
        assertTrue("Only a debug build may request sample ads", BuildConfig.DEBUG)
        assertFalse("Screenshot mode hides banners", BuildConfig.SCREENSHOT_MODE)
        assertEquals(SAMPLE_BANNER, BuildConfig.BANNER_AD_UNIT_ID)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val settings = repository.currentSettings()
        val retiredBanners = mutableListOf<WeakReference<AdView>>()
        val retiredActivities = mutableListOf<WeakReference<MainActivity>>()
        val evidence = JSONObject().put("versionCode", BuildConfig.VERSION_CODE)
            .put("adUnitId", SAMPLE_BANNER).put("loads", JSONArray())
        val output = File(context.getExternalFilesDir(null) ?: context.filesDir,
            "quality-lifecycle/banner-${System.currentTimeMillis()}.json")
        fun saveEvidence() {
            check(output.parentFile?.isDirectory == true || output.parentFile?.mkdirs() == true)
            output.writeText(evidence.toString(2) + "\n")
        }
        try {
            repository.updateSettings {
                it.copy(preferredTranslation = Translation.BSB, dailyHopeReminderEnabled = false)
            }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var model: HopeCardsViewModel
                fun bindActivity() {
                    scenario.onActivity {
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                    }
                }
                bindActivity()
                compose.waitUntil(15_000) { model.uiState.value.initialized }
                compose.waitUntil(20_000) { !model.billing.state.value.loading }
                assertFalse("An ad-free profile cannot verify a rendered banner", model.billing.state.value.isAdFree)
                compose.waitUntil(30_000) { model.ads.ready.value }

                fun loadBanner(label: String): LoadedBanner {
                    scenario.onActivity { model.navigate(Destination.FAVORITES) }
                    val loaded = waitForLoadedBanner(scenario)
                    verifyDistinctApplicationBanner(loaded.reference, retiredBanners)
                    evidence.getJSONArray("loads").put(loaded.json().put("stage", label))
                    saveEvidence()
                    retiredBanners += loaded.reference
                    return loaded
                }

                val first = loadBanner("initial")
                scenario.moveToState(Lifecycle.State.CREATED)
                SystemClock.sleep(1_000)
                scenario.moveToState(Lifecycle.State.RESUMED)
                compose.waitForIdle()
                // A pause/resume should preserve the loaded view and restore its visible content.
                scenario.onActivity { activity ->
                    val resumed = findBanners(activity.window.decorView).single()
                    assertSame("Resuming needlessly recreated the loaded banner", first.reference.get(), resumed)
                    assertTrue(resumed.isShown && resumed.isAttachedToWindow)
                    assertTrue(!resumed.responseInfo?.responseId.isNullOrBlank())
                }

                scenario.onActivity { model.navigate(Destination.HOME) }
                compose.waitForIdle()
                assertDetached(first.reference)

                val second = loadBanner("after navigation")
                scenario.onActivity { retiredActivities += WeakReference(it) }
                scenario.recreate()
                bindActivity()
                compose.waitForIdle()
                assertDetached(second.reference)
                val recreated = waitForLoadedBanner(scenario)
                verifyDistinctApplicationBanner(recreated.reference, retiredBanners)
                retiredBanners += recreated.reference
                evidence.getJSONArray("loads").put(recreated.json().put("stage", "after recreation"))
                scenario.onActivity { retiredActivities += WeakReference(it) }
                saveEvidence()
                // ActivityScenario.close below releases the last visible banner and the final host.
            }
            // Wall-clock waiting does not drain the Compose test scheduler after the host closes.
            compose.mainClock.advanceTimeBy(1_000)
            compose.waitForIdle()
            retiredBanners.forEach(::assertDetached)
            // The current SDK can retain its most recent loaded view through a native bridge.
            // Only that final, application-context view is allowed; every earlier view must clear.
            val earlierBanners = retiredBanners.dropLast(1)
            val deadline = SystemClock.elapsedRealtime() + 20_000
            while ((retainedCount(earlierBanners) != 0 || retainedCount(retiredActivities) != 0) &&
                SystemClock.elapsedRealtime() < deadline) {
                Runtime.getRuntime().gc()
                System.runFinalization()
                Runtime.getRuntime().gc()
                SystemClock.sleep(500)
            }
            val retainedBanners = retainedCount(retiredBanners)
            val retainedActivities = retainedCount(retiredActivities)
            val retainedEarlierBanners = retainedCount(earlierBanners)
            retiredBanners.forEach(::assertDetached)
            evidence.put("retainedBanners", retainedBanners).put("retainedActivities", retainedActivities)
                .put("retainedEarlierBanners", retainedEarlierBanners)
                .put("retainedFinalBanner", retainedCount(retiredBanners.takeLast(1)))
            saveEvidence()
            assertEquals("An earlier released banner survived repeated GC; inspect $output", 0, retainedEarlierBanners)
            assertTrue("Banner retention accumulated; inspect $output", retainedBanners <= 1)
            assertEquals("Banner SDK retained a destroyed host; inspect $output", 0, retainedActivities)
            evidence.put("result", "passed")
            saveEvidence()
            println("Official sample banners loaded, resumed and released. No ad clicks. Evidence=$output")
        } catch (error: Throwable) {
            evidence.put("result", "failed").put("failure", error.toString())
            runCatching { saveEvidence() }
            throw error
        } finally {
            repository.replaceSettings(settings)
            ReminderScheduler(context.applicationContext).let { reminders ->
                if (settings.dailyHopeReminderEnabled) reminders.schedule(
                    settings.dailyHopeReminderHour, settings.dailyHopeReminderMinute,
                    settings.preferredTranslation,
                ) else reminders.cancel()
            }
        }
    }

    private fun waitForLoadedBanner(scenario: ActivityScenario<MainActivity>): LoadedBanner {
        var result: LoadedBanner? = null
        compose.waitUntil(90_000) {
            scenario.onActivity { activity ->
                val banners = findBanners(activity.window.decorView)
                if (banners.size != 1) return@onActivity
                val banner = banners.single()
                val response = banner.responseInfo ?: return@onActivity
                val responseId = response.responseId?.takeIf { it.isNotBlank() } ?: return@onActivity
                val adSize = banner.adSize ?: return@onActivity
                val visible = Rect()
                if (banner.childCount == 0 || !banner.isShown || !banner.getGlobalVisibleRect(visible)) return@onActivity
                val expectedWidth = adSize.getWidthInPixels(activity)
                val expectedHeight = adSize.getHeightInPixels(activity)
                if (banner.width <= 0 || banner.height <= 0) return@onActivity
                assertEquals(SAMPLE_BANNER, banner.adUnitId)
                assertTrue("Banner width differs from requested adaptive width", abs(banner.width - expectedWidth) <= 2)
                assertTrue("Banner height differs from requested adaptive height", abs(banner.height - expectedHeight) <= 2)
                assertTrue("Banner is clipped vertically", visible.height() >= banner.height - 2)
                result = LoadedBanner(WeakReference(banner), responseId,
                    response.mediationAdapterClassName.orEmpty(), banner.width, banner.height)
            }
            result != null
        }
        return checkNotNull(result)
    }

    private fun assertDetached(reference: WeakReference<AdView>) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            reference.get()?.let { banner ->
                assertNull("Released banner is still owned by a parent view", banner.parent)
                assertFalse("Released banner remains attached to a window", banner.isAttachedToWindow)
            }
        }
    }

    // Keep WeakReference.get() temporaries out of the frame that requests garbage collection.
    private fun retainedCount(references: List<WeakReference<*>>): Int =
        references.count { it.get() != null }

    private fun verifyDistinctApplicationBanner(
        reference: WeakReference<AdView>,
        previous: List<WeakReference<AdView>>,
    ) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val banner = checkNotNull(reference.get())
            assertSame("The SDK view must use application context", banner.context.applicationContext, banner.context)
            assertFalse("A cycle reused an already recorded banner", previous.any { it.get() === banner })
        }
    }

    private data class LoadedBannerMemory(
        val cycles: Int,
        val javaBytes: Long,
        val nativeBytes: Long,
        val pssBytes: Long,
        val graphicsBytes: Long,
        val retainedBanners: Int,
    ) {
        val heapBytes get() = javaBytes + nativeBytes
        fun json() = JSONObject().put("cycles", cycles).put("javaBytes", javaBytes)
            .put("nativeBytes", nativeBytes).put("pssBytes", pssBytes)
            .put("graphicsBytes", graphicsBytes).put("retainedBanners", retainedBanners)
    }

    private fun loadedBannerMemory(cycles: Int, retainedBanners: Int): LoadedBannerMemory {
        val runtime = Runtime.getRuntime()
        val info = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        return LoadedBannerMemory(cycles, runtime.totalMemory() - runtime.freeMemory(),
            Debug.getNativeHeapAllocatedSize(), info.totalPss.toLong() * 1024,
            (info.getMemoryStat("summary.graphics")?.toLongOrNull() ?: 0L) * 1024, retainedBanners)
    }

    private fun loadedHeapSlope(samples: List<LoadedBannerMemory>): Double {
        val meanX = samples.map { it.cycles }.average()
        val meanY = samples.map { it.heapBytes }.average()
        val covariance = samples.sumOf { (it.cycles - meanX) * (it.heapBytes - meanY) }
        val variance = samples.sumOf { (it.cycles - meanX) * (it.cycles - meanX) }
        return covariance / variance
    }

    private fun forceCollection() {
        repeat(2) {
            Runtime.getRuntime().gc()
            System.runFinalization()
            SystemClock.sleep(150)
        }
        Runtime.getRuntime().gc()
    }

    private fun findBanners(root: View): List<AdView> = buildList {
        fun visit(view: View) {
            if (view is AdView) add(view)
            else if (view is ViewGroup) repeat(view.childCount) { visit(view.getChildAt(it)) }
        }
        visit(root)
    }

    private data class LoadedBanner(
        val reference: WeakReference<AdView>,
        val responseId: String,
        val adapter: String,
        val widthPixels: Int,
        val heightPixels: Int,
    ) {
        fun json() = JSONObject().put("responseId", responseId).put("adapter", adapter)
            .put("widthPixels", widthPixels).put("heightPixels", heightPixels)
    }

    private companion object {
        const val SAMPLE_BANNER = "ca-app-pub-3940256099942544/9214589741"
    }
}
