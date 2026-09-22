package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File

class ArtisticGalleryDeviceTest {
    @get:Rule val compose = createEmptyComposeRule()

    private data class Sample(val edition: Translation, val category: String, val artwork: String, val verse: String, val file: String)

    @Test fun selectedEditionArtworkIsVisibleOnThePhone(): Unit = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val output = File(context.getExternalFilesDir(null), "artistic-gallery-device").apply { mkdirs() }
        try {
            val samples = if (InstrumentationRegistry.getArguments().getString("quietBranchOnly") == "true") {
                listOf(Translation.WEB, Translation.MAL1910).map { edition ->
                    val gallery = VerseArtImages.gallery(context, edition)
                    val art = VerseArtCatalog.artworks.first { gallery.scenes[it.id]?.background == "photo-quiet-branch" }
                    Sample(edition, art.categoryId, art.id, art.verseId, "quiet-branch-${edition.id}")
                }
            } else listOf(
                Sample(Translation.WEB, "hope", "hope-faithful", "hebrews-10-23", "faithful-web"),
                Sample(Translation.MAL1910, "hope", "hope-faithful", "hebrews-10-23", "faithful-mal1910"),
                Sample(Translation.LUT1912, "peace", "peace", "psalm-46-10", "peace-lut1912"),
                Sample(Translation.WEB, "strength", "strength-through-christ", "philippians-4-13", "original-lake-web"),
                Sample(Translation.WEB, "comfort", "comfort-psalm-23-1", "psalm-23-1", "original-deer-web"),
            )
            for (sample in samples) {
                val edition = sample.edition
                repository.updateSettings { it.copy(preferredTranslation = edition) }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    scenario.onActivity { it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
                    }
                    scenario.onActivity { ViewModelProvider(it)[HopeCardsViewModel::class.java].navigate(Destination.VERSE_ART) }
                    compose.waitUntil(15_000) { runCatching { compose.onNodeWithTag("art-category-${sample.category}").assertIsDisplayed() }.isSuccess }
                    compose.onNodeWithTag("art-category-${sample.category}").performClick()
                    compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${sample.artwork}"))
                    compose.onNodeWithTag("art-open-${sample.artwork}").performClick()
                    compose.onNodeWithText("${edition.language} · ${edition.label}").assertIsDisplayed()
                    val reference = checkNotNull(VerseRepository(context).byId(sample.verse, edition)).displayReference
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithContentDescription(reference).assertIsDisplayed() }.isSuccess
                    }
                    compose.waitForIdle()
                    compose.onNodeWithText(reference).assertIsDisplayed()
                    android.os.SystemClock.sleep(300)
                    val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                    File(output, "${sample.file}.png").outputStream().use {
                        screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    screenshot.recycle()
                }
            }
        } finally { repository.replaceSettings(original) }
    }
}
