package com.aaronsedna.hopecards.ui

import android.content.Context
import android.media.AudioManager
import android.os.Debug
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.lang.ref.WeakReference

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

    /**
     * Opt in with -e stressCycles 60 [-e stressAds disabled|enabled]. No device is selected here.
     * Ten warm-up cycles precede ten measured batches. Disabled ads isolate app allocations;
     * enabled ads use only the existing official debug inventory, without changing entitlement.
     */
    @Test fun sustainedNavigationReleasesActivitiesAndBoundsLateHeapGrowth(): Unit = runBlocking {
        val arguments = InstrumentationRegistry.getArguments()
        val requestedCycles = arguments.getString("stressCycles")
        assumeTrue("Opt in to sustained QA with stressCycles=60", requestedCycles != null)
        val cycles = requireNotNull(requestedCycles?.toIntOrNull()) { "stressCycles must be an integer" }
        require(cycles in 60..240 && cycles % 10 == 0) { "Use 60..240 cycles, divisible by ten" }
        val adMode = arguments.getString("stressAds") ?: "disabled"
        require(adMode in setOf("disabled", "enabled")) { "stressAds must be disabled or enabled" }
        assertTrue("Stress QA must not request production advertising inventory", BuildConfig.DEBUG)
        assertEquals("ca-app-pub-3940256099942544/9214589741", BuildConfig.BANNER_AD_UNIT_ID)
        assertEquals("ca-app-pub-3940256099942544/1033173712", BuildConfig.INTERSTITIAL_AD_UNIT_ID)
        if (adMode == "enabled") assertTrue("Screenshot builds suppress advertisements", !BuildConfig.SCREENSHOT_MODE)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val originalSettings = repository.currentSettings()
        val originalDaily = repository.getDailyHopeRecord()
        val rotationPreferences = context.getSharedPreferences("verse-art-rotation", Context.MODE_PRIVATE)
        val originalRotation = rotationPreferences.getLong("visit", 0)
        val hadRotation = rotationPreferences.contains("visit")
        val audio = context.getSystemService(AudioManager::class.java)
        assertTrue("Stop other audio before measuring Daily Hope teardown", !audio.isMusicActive)
        val artReference = checkNotNull(VerseRepository(context.applicationContext)
            .byId("psalm-46-10", Translation.BSB)).displayReference
        val retiredActivities = mutableListOf<WeakReference<MainActivity>>()
        val retiredBanners = mutableListOf<WeakReference<AdView>>()
        val samples = mutableListOf<MemorySample>()
        val evidence = JSONObject().put("stressCycles", cycles).put("warmupCycles", 10)
            .put("adMode", adMode).put("versionCode", BuildConfig.VERSION_CODE)
            .put("versionName", BuildConfig.VERSION_NAME).put("samples", JSONArray())
        val output = File(context.getExternalFilesDir(null) ?: context.filesDir,
            "quality-lifecycle/stress-$adMode-${System.currentTimeMillis()}.json")
        fun saveEvidence() {
            check(output.parentFile?.isDirectory == true || output.parentFile?.mkdirs() == true)
            output.writeText(evidence.toString(2) + "\n")
        }
        println("Sustained lifecycle evidence: ${output.absolutePath}")
        try {
            repository.updateSettings { it.copy(preferredTranslation = Translation.BSB, dailyHopeMusicEnabled = true,
                dailyHopeReminderEnabled = false) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var model: HopeCardsViewModel
                fun bindActivity() {
                    scenario.onActivity {
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        model = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                        if (adMode == "disabled") model.ads.close()
                    }
                }
                bindActivity()
                compose.waitUntil(15_000) { model.uiState.value.initialized }
                if (adMode == "enabled") {
                    compose.waitUntil(20_000) { !model.billing.state.value.loading }
                    assertTrue("An ad-free profile cannot verify enabled-ad behavior", !model.billing.state.value.isAdFree)
                    compose.waitUntil(30_000) { model.ads.ready.value }
                }
                fun waitForTag(tag: String) {
                    compose.waitUntil(20_000) {
                        runCatching { compose.onNodeWithTag(tag).assertIsDisplayed() }.isSuccess
                    }
                }
                fun cycle() {
                    // The drawer resets remembered artwork/category state from the previous visit.
                    compose.onNodeWithContentDescription("Open navigation").performClick()
                    compose.onNodeWithText("Verse Gallery").performClick()
                    waitForTag("art-category-peace")
                    compose.onNodeWithTag("art-category-peace").performClick()
                    waitForTag("art-gallery")
                    compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-peace"))
                    compose.onNodeWithTag("art-open-peace").performClick()
                    waitForTag("art-detail-image")
                    // The container also exists while loading; require the actual image semantics.
                    compose.waitUntil(20_000) {
                        runCatching { compose.onNodeWithContentDescription(artReference).assertIsDisplayed() }.isSuccess
                    }
                    assertTrue("Bitmap cache exceeded its allocation budget", VerseArtImages.memoryBytes() <= 8 * 1024 * 1024)

                    // Direct navigation avoids an interstitial overlay controlling the stress loop.
                    // This still exercises banner creation/teardown and interstitial preloading.
                    scenario.onActivity { model.navigate(Destination.DAILY) }
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    scenario.moveToState(Lifecycle.State.CREATED)
                    compose.waitUntil(5_000) { !audio.isMusicActive }
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitUntil(10_000) { audio.isMusicActive }
                    scenario.onActivity { model.navigate(Destination.BIBLE_QUIZ) }
                    compose.waitUntil(5_000) { !audio.isMusicActive }
                    waitForTag("quiz_start")
                    for (destination in listOf(Destination.FAVORITES, Destination.JOURNAL,
                        Destination.REMOVE_ADS, Destination.SETTINGS, Destination.HOME)) {
                        scenario.onActivity { model.navigate(destination) }
                        compose.waitForIdle()
                        if (adMode == "enabled" && destination == Destination.FAVORITES) {
                            scenario.onActivity { activity ->
                                val banners = bannerReferences(activity.window.decorView)
                                assertEquals("Each stress cycle must exercise its real banner", 1, banners.size)
                                retiredBanners += banners
                            }
                        }
                    }
                    // Keep only weak references; a new Activity must not retain the old window.
                    scenario.onActivity { retiredActivities += WeakReference(it) }
                    scenario.recreate()
                    bindActivity()
                    compose.waitForIdle()
                }
                fun sample(completedCycles: Int): MemorySample {
                    scenario.moveToState(Lifecycle.State.CREATED)
                    SystemClock.sleep(1_500)
                    forceCollection()
                    val measurement = memorySample(completedCycles, retainedActivityCount(retiredActivities),
                        retainedBannerCount(retiredBanners))
                    evidence.getJSONArray("samples").put(measurement.json())
                    evidence.put("adsReadyAtSample", model.ads.ready.value)
                    saveEvidence()
                    println("Lifecycle stress ${measurement.json()}")
                    scenario.moveToState(Lifecycle.State.RESUMED)
                    compose.waitForIdle()
                    return measurement
                }
                repeat(10) { cycle() }
                sample(0)
                repeat(10) { batch ->
                    repeat(cycles / 10) { cycle() }
                    samples += sample((batch + 1) * (cycles / 10))
                }
                scenario.moveToState(Lifecycle.State.CREATED)
                val cpuStart = android.os.Process.getElapsedCpuTime()
                val timeStart = SystemClock.elapsedRealtime()
                SystemClock.sleep(30_000)
                evidence.put("backgroundWallMillis", SystemClock.elapsedRealtime() - timeStart)
                    .put("backgroundCpuMillis", android.os.Process.getElapsedCpuTime() - cpuStart)
                // With the 30-second CPU observation above, allow the SDK request cleanup's
                // bounded 60-second deadline to pass before the final collection assertion.
                val deadline = SystemClock.elapsedRealtime() + 40_000
                while ((retainedActivityCount(retiredActivities) > 0 || retainedBannerCount(retiredBanners) > 1) &&
                    SystemClock.elapsedRealtime() < deadline) {
                    forceCollection()
                    SystemClock.sleep(500)
                }
                val retained = retainedActivityCount(retiredActivities)
                val retainedAdViews = retainedBannerCount(retiredBanners)
                evidence.put("recreatedActivities", retiredActivities.size).put("retainedDestroyedActivities", retained)
                    .put("createdBanners", retiredBanners.size).put("retainedDetachedBanners", retainedAdViews)
                val late = samples.takeLast(6)
                val gain = late.last().heapBytes - late.first().heapBytes
                val slope = heapSlope(late)
                evidence.put("lateHeapGainBytes", gain).put("lateHeapSlopeBytesPerCycle", slope)
                saveEvidence()
                if (arguments.getString("dumpHeapOnFailure") == "true" &&
                    (retained > 0 || retainedAdViews > 1 || gain > 8L * 1024 * 1024 || slope > 128.0 * 1024)) {
                    // Capture only after CPU/heap measurements, while the failing roots still exist.
                    val heapFile = File(output.parentFile, output.nameWithoutExtension + ".hprof")
                    Debug.dumpHprofData(heapFile.absolutePath)
                    evidence.put("failureHeap", heapFile.absolutePath)
                    saveEvidence()
                }
                assertTrue("Daily Hope kept playing after navigation/backgrounding", !audio.isMusicActive)
                assertEquals("Destroyed Activities survived teardown and repeated GC; inspect $output", 0, retained)
                // Heap analysis identifies one application-context view held by the SDK's native
                // bridge after destroy. Verify that this stays bounded across all 70 creations,
                // alongside the stricter zero-Activity and late heap-growth requirements.
                assertTrue("Detached SDK banner retention accumulated; inspect $output", retainedAdViews <= 1)
                // Regression budgets catch v41's ~0.5 MiB/cycle tail without treating PSS
                // allocator reservation or a single GC fluctuation as proof of a leak.
                assertTrue("Late heap grew by more than 8 MiB; inspect $output", gain <= 8L * 1024 * 1024)
                assertTrue("Late heap trend exceeds 128 KiB per cycle; inspect $output", slope <= 128.0 * 1024)
                evidence.put("result", "passed")
                saveEvidence()
            }
        } catch (error: Throwable) {
            evidence.put("result", "failed").put("failure", error.toString())
            runCatching { saveEvidence() }
            throw error
        } finally {
            repository.replaceSettings(originalSettings)
            ReminderScheduler(context.applicationContext).let { reminders ->
                if (originalSettings.dailyHopeReminderEnabled) reminders.schedule(
                    originalSettings.dailyHopeReminderHour, originalSettings.dailyHopeReminderMinute,
                    originalSettings.preferredTranslation,
                ) else reminders.cancel()
            }
            originalDaily?.let { repository.setDailyHopeRecord(it) }
            rotationPreferences.edit().apply {
                if (hadRotation) putLong("visit", originalRotation) else remove("visit")
            }.commit()
        }
    }

    private data class MemorySample(
        val cycles: Int,
        val javaBytes: Long,
        val nativeBytes: Long,
        val pssBytes: Long,
        val graphicsBytes: Long,
        val bitmapCacheBytes: Int,
        val retainedActivities: Int,
        val retainedBanners: Int,
    ) {
        val heapBytes get() = javaBytes + nativeBytes
        fun json() = JSONObject().put("cycles", cycles).put("javaBytes", javaBytes)
            .put("nativeBytes", nativeBytes).put("pssBytes", pssBytes)
            .put("graphicsBytes", graphicsBytes).put("bitmapCacheBytes", bitmapCacheBytes)
            .put("retainedActivities", retainedActivities)
            .put("retainedBanners", retainedBanners)
    }

    private fun memorySample(cycles: Int, retainedActivities: Int, retainedBanners: Int): MemorySample {
        val runtime = Runtime.getRuntime()
        val info = Debug.MemoryInfo().also(Debug::getMemoryInfo)
        return MemorySample(cycles, runtime.totalMemory() - runtime.freeMemory(),
            Debug.getNativeHeapAllocatedSize(), info.totalPss.toLong() * 1024,
            (info.getMemoryStat("summary.graphics")?.toLongOrNull() ?: 0L) * 1024,
            VerseArtImages.memoryBytes(), retainedActivities, retainedBanners)
    }

    private fun heapSlope(samples: List<MemorySample>): Double {
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

    // Keep WeakReference.get() temporaries out of the frame that requests collection.
    private fun retainedActivityCount(references: List<WeakReference<MainActivity>>): Int =
        references.count { it.get() != null }

    private fun retainedBannerCount(references: List<WeakReference<AdView>>): Int =
        references.count { it.get() != null }

    private fun bannerReferences(root: View): List<WeakReference<AdView>> = buildList {
        fun visit(view: View) {
            if (view is AdView) add(WeakReference(view))
            else if (view is ViewGroup) repeat(view.childCount) { visit(view.getChildAt(it)) }
        }
        visit(root)
    }

    private fun heap(): Long {
        SystemClock.sleep(500)
        Runtime.getRuntime().gc()
        System.runFinalization()
        Runtime.getRuntime().gc()
        return Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() } + Debug.getNativeHeapAllocatedSize()
    }
}
