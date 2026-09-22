package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.*
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Opt-in capture utility. Uses the real app UI and restores user-authored data afterward. */
class StoreScreenshotCapture {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun captureDrawerThemes(): Unit = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("captureDrawer") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val output = File(context.getExternalFilesDir(null), "drawer-review").apply { mkdirs() }
        fun capture(name: String) {
            compose.waitForIdle()
            android.os.SystemClock.sleep(300)
            val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        try {
            for (theme in ThemeName.entries) {
                repository.updateSettings { it.copy(themeName = theme) }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var vm: HopeCardsViewModel
                    scenario.onActivity { vm = ViewModelProvider(it)[HopeCardsViewModel::class.java] }
                    compose.waitUntil(15_000) { vm.uiState.value.initialized }
                    compose.runOnIdle { vm.navigate(Destination.VERSE_ART) }
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithTag("art-browse-saved").assertIsDisplayed() }.isSuccess
                    }
                    compose.onNodeWithTag("art-browse-all").assertIsDisplayed()
                    capture("gallery-${theme.id}")
                    compose.onNodeWithContentDescription("Open navigation").performClick()
                    compose.onNodeWithText("Remove ads").assertIsDisplayed()
                    capture("drawer-${theme.id}")
                }
            }
        } finally { repository.replaceSettings(original) }
    }

    @Test fun captureListingScreenshots() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("captureStore") == "true" && BuildConfig.SCREENSHOT_MODE)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        val originalSettings = runBlocking { repository.currentSettings() }
        val originalFavorites = runBlocking { repository.currentFavorites() }
        val originalJournal = runBlocking { repository.currentJournalEntries() }
        val originalDaily = runBlocking { repository.getDailyHopeRecord() }
        val device = args.getString("captureDevice") ?: "phone"
        val dailyVerseId = args.getString("captureDailyVerseId") ?: "philippians-4-13"
        val locales = listOf(
            "en-US" to Translation.BSB, "es-419" to Translation.RV1909,
            "fr-FR" to Translation.LSG1910, "de-DE" to Translation.LUT1912,
            "it-IT" to Translation.RIV1927, "ml-IN" to Translation.MAL1910,
            "fil-PH" to Translation.ADB1905,
        ).filter { args.getString("captureLocale") == null || it.first == args.getString("captureLocale") }
        val notes = mapOf(
            "en-US" to "Today I will pause, breathe, and trust God with the next step.",
            "es-419" to "Hoy voy a hacer una pausa, respirar y confiar en Dios para dar el siguiente paso.",
            "fr-FR" to "Aujourd’hui, je prends le temps de respirer et de confier mon prochain pas à Dieu.",
            "de-DE" to "Heute halte ich inne, atme durch und vertraue Gott meinen nächsten Schritt an.",
            "it-IT" to "Oggi mi fermo, respiro e affido a Dio il mio prossimo passo.",
            "ml-IN" to "ഇന്ന് ഞാൻ ശാന്തമായി ശ്വസിച്ച് എന്റെ അടുത്ത ചുവട് ദൈവത്തെ ഏൽപ്പിക്കും.",
            "fil-PH" to "Ngayon, hihinto muna ako, hihinga nang malalim, at magtitiwala sa Diyos sa susunod kong hakbang.",
        )
        val today = LocalDate.now(ZoneOffset.UTC).toString()
        try {
            for ((locale, translation) in locales) {
                runBlocking {
                    repository.initialize()
                    repository.replaceSettings(AppSettings(preferredTranslation = translation, enableHaptics = false, dailyHopeMusicEnabled = false))
                    repository.replaceFavorites(setOf("matthew-11-28", "john-14-27", "philippians-4-13"))
                    repository.replaceJournalEntries(listOf(JournalEntry(
                        "$today:matthew-11-28", today, "matthew-11-28", "Matthew 11:28",
                        "comfort", notes.getValue(locale), "${today}T08:00:00Z",
                    )))
                    repository.setDailyHopeRecord(DailyHopeRecord(today, dailyVerseId, translation.id))
                }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var vm: HopeCardsViewModel
                    scenario.onActivity { vm = ViewModelProvider(it)[HopeCardsViewModel::class.java] }
                    compose.waitUntil(20_000) { vm.uiState.value.initialized && vm.uiState.value.dailyVerse != null }
                    fun capture(name: String) {
                        // Daily Hope deliberately staggers its entrance over 2.7 seconds.
                        compose.mainClock.advanceTimeBy(5_000)
                        compose.waitForIdle()
                        Thread.sleep(300)
                        val file = File(context.getExternalFilesDir(null), "store-captures/$device/$locale/$name")
                        file.parentFile!!.mkdirs()
                        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                        file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                        bitmap.recycle()
                        println("Captured $device/$locale/$name")
                    }
                    capture("01-card-back.png")
                    val localized = context.forTranslation(translation)
                    compose.onNodeWithText(localized.getString(R.string.draw_a_card)).performClick()
                    compose.waitForIdle()
                    compose.runOnIdle {
                        // Select the same real, concise verse in every Bible translation.
                        var attempts = 0
                        while (vm.uiState.value.currentVerse?.id != "matthew-11-28" && attempts++ < 20_000) vm.nextCard()
                        check(vm.uiState.value.currentVerse?.id == "matthew-11-28")
                    }
                    capture("02-card-front.png")
                    if (device == "phone") {
                        compose.runOnIdle { vm.navigate(Destination.DAILY) }
                        capture("03-daily-hope.png")
                        compose.runOnIdle { vm.navigate(Destination.FAVORITES) }
                        capture("04-favorites.png")
                        compose.runOnIdle { vm.navigate(Destination.JOURNAL) }
                        capture("05-journal.png")
                        compose.runOnIdle { vm.navigate(Destination.SETTINGS) }
                        compose.onNodeWithText(localized.getString(R.string.settings_theme)).performClick()
                        capture("06-themes.png")
                    }
                }
            }
        } finally {
            runBlocking {
                repository.replaceBackupData(originalSettings, originalFavorites, originalJournal)
                originalDaily?.let { repository.setDailyHopeRecord(it) }
            }
        }
    }
}
