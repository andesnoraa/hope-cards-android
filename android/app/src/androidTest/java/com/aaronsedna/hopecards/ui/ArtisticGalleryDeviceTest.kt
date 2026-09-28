package com.aaronsedna.hopecards.ui

import android.content.Context
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
import com.aaronsedna.hopecards.data.VerseArtRenderer
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class ArtisticGalleryDeviceTest {
    @get:Rule val compose = createEmptyComposeRule()

    private data class Sample(val edition: Translation, val category: String, val artwork: String, val verse: String,
        val file: String, val rotation: Long? = null, val background: String? = null)

    @Test fun selectedEditionArtworkIsVisibleOnThePhone(): Unit = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val rotationPreferences = context.getSharedPreferences("verse-art-rotation", Context.MODE_PRIVATE)
        val originalVisit = if (rotationPreferences.contains("visit")) rotationPreferences.getLong("visit", 0) else null
        val output = File(context.getExternalFilesDir(null), "artistic-gallery-device").apply { mkdirs() }
        try {
            val samples = if (InstrumentationRegistry.getArguments().getString("approvedSeptember27Only") == "true") {
                val renderer = VerseArtRenderer(context)
                val art = checkNotNull(VerseArtCatalog.artwork("peace-psalm-56-3"))
                listOf(Translation.WEB, Translation.MAL1910).flatMap { edition ->
                    val verse = VerseArtImages.verse(context, art, edition)
                    listOf("scene-water-lily-stillness", "scene-coast-after-rain", "scene-vineyard-light",
                        "scene-firefly-evening").map { background ->
                        val rotation = (0L..99L).first { renderer.design(verse, it).background == background }
                        Sample(edition, art.categoryId, art.id, art.verseId, "$background-${edition.id}", rotation, background)
                    }
                }
            } else if (InstrumentationRegistry.getArguments().getString("quietBranchOnly") == "true") {
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
                sample.rotation?.let { assertTrue(rotationPreferences.edit().putLong("visit", it - 1).commit()) }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    scenario.onActivity { it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
                    }
                    scenario.onActivity { ViewModelProvider(it)[HopeCardsViewModel::class.java].navigate(Destination.VERSE_ART) }
                    compose.waitUntil(15_000) { runCatching { compose.onNodeWithTag("art-category-${sample.category}").assertIsDisplayed() }.isSuccess }
                    sample.rotation?.let { rotation ->
                        assertEquals(rotation, rotationPreferences.getLong("visit", Long.MIN_VALUE))
                        // Do not await VerseArtImages' mutex while Compose owns the Main test
                        // scheduler: a pending thumbnail may need that scheduler to release it.
                        val verse = checkNotNull(VerseRepository(context).byId(sample.verse, edition))
                        assertEquals(sample.background,
                            VerseArtRenderer(context).design(verse, rotation).background)
                    }
                    compose.onNodeWithTag("art-category-${sample.category}").performClick()
                    compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${sample.artwork}"))
                    compose.onNodeWithTag("art-open-${sample.artwork}").performClick()
                    compose.onNodeWithText("${edition.language} (${edition.label})").assertIsDisplayed()
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
        } finally {
            repository.replaceSettings(original)
            val restore = rotationPreferences.edit()
            if (originalVisit == null) restore.remove("visit") else restore.putLong("visit", originalVisit)
            assertTrue(restore.commit())
        }
    }
}
