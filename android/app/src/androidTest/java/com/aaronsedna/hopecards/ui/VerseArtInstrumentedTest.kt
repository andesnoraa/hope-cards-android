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
import com.aaronsedna.hopecards.data.VerseArtFiles
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtCatalog
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class VerseArtInstrumentedTest {
    @get:Rule val composeRule = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun drawerCategoryArtworkAndBackNavigation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            composeRule.waitUntil(15_000) {
                runCatching { composeRule.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
            }
            composeRule.onNodeWithContentDescription("Open navigation").performClick()
            capture("drawer")
            composeRule.onNodeWithText("Verse Art").performClick()
            capture("categories")
            composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed().performClick()
            composeRule.onNodeWithText("30 artworks").assertIsDisplayed()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription("Be still").assertIsDisplayed() }.isSuccess }
            capture("gallery")
            composeRule.onNodeWithTag("art-open-peace").assertIsDisplayed().performClick()
            composeRule.onNodeWithTag("art-detail-image").assertIsDisplayed()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription("Psalm 46:10").assertIsDisplayed() }.isSuccess }
            capture("detail")
            val wasSaved = runCatching { composeRule.onNodeWithContentDescription("Remove artwork from Favorites").assertIsDisplayed() }.isSuccess
            val before = if (wasSaved) "Remove artwork from Favorites" else "Save artwork to Favorites"
            val after = if (wasSaved) "Save artwork to Favorites" else "Remove artwork from Favorites"
            composeRule.onNodeWithContentDescription(before).performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription(after).assertIsDisplayed() }.isSuccess }
            scenario.recreate()
            composeRule.onNodeWithContentDescription(after).assertIsDisplayed().performClick()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription(before).assertIsDisplayed() }.isSuccess }
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.onNodeWithTag("art-open-peace").assertIsDisplayed()
            val last = VerseArtCatalog.inCategory("peace", emptySet()).last()
            composeRule.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${last.id}"))
            composeRule.onNodeWithTag("art-open-${last.id}").performClick()
            composeRule.onNodeWithTag("art-detail-image").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.onNodeWithTag("art-open-${last.id}").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Back").performClick()
            composeRule.onNodeWithTag("art-category-peace").assertIsDisplayed()
            composeRule.onNodeWithTag("art-category-hope").performClick()
            composeRule.onNodeWithText("30 artworks").assertIsDisplayed()
            composeRule.waitUntil(5_000) { runCatching { composeRule.onNodeWithContentDescription("Abound in hope").assertIsDisplayed() }.isSuccess }
            capture("hope-gallery")
        }
    }

    @Test fun allCatalogAssetsDecodeAndTextMatchesBundledBible() {
        val verses = VerseRepository(context)
        assertEquals(180, VerseArtCatalog.artworks.size)
        assertEquals(180, VerseArtCatalog.artworks.map { it.id }.toSet().size)
        assertEquals(180, VerseArtCatalog.artworks.map { it.verseId }.toSet().size)
        assertEquals(180, VerseArtCatalog.artworks.map { it.text }.toSet().size)
        assertEquals(6, VerseArtCatalog.categories.size)
        assertEquals(180, VerseArtCatalog.categories.flatMap { it.artworkIds }.toSet().size)
        for (art in VerseArtCatalog.artworks) {
            val verse = checkNotNull(verses.byId(art.verseId, Translation.WEB))
            assertEquals(verse.verseTextForComparison(), art.text)
            for (path in listOf(art.assetPath, art.thumbnailPath)) {
                context.assets.open(path).use { stream ->
                    val bitmap = checkNotNull(BitmapFactory.decodeStream(stream))
                    assertEquals(bitmap.width, bitmap.height)
                    assertTrue(bitmap.width >= 360)
                    bitmap.recycle()
                }
            }
        }
        for (category in VerseArtCatalog.categories) {
            assertEquals(30, category.artworkIds.size)
            category.artworkIds.forEach { assertNotNull(VerseArtCatalog.artwork(it)) }
        }
    }

    @Test fun sharedImageIsReadableAndRetainsTheOriginalBytes() {
        val art = VerseArtCatalog.artworks.first()
        val intent = VerseArtFiles.shareIntent(context, art)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals(VerseArtFiles.MIME_TYPE, intent.type)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        val uri = checkNotNull(intent.clipData).getItemAt(0).uri
        val expected = context.assets.open(art.assetPath).use { it.readBytes() }
        val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
        assertArrayEquals(expected, actual)
    }

    @Test fun sharingReplacesExportsCachedBeforeAnArtworkUpdate() {
        val art = checkNotNull(VerseArtCatalog.artwork("hope-abound"))
        VerseArtFiles.shareIntent(context, art)
        val stale = File(context.cacheDir, "shared/verse-art/hope-cards-${art.id}.webp")
        stale.writeText("Artwork cached before the update")
        val intent = VerseArtFiles.shareIntent(context, art)
        val uri = checkNotNull(intent.clipData).getItemAt(0).uri
        val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
        assertArrayEquals(context.assets.open(art.assetPath).use { it.readBytes() }, actual)
    }

    @Test fun savedImageIsPublishedToPicturesWithoutStoragePermission() {
        assumeTrue(Build.VERSION.SDK_INT >= 29)
        val art = VerseArtCatalog.artworks.last()
        val uri = VerseArtFiles.saveToPhotos(context, art)
        try {
            val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
            assertArrayEquals(context.assets.open(art.assetPath).use { it.readBytes() }, actual)
            context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.IS_PENDING), null, null, null)!!.use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        } finally { context.contentResolver.delete(uri, null, null) }
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

    private fun com.aaronsedna.hopecards.model.Verse.verseTextForComparison() = text.trim('“', '”')
}
