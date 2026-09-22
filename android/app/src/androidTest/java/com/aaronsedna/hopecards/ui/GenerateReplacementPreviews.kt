package com.aaronsedna.hopecards.ui

import android.content.ContextWrapper
import android.graphics.*
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.data.VerseArtRenderer
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.VerseArtEligibility
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Targeted replacement candidates, kept outside production until the user chooses them. */
class GenerateReplacementPreviews {
    @Test fun generate() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("replacementPreview") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext
        val renderer = VerseArtRenderer(object : ContextWrapper(app) {
            override fun getAssets() = instrumentation.context.assets
        })
        val repository = VerseRepository(app)
        val v2 = InstrumentationRegistry.getArguments().getString("replacementPreviewV2") == "true"
        val names = if (v2) listOf("quiet-branch-v2", "woodland-path-v2", "golden-meadow-v2") else listOf("greenery", "meadow")
        val out = File(app.getExternalFilesDir(null), "verse-art-generated-options" + if (v2) "/v2" else "").apply { mkdirs() }
        val sheet = Bitmap.createBitmap(names.size * 800, 1740, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet).apply { drawColor(Color.rgb(246, 243, 236)) }
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(31, 52, 42); textSize = 30f; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        var index = 0
        for (edition in listOf(Translation.WEB, Translation.MAL1910)) {
            val verse = checkNotNull(repository.byId("psalm-56-3", edition))
            check(VerseArtEligibility.includes(verse))
            val ml = edition == Translation.MAL1910
            val emphasis = if (ml) verse.text else "I will put my trust in you."
            for ((number, name) in names.withIndex()) {
                val bitmap = renderer.render(verse, 1080, VerseArtRenderer.Design(
                    "replacement-$name", if (ml) "manjari" else "bebas_neue", if (ml) "manjari" else "caveatbrush",
                    emphasis, emphasis, if (ml) 50f else 43f, if (ml) 100f else 142f))
                File(out, "$name-${edition.id}.jpg").outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 94, it) }
                val x = (index % names.size) * 800; val y = (index / names.size) * 870
                canvas.drawBitmap(bitmap, null, Rect(x+16, y+16, x+784, y+784), Paint(Paint.FILTER_BITMAP_FLAG))
                canvas.drawText("${number+1}. ${name.removeSuffix("-v2").replace("-", " ").replaceFirstChar(Char::uppercase)} · ${if (ml) "Malayalam" else "English"}",x+24f,y+833f,label)
                bitmap.recycle(); index++
            }
        }
        File(out, "replacement-comparison.jpg").outputStream().use { sheet.compress(Bitmap.CompressFormat.JPEG, 94, it) }
        sheet.recycle()
    }
}
