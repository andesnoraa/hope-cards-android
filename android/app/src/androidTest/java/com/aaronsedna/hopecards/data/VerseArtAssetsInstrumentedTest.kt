package com.aaronsedna.hopecards.data

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.model.VerseArtEligibility
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Asset and file checks run without a Compose rule or its test-controlled main dispatcher. */
class VerseArtAssetsInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun allCatalogAssetsDecodeAndTextMatchesBundledBible(): Unit = runBlocking {
        val verses = VerseRepository(context)
        assertEquals(181, VerseArtCatalog.artworks.size)
        assertEquals(181, VerseArtCatalog.artworks.map { it.id }.toSet().size)
        assertEquals(181, VerseArtCatalog.artworks.map { it.verseId }.toSet().size)
        assertEquals(181, VerseArtCatalog.artworks.map { it.text }.toSet().size)
        assertEquals(6, VerseArtCatalog.categories.size)
        assertEquals(181, VerseArtCatalog.categories.flatMap { it.artworkIds }.toSet().size)
        for (art in VerseArtCatalog.artworks) {
            val verse = checkNotNull(verses.byId(art.verseId, Translation.WEB))
            // Artwork uses display capitalization and quotation styling. Compare
            // every word in order, while the source verifier checks exact asset text.
            assertEquals(verse.text.bibleWords(), art.text.bibleWords())
            assertEquals(verse.reference, art.reference)
            if (!VerseArtEligibility.includes(verse)) continue
            val bitmap = VerseArtImages.image(context, art, Translation.WEB, 360)
            assertEquals(360, bitmap.width)
            assertEquals(360, bitmap.height)
        }
        for (category in VerseArtCatalog.categories) {
            assertEquals(if (category.id == "hope") 31 else 30, category.artworkIds.size)
            category.artworkIds.forEach { assertNotNull(VerseArtCatalog.artwork(it)) }
        }
    }

    @Test fun sharedImageIsReadableJpegWithTheSelectedEdition(): Unit = runBlocking {
        val art = VerseArtCatalog.artworks.first()
        val intent = VerseArtFiles.shareIntent(context, art)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("Shared from Hope Cards ❤️\nhttps://play.google.com/store/apps/details?id=com.aaronsedna.hopecards", intent.getStringExtra(Intent.EXTRA_TEXT))
        assertEquals(VerseArtFiles.MIME_TYPE, intent.type)
        assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        val uri = checkNotNull(intent.clipData).getItemAt(0).uri
        val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
        assertEquals(0xff.toByte(), actual[0])
        assertEquals(0xd8.toByte(), actual[1])
        val bitmap = checkNotNull(BitmapFactory.decodeByteArray(actual, 0, actual.size))
        assertEquals(1080, bitmap.width)
        assertEquals(1080, bitmap.height)
        bitmap.recycle()
        val malayalam = VerseArtFiles.shareIntent(context, art, Translation.MAL1910)
        val translatedUri = checkNotNull(malayalam.clipData).getItemAt(0).uri
        assertNotEquals(uri, translatedUri)
        val translated = checkNotNull(context.contentResolver.openInputStream(translatedUri)).use { it.readBytes() }
        assertFalse(actual.contentEquals(translated))
    }

    @Test fun sharingIgnoresLegacyExportsCachedBeforeAnArtworkUpdate(): Unit = runBlocking {
        val art = checkNotNull(VerseArtCatalog.artwork("hope-abound"))
        VerseArtFiles.shareIntent(context, art)
        val stale = File(context.cacheDir, "shared/verse-art/hope-cards-${art.id}.webp")
        stale.writeText("Artwork cached before the update")
        val intent = VerseArtFiles.shareIntent(context, art)
        val uri = checkNotNull(intent.clipData).getItemAt(0).uri
        val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
        assertArrayEquals(VerseArtImages.jpeg(context, art, Translation.WEB).readBytes(), actual)
        assertNotNull(BitmapFactory.decodeByteArray(actual, 0, actual.size)?.also { it.recycle() })
        stale.delete()
    }

    @Test fun savedImageIsPublishedToPicturesWithoutStoragePermission(): Unit = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= 29)
        val art = VerseArtCatalog.artworks.last()
        val uri = VerseArtFiles.saveToPhotos(context, art)
        try {
            val actual = checkNotNull(context.contentResolver.openInputStream(uri)).use { it.readBytes() }
            assertArrayEquals(VerseArtImages.jpeg(context, art, Translation.WEB).readBytes(), actual)
            context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.IS_PENDING), null, null, null)!!.use {
                assertTrue(it.moveToFirst())
                assertEquals(0, it.getInt(0))
            }
        } finally { context.contentResolver.delete(uri, null, null) }
    }

    private fun String.bibleWords() = Regex("[\\p{L}\\p{N}]+")
        .findAll(lowercase()).map { it.value }.toList()
}
