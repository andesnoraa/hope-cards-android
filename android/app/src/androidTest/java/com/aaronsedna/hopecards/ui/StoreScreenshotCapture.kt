package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtImages
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

    @Test fun captureVerseImageScreenshots() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("captureVerseImages") == "true" && BuildConfig.SCREENSHOT_MODE)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        val original = runBlocking { repository.initialize(); repository.currentSettings() }
        val locale = checkNotNull(args.getString("captureLocale"))
        val edition = mapOf("en-US" to Translation.BSB, "ml-IN" to Translation.MAL1910,
            "fil-PH" to Translation.ADB1905).getValue(locale)
        val output = File(context.getExternalFilesDir(null), "verse-image-captures/$locale").apply { mkdirs() }
        val manifest = mutableListOf("file\tcategory\tartwork\treference\tedition")
        try {
            runBlocking {
                repository.replaceSettings(original.copy(preferredTranslation = edition,
                    enableHaptics = false, dailyHopeMusicEnabled = false, themeName = ThemeName.CLASSIC))
            }
            val gallery = runBlocking { VerseArtImages.gallery(context, edition) }
            val choices = VerseArtCatalog.categories.map { category ->
                val candidates = gallery.ordered(VerseArtCatalog.inCategory(category.id, emptySet()))
                // Skip the Hope image already delivered in the earlier listing screenshots.
                category to candidates[if (category.id == "hope") 1 else 0]
            } + listOf(VerseArtCatalog.categories.first() to
                gallery.ordered(VerseArtCatalog.inCategory("hope", emptySet()))[2])
            runBlocking { choices.forEach { (_, art) -> VerseArtImages.image(context, art, edition, 1080) } }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                lateinit var vm: HopeCardsViewModel
                scenario.onActivity {
                    vm = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                    it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                compose.waitUntil(20_000) { vm.uiState.value.initialized && vm.uiState.value.dailyVerse != null }
                compose.mainClock.advanceTimeBy(5_000)
                compose.waitForIdle()
                compose.runOnIdle { vm.navigate(Destination.VERSE_ART) }
                val back = context.forTranslation(edition).getString(R.string.back)
                choices.forEachIndexed { index, (category, art) ->
                    compose.waitUntil(30_000) {
                        runCatching { compose.onNodeWithTag("art-browse-all").assertIsDisplayed() }.isSuccess
                    }
                    compose.onNodeWithTag("art-category-${category.id}").performScrollTo().performClick()
                    compose.waitUntil(30_000) {
                        runCatching { compose.onNodeWithTag("art-open-${art.id}").assertIsDisplayed() }.isSuccess
                    }
                    compose.onNodeWithTag("art-open-${art.id}").performClick()
                    val reference = gallery.references.getValue(art.id)
                    compose.waitUntil(30_000) {
                        runCatching { compose.onNodeWithTag("art-detail-image").assertIsDisplayed()
                            compose.onNodeWithContentDescription(reference).assertIsDisplayed() }.isSuccess
                    }
                    compose.mainClock.advanceTimeBy(5_000)
                    repeat(4) { Thread.sleep(200); compose.waitForIdle() }
                    val name = "%02d-%s-%s.png".format(index + 1, category.title, art.verseId)
                    val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                    File(output, name).outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                    bitmap.recycle()
                    manifest += "$name\t${category.title}\t${art.id}\t$reference\t${edition.id}"
                    println("Captured verse image $locale/$name")
                    compose.onNodeWithContentDescription(back).performClick()
                    compose.waitForIdle()
                    compose.onNodeWithContentDescription(back).performClick()
                }
            }
            File(output, "manifest.tsv").writeText(manifest.joinToString("\n") + "\n")
        } finally { runBlocking { repository.replaceSettings(original) } }
    }

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
        val promo = args.getString("capturePromo") == "true"
        val promoReady = File(context.filesDir, "promo-ready")
        val promoGo = File(context.filesDir, "promo-go")
        val promoDone = File(context.filesDir, "promo-done")
        val promoScenes = File(context.filesDir, "promo-scenes.tsv")
        var promoStart = 0L
        if (promo) {
            listOf(promoReady, promoGo, promoDone, promoScenes).forEach { it.delete() }
        }
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
                    repository.replaceSettings(AppSettings(
                        preferredTranslation = translation, enableHaptics = false, dailyHopeMusicEnabled = false,
                        themeName = ThemeName.fromId(args.getString("captureTheme")),
                    ))
                    repository.replaceFavorites(setOf("matthew-11-28", "john-14-27", "philippians-4-13"))
                    repository.replaceJournalEntries(listOf(JournalEntry(
                        "$today:matthew-11-28", today, "matthew-11-28", "Matthew 11:28",
                        "comfort", notes.getValue(locale), "${today}T08:00:00Z",
                    )))
                    repository.setDailyHopeRecord(DailyHopeRecord(today, dailyVerseId, translation.id))
                    if (promo) {
                        val gallery = VerseArtImages.gallery(context, translation)
                        val art = gallery.ordered(VerseArtCatalog.inCategory("hope", emptySet())).take(6)
                        VerseArtCatalog.categories.forEach { category ->
                            VerseArtImages.image(context, VerseArtCatalog.artwork(category.coverArtworkId)!!, translation, 360)
                        }
                        art.forEach { VerseArtImages.image(context, it, translation, 360) }
                        VerseArtImages.image(context, art.first(), translation, 1080)
                    }
                }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var vm: HopeCardsViewModel
                    scenario.onActivity {
                        vm = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    compose.waitUntil(20_000) { vm.uiState.value.initialized && vm.uiState.value.dailyVerse != null }
                    fun capture(name: String) {
                        // Daily Hope deliberately staggers its entrance over 2.7 seconds.
                        compose.mainClock.advanceTimeBy(5_000)
                        compose.waitForIdle()
                        // Let cached artwork callbacks recompose before capturing device pixels.
                        repeat(3) {
                            Thread.sleep(200)
                            compose.waitForIdle()
                        }
                        if (promo) {
                            if (promoStart == 0L) {
                                promoReady.writeText("ready")
                                val deadline = android.os.SystemClock.elapsedRealtime() + 120_000
                                while (!promoGo.exists()) {
                                    check(android.os.SystemClock.elapsedRealtime() < deadline) { "Recording did not start" }
                                    Thread.sleep(200)
                                }
                                promoStart = android.os.SystemClock.elapsedRealtime()
                            }
                            val elapsed = android.os.SystemClock.elapsedRealtime() - promoStart
                            promoScenes.appendText("$elapsed\t$name\n")
                            repeat(if (name == "09-art-detail.png") 35 else 25) {
                                Thread.sleep(200)
                                compose.waitForIdle()
                            }
                        }
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
                        if (args.getString("captureArt") == "true") {
                            val gallery = runBlocking { VerseArtImages.gallery(context, translation) }
                            val visibleArt = gallery.ordered(VerseArtCatalog.inCategory("hope", emptySet())).take(6)
                            runBlocking {
                                VerseArtCatalog.categories.forEach { category ->
                                    val art = VerseArtCatalog.artwork(category.coverArtworkId)!!
                                    VerseArtImages.image(context, art, translation, 360)
                                }
                                visibleArt.forEach { VerseArtImages.image(context, it, translation, 360) }
                            }
                            compose.runOnIdle { vm.navigate(Destination.VERSE_ART) }
                            compose.waitUntil(30_000) {
                                runCatching { compose.onNodeWithTag("art-browse-all").assertIsDisplayed() }.isSuccess
                            }
                            capture("07-art-categories.png")
                            compose.onNodeWithTag("art-category-hope").performClick()
                            compose.waitUntil(30_000) {
                                runCatching { compose.onNodeWithTag("art-gallery").assertIsDisplayed() }.isSuccess
                            }
                            capture("08-art-gallery.png")
                            val detailArt = visibleArt.first()
                            runBlocking { VerseArtImages.image(context, detailArt, translation, 1080) }
                            compose.onNodeWithTag("art-open-${detailArt.id}").performClick()
                            compose.waitUntil(30_000) {
                                runCatching { compose.onNodeWithTag("art-detail-image").assertIsDisplayed() }.isSuccess
                            }
                            capture("09-art-detail.png")
                        }
                        compose.runOnIdle { vm.navigate(Destination.SETTINGS) }
                        compose.onNodeWithText(localized.getString(R.string.settings_theme)).performClick()
                        capture("06-themes.png")
                        if (promo) {
                            promoScenes.appendText("${android.os.SystemClock.elapsedRealtime() - promoStart}\tEND\n")
                            promoDone.writeText("done")
                            Thread.sleep(2_000)
                        }
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
