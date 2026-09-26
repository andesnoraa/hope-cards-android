package com.aaronsedna.hopecards.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import java.io.File
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtFiles
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtEligibility
import com.aaronsedna.hopecards.model.VerseArtCatalog
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.runBlocking

class VerseArtInstrumentedTest {
    @get:Rule val composeRule = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun drawerCategoryArtworkAndBackNavigation() {
        val settings = AppRepository(context)
        runBlocking { settings.initialize() }
        val reference = checkNotNull(VerseRepository(context).byId("psalm-46-10", runBlocking { settings.currentSettings().preferredTranslation })).displayReference
        val gallery = runBlocking { VerseArtImages.gallery(context, settings.currentSettings().preferredTranslation) }
        val available = gallery.references
        val rotationSettings = context.getSharedPreferences("verse-art-rotation", android.content.Context.MODE_PRIVATE)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            composeRule.waitUntil(15_000) {
                runCatching { composeRule.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
            }
            composeRule.onNodeWithContentDescription("Open navigation").performClick()
            capture("drawer")
            composeRule.onNodeWithText("Verse Gallery").performClick()
            composeRule.waitUntil(15_000) { runCatching { composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed() }.isSuccess }
            val visit = rotationSettings.getLong("visit", -1)
            capture("categories")
            composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed().performClick()
            composeRule.onNodeWithText("${VerseArtCatalog.inCategory("peace", emptySet()).count { it.id in available }} artworks").assertIsDisplayed()
            composeRule.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-peace"))
            capture("gallery")
            composeRule.onNodeWithTag("art-open-peace").assertIsDisplayed().performClick()
            composeRule.onNodeWithTag("art-detail-image").assertIsDisplayed()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription(reference).assertIsDisplayed() }.isSuccess }
            capture("detail")
            val wasSaved = runCatching { composeRule.onNodeWithContentDescription("Remove artwork from Saved Verses").assertIsDisplayed() }.isSuccess
            val before = if (wasSaved) "Remove artwork from Saved Verses" else "Save artwork to Saved Verses"
            val after = if (wasSaved) "Save artwork to Saved Verses" else "Remove artwork from Saved Verses"
            composeRule.onNodeWithContentDescription(before).performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription(after).assertIsDisplayed() }.isSuccess }
            scenario.recreate()
            composeRule.waitUntil(15_000) { runCatching { composeRule.onNodeWithContentDescription(after).assertIsDisplayed() }.isSuccess }
            assertEquals("Recreation must preserve the displayed background", visit, rotationSettings.getLong("visit", -1))
            composeRule.onNodeWithContentDescription(after).assertIsDisplayed().performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription(before).assertIsDisplayed() }.isSuccess }
            // Isolate navigation assertions from a network-delivered interstitial overlay.
            // Ad cadence/transition behavior is covered by ContentBreakTest and share-navigation tests.
            scenario.onActivity { androidx.lifecycle.ViewModelProvider(it)[HopeCardsViewModel::class.java].ads.close() }
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithTag("art-open-peace").assertIsDisplayed() }.isSuccess }
            val last = gallery.ordered(VerseArtCatalog.inCategory("peace", emptySet())).last()
            composeRule.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${last.id}"))
            composeRule.onNodeWithTag("art-open-${last.id}").performClick()
            composeRule.onNodeWithTag("art-detail-image").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithTag("art-open-${last.id}").assertIsDisplayed() }.isSuccess }
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed()
            composeRule.onNodeWithTag("art-category-hope").performClick()
            composeRule.onNodeWithText("${VerseArtCatalog.inCategory("hope", emptySet()).count { it.id in available }} artworks").assertIsDisplayed()
            val firstHope = gallery.ordered(VerseArtCatalog.inCategory("hope", emptySet())).first()
            composeRule.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${firstHope.id}"))
            composeRule.onNodeWithTag("art-open-${firstHope.id}").assertIsDisplayed()
            capture("hope-gallery")
            assertEquals("Browsing categories must preserve the visit", visit, rotationSettings.getLong("visit", -1))
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Open navigation").performClick()
            composeRule.onNodeWithText("Bible Quiz").performClick()
            composeRule.onNodeWithContentDescription("Open navigation").performClick()
            composeRule.onNodeWithText("Verse Gallery").performClick()
            composeRule.waitUntil(15_000) { rotationSettings.getLong("visit", -1) != visit }
            assertEquals(visit + 1, rotationSettings.getLong("visit", -1))
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureVerseArt") != "true") return
        composeRule.waitForIdle()
        // UiAutomation captures the display compositor, which can trail Compose semantics by a frame.
        android.os.SystemClock.sleep(250)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val folder = File(context.getExternalFilesDir(null), "verse-art-ui").apply { mkdirs() }
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

}
