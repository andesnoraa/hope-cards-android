package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
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
                        HopeCardsViewModel.prompts.getValue("comfort"), notes.getValue(locale), "${today}T08:00:00Z",
                    )))
                    repository.setDailyHopeRecord(DailyHopeRecord(today, "matthew-11-28", translation.id))
                }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var vm: HopeCardsViewModel
                    scenario.onActivity { vm = ViewModelProvider(it)[HopeCardsViewModel::class.java] }
                    compose.waitUntil(20_000) { vm.uiState.value.initialized && vm.uiState.value.dailyVerse != null }
                    fun capture(name: String) {
                        // Daily Hope deliberately staggers its entrance over 2.7 seconds.
                        compose.mainClock.advanceTimeBy(3_500)
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
                    compose.onNodeWithText("Draw a Card").performClick()
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
                        compose.onNodeWithText("Theme").performClick()
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
