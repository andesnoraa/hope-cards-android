package com.aaronsedna.hopecards.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.model.VerseArtEligibility
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VerseArtRotationInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun newBackgroundsDecodeAndFitEachEdition() {
        val backgrounds = context.assets.list("verse-art-renderer/backgrounds")!!.filter { it.startsWith("scene-") }
        assertEquals(setOf("scene-desert-stars", "scene-magnolia-morning", "scene-olive-sunlight",
            "scene-winter-silence", "scene-autumn-stillness", "scene-rainy-fern", "scene-songbird-blossom",
            "scene-stars-over-lake"), backgrounds.map { it.removeSuffix(".webp") }.toSet())
        val renderer = VerseArtRenderer(context)
        val repository = VerseRepository(context)
        val output = File(context.getExternalFilesDir(null), "verse-art-new-backgrounds").apply { mkdirs() }
        var bytes = 0L
        backgrounds.forEach { name ->
            val data = context.assets.open("verse-art-renderer/backgrounds/$name").use { it.readBytes() }
            bytes += data.size
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
            assertEquals(1080, bounds.outWidth)
            assertEquals(1080, bounds.outHeight)
        }
        assertTrue("New photos should stay compact: $bytes", bytes < 3 * 1024 * 1024)
        for (edition in Translation.entries) {
            val eligible = VerseArtCatalog.artworks.mapNotNull { repository.byId(it.verseId, edition) }
                .filter(VerseArtEligibility::includes)
            val examples = listOf(eligible.maxBy { it.text.length }, checkNotNull(repository.byId("psalm-56-3", edition)))
            for (name in backgrounds) for ((index, verse) in examples.withIndex()) {
                val design = renderer.design(verse).copy(background = name.removeSuffix(".webp"))
                val preview = index == 1 && edition in listOf(Translation.WEB, Translation.MAL1910)
                val bitmap = renderer.render(verse, if (preview) 1080 else 360, design)
                if (preview) File(output, "${name.removeSuffix(".webp")}-${edition.id}.jpg")
                    .outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                bitmap.recycle()
            }
        }
    }

    @Test fun rotationUsesBoundedCachesAndExportsTheDisplayedBackground(): Unit = runBlocking {
        val renderer = VerseArtRenderer(context)
        val art = VerseArtCatalog.artworks.first()
        val verse = VerseArtImages.verse(context, art, Translation.WEB)
        val firstDesign = renderer.design(verse, 20)
        assertNotEquals(firstDesign.background, renderer.design(verse, 21).background)
        val gallery = VerseArtImages.gallery(context, Translation.WEB, 20)
        assertEquals(firstDesign.background, gallery.scenes.getValue(art.id).background)
        VerseArtImages.clearMemory()
        val first = VerseArtImages.image(context, art, Translation.WEB, 360, 20)
        assertSame(first, VerseArtImages.image(context, art, Translation.WEB, 360, 20))
        val repeat = (21L..100L).first { renderer.design(verse, it).background == firstDesign.background }
        assertSame(first, VerseArtImages.image(context, art, Translation.WEB, 360, repeat))
        assertNotSame(first, VerseArtImages.image(context, art, Translation.WEB, 360, 21))
        val file = VerseArtImages.jpeg(context, art, Translation.WEB, 20)
        val otherFile = VerseArtImages.jpeg(context, art, Translation.WEB, 21)
        assertNotEquals(file.name, otherFile.name)
        val shared = VerseArtFiles.shareIntent(context, art, Translation.WEB, 20)
        val uri = checkNotNull(shared.clipData).getItemAt(0).uri
        assertArrayEquals(file.readBytes(), context.contentResolver.openInputStream(uri)!!.use { it.readBytes() })
        assertTrue(VerseArtImages.memoryBytes() <= 8 * 1024 * 1024)
        assertFalse(first.isRecycled)
        VerseArtImages.clearMemory()
    }
}
