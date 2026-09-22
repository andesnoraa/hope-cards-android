package com.aaronsedna.hopecards.ui

import android.graphics.*
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.data.VerseArtRenderer
import com.aaronsedna.hopecards.model.Translation
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Opt-in authoring only; preview fonts and backgrounds never enter the production APK. */
class GenerateVerseArtDesignPreviews {
    @Suppress("DEPRECATION")
    @Test fun generatePreviews() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("generateDesignPreviews") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val app = instrumentation.targetContext
        val greenery = InstrumentationRegistry.getArguments().getString("greeneryPreview") == "true"
        val assets = instrumentation.context.assets
        val output = File(app.getExternalFilesDir(null), if (greenery) "verse-art-greenery-preview" else "verse-art-design-preview").apply { mkdirs() }
        val repository = VerseRepository(app)
        val renderer = VerseArtRenderer(if (greenery) object : android.content.ContextWrapper(app) {
            override fun getAssets() = instrumentation.context.assets
        } else app)
        val designs = JSONArray(assets.open(if (greenery) "verse-art-preview/greenery-designs.json" else "verse-art-preview/designs.json").bufferedReader().use { it.readText() })
        val fonts = mutableMapOf<String, Typeface>()
        fun font(name: String): Typeface = fonts.getOrPut(name) {
            if (name == "noto_malayalam") checkNotNull(ResourcesCompat.getFont(app, R.font.noto_sans_malayalam_regular))
            else Typeface.createFromAsset(assets, "verse-art-preview/fonts/$name.ttf")
        }
        fun paint(name: String, size: Float, ink: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = font(name); textSize = size; color = ink
        }
        fun write(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat, quality: Int) {
            file.outputStream().use { check(bitmap.compress(format, quality, it)) }
        }
        val metrics = JSONArray()
        for (i in 0 until designs.length()) {
            val design = designs.getJSONObject(i)
            val edition = Translation.fromId(design.getString("edition"))
            val verse = checkNotNull(repository.byId(design.getString("verseId"), edition))
            check(verse.text == design.getString("verifiedText")) { "Source text changed; reverify preview" }
            val started = System.nanoTime()
            val fontName = design.getString("font")
            val highlightFont = design.getString("highlightFont")
            val typography = VerseArtRenderer.Design(
                design.getString("background"), fontName, highlightFont, design.getString("emphasis"),
                design.optString("highlightText", design.getString("emphasis")),
                design.optDouble("bodySize", 47.0).toFloat(), design.getDouble("highlightSize").toFloat())
            val bitmap = renderer.render(verse, 1080, typography)
            val reference = verse.displayReference + " · " + edition.label
            val renderMs = (System.nanoTime() - started) / 1_000_000.0
            val name = design.getString("id")
            write(bitmap, File(output, "$name.jpg"), Bitmap.CompressFormat.JPEG, 93)
            write(bitmap, File(output, "$name.webp"), Bitmap.CompressFormat.WEBP, 88)
            val thumb = Bitmap.createScaledBitmap(bitmap, 360, 360, true)
            write(thumb, File(output, "$name-thumb.webp"), Bitmap.CompressFormat.WEBP, 84)
            thumb.recycle(); bitmap.recycle()
            metrics.put(JSONObject().put("id", name).put("renderMs", renderMs)
                .put("text", verse.text).put("reference", reference).put("font", fontName).put("highlightFont", highlightFont)
                .put("jpegBytes", File(output, "$name.jpg").length())
                .put("webpBytes", File(output, "$name.webp").length())
                .put("thumbBytes", File(output, "$name-thumb.webp").length()))
        }
        for (group in if (greenery) listOf("greenery") else listOf("english", "landscapes", "malayalam", "languages", "editions", "directions", "photo-lakes", "photo-woodland", "photo-new", "approved-bright", "approved-greenery")) {
            val selected = (0 until designs.length()).map { designs.getJSONObject(it) }
                .filter { if (group == "directions") it.getString("id") in setOf(
                    "en-forest-handlettered", "en-wheat-serif", "en-flowers-modern",
                    "en-moonlight", "en-mountains", "en-dew-leaf"
                ) else it.getString("group") == group }
            val columns = 3
            val rows = (selected.size + columns - 1) / columns
            val sheet = Bitmap.createBitmap(1800, rows * 670, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(sheet)
            canvas.drawColor(Color.parseColor("#F6F2EC"))
            selected.forEachIndexed { index, d ->
                val image = BitmapFactory.decodeFile(File(output, d.getString("id") + ".jpg").path)
                val x = index % columns * 600
                val y = index / columns * 670
                canvas.drawBitmap(image, null, Rect(x + 16, y + 16, x + 584, y + 584), Paint(Paint.FILTER_BITMAP_FLAG))
                image.recycle()
                val label = paint("dm_sans", 23f, Color.parseColor("#26382F")).apply { textAlign = Paint.Align.CENTER }
                canvas.drawText(d.getString("label"), x + 300f, y + 624f, label)
            }
            write(sheet, File(output, "review-$group.jpg"), Bitmap.CompressFormat.JPEG, 94)
            sheet.recycle()
        }
        File(output, "render-report.json").writeText(metrics.toString(2))
        val backgroundOutput = File(output, "reusable-backgrounds").apply { mkdirs() }
        checkNotNull(assets.list("verse-art-preview/backgrounds")).forEach { name ->
            val original = assets.open("verse-art-preview/backgrounds/$name").use { checkNotNull(BitmapFactory.decodeStream(it)) }
            val resized = Bitmap.createScaledBitmap(original, 1080, 1080, true)
            write(resized, File(backgroundOutput, name.removeSuffix(".png") + ".webp"), Bitmap.CompressFormat.WEBP, 88)
            if (resized !== original) resized.recycle()
            original.recycle()
        }
    }
}
