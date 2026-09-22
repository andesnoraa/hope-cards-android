package com.aaronsedna.hopecards.data

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtEligibility
import com.aaronsedna.hopecards.model.VerseArtCatalog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VerseArtRenderingInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun everyArtworkFitsEveryEditionWithoutDroppingSourceText() {
        val renderer = VerseArtRenderer(context)
        val repository = VerseRepository(context)
        var rendered = 0
        val regenerate = InstrumentationRegistry.getArguments().getString("regenerateGallery") == "true"
        val output = File(context.getExternalFilesDir(null), "verse-art-regenerated").apply { if (regenerate) mkdirs() }
        val manifest = org.json.JSONArray()
        val excluded = org.json.JSONArray()
        for (edition in Translation.entries) {
            val gallery = runBlocking { VerseArtImages.gallery(context, edition) }
            val ordered = gallery.ordered(VerseArtCatalog.artworks)
            assertEquals(gallery.references.keys, ordered.map { it.id }.toSet())
            assertEquals(ordered, gallery.ordered(VerseArtCatalog.artworks.reversed()))
            if (edition == Translation.MAL1910) assertTrue(gallery.scenes.values.map { it.background }.distinct().size > 10)
            for (art in ordered + VerseArtCatalog.artworks.filter { it.id !in gallery.references }) {
                val verse = checkNotNull(repository.byId(art.verseId, edition))
                if (!VerseArtEligibility.includes(verse)) {
                    excluded.put(org.json.JSONObject().put("edition", edition.id).put("id", art.id)
                        .put("reference", verse.displayReference).put("text", verse.text)
                        .put("characters", verse.text.length).put("words", verse.text.split(Regex("\\s+")).size))
                    continue
                }
                val design = renderer.design(verse)
                assertTrue(verse.text.contains(design.emphasis))
                assertTrue(design.highlightFont in setOf("caveatbrush", "bebas_neue", "manjari", "baloo_chettan", "noto_malayalam"))
                val bitmap = renderer.render(verse, if (regenerate) 1080 else 360)
                assertEquals(if (regenerate) 1080 else 360, bitmap.width)
                if (regenerate) {
                    val folder = File(output, edition.id).apply { mkdirs() }
                    File(folder, "${art.id}.jpg").outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 93, it) }
                    manifest.put(org.json.JSONObject().put("edition", edition.id).put("id", art.id)
                        .put("reference", verse.displayReference).put("text", verse.text)
                        .put("background", design.background).put("family", renderer.family(design.background)).put("font", design.highlightFont))
                }
                bitmap.recycle()
                rendered++
            }
        }
        assertEquals(1810, rendered + excluded.length())
        assertTrue(rendered > 1000)
        if (regenerate) {
            File(output, "manifest.json").writeText(manifest.toString(2))
            File(output, "excluded-long-verses.json").writeText(excluded.toString(2))
        }
    }

    @Test fun cachedBitmapsAreBoundedAndEditionSpecific(): Unit = runBlocking {
        val art = VerseArtCatalog.artworks.first()
        VerseArtImages.clearMemory()
        val english = VerseArtImages.image(context, art, Translation.WEB, 360)
        assertSame(english, VerseArtImages.image(context, art, Translation.WEB, 360))
        val malayalam = VerseArtImages.image(context, art, Translation.MAL1910, 360)
        assertNotSame(english, malayalam)
        for (entry in VerseArtCatalog.artworks.filter { VerseArtEligibility.includes(checkNotNull(VerseRepository(context).byId(it.verseId, Translation.WEB))) }.take(40)) {
            VerseArtImages.image(context, entry, Translation.WEB, 360)
            assertTrue(VerseArtImages.memoryBytes() <= 8 * 1024 * 1024)
        }
        // Eviction must not recycle a bitmap still being drawn by Compose.
        assertFalse(english.isRecycled)
        assertFalse(malayalam.isRecycled)
        VerseArtImages.clearMemory()
        assertEquals(0, VerseArtImages.memoryBytes())
    }

    @Test fun captureInstalledRendererSamples(): Unit = runBlocking {
        val output = File(context.getExternalFilesDir(null), "verse-art-installed").apply { mkdirs() }
        val samples = listOf("peace", "strength", "joy", "hope-faithful")
        for (edition in listOf(Translation.WEB, Translation.MAL1910)) {
            for (id in samples) {
                val art = checkNotNull(VerseArtCatalog.artwork(id))
                val image = VerseArtImages.image(context, art, edition, 1080)
                File(output, "$id-${edition.id}.jpg").outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, 93, it) }
            }
        }
    }
}
