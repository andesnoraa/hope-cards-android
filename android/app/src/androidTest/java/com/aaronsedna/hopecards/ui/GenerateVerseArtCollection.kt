package com.aaronsedna.hopecards.ui

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.VerseArtCatalog
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/** Offline authoring tool. Opt in with -e generateVerseArt true; output is bundled in the app. */
class GenerateVerseArtCollection {
    @Test fun generateCollection() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("generateVerseArt") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val source = instrumentation.context
        val designs = JSONArray(source.assets.open("verse-art-generation.json").bufferedReader().use { it.readText() })
        val output = File(context.getExternalFilesDir(null), "verse-art-generated").apply { mkdirs() }
        val renderer = Renderer(context, source)
        for (index in 0 until designs.length()) {
            val design = designs.getJSONObject(index)
            val art = checkNotNull(VerseArtCatalog.artwork(design.getString("id")))
            assertEquals(art.text, design.getString("text"))
            val bitmap = renderer.render(design)
            write(bitmap, File(output, "${art.id}.webp"), 90)
            val thumb = Bitmap.createScaledBitmap(bitmap, 360, 360, true)
            write(thumb, File(output, "${art.id}-thumb.webp"), 84)
            thumb.recycle()
            bitmap.recycle()
        }
        for (category in VerseArtCatalog.categories) {
            val sheet = Bitmap.createBitmap(1500, 1800, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(sheet)
            category.artworkIds.forEachIndexed { index, id ->
                val art = checkNotNull(VerseArtCatalog.artwork(id))
                val generated = File(output, "$id.webp")
                val image = if (generated.exists()) BitmapFactory.decodeFile(generated.path)
                    else context.assets.open(art.assetPath).use { BitmapFactory.decodeStream(it) }
                val x = index % 5 * 300
                val y = index / 5 * 300
                canvas.drawBitmap(image, null, Rect(x, y, x + 300, y + 300), Paint(Paint.FILTER_BITMAP_FLAG))
                image.recycle()
            }
            File(output, "review-${category.id}.png").outputStream().use { sheet.compress(Bitmap.CompressFormat.PNG, 100, it) }
            sheet.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private fun write(bitmap: Bitmap, file: File, quality: Int) {
        file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.WEBP, quality, it)) }
    }

    private class Renderer(context: Context, private val source: Context) {
        private val sans = checkNotNull(ResourcesCompat.getFont(context, R.font.poppins_regular))
        private val serif = checkNotNull(ResourcesCompat.getFont(context, R.font.source_serif_regular))
        private val script = Typeface.createFromAsset(source.assets, "verse-art-fonts/kaushan_script.ttf")
        private val brush = Typeface.createFromAsset(source.assets, "verse-art-fonts/yellowtail.ttf")
        private val marker = Typeface.createFromAsset(source.assets, "verse-art-fonts/permanent_marker.ttf")
        private val condensed = Typeface.createFromAsset(source.assets, "verse-art-fonts/bebas_neue.ttf")

        fun render(design: JSONObject): Bitmap {
            val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            source.assets.open("verse-art-backgrounds/${design.getString("background")}.webp").use {
                val background = checkNotNull(BitmapFactory.decodeStream(it))
                canvas.drawBitmap(background, null, Rect(0, 0, 1080, 1080), Paint(Paint.FILTER_BITMAP_FLAG))
                background.recycle()
            }
            check(!design.getBoolean("light")) { "Pale artwork backgrounds are no longer used" }
            val replacementBackgrounds = setOf("hope-dawn", "hope-path", "hope-horizon", "peace-mist",
                "joy-watercolor", "joy-blossom", "comfort-shore", "comfort-clouds", "gratitude-wheat", "gratitude-paper")
            canvas.drawColor(when {
                design.getString("background") in setOf("reference-coast", "reference-waterfall") -> 0x660C1C20
                design.getString("background").startsWith("reference-") -> 0x330C1C20
                design.getString("background") in replacementBackgrounds -> 0x330B1720
                else -> 0x550B1720
            })
            val color = Color.rgb(255, 249, 235)
            val style = design.getInt("style")
            val text = design.getString("text")
            val emphasis = design.getString("emphasis")
            val start = text.indexOf(emphasis)
            check(start >= 0)
            var end = start + emphasis.length
            while (end < text.length && text[end] in ".,!?;:’”") end++
            val before = text.substring(0, start).trim()
            val quoteOnly = before.isNotEmpty() && before.all { it in "‘“\"" }
            val parts = if (style == 1) listOf("", text, "") else listOf(if (quoteOnly) "" else before,
                (if (quoteOnly) before else "") + text.substring(start, end), text.substring(end).trim())
            val alignment = Layout.Alignment.ALIGN_CENTER
            val base = if (text.length < 110) 51f else 45f
            var scale = 1f
            var layouts: List<StaticLayout>
            while (true) {
                layouts = parts.mapIndexedNotNull { index, part ->
                    if (part.isEmpty()) return@mapIndexedNotNull null
                    val highlight = index == 1
                    val uppercase = style == 2 || !highlight
                    val face = if (highlight) when (style) {
                        0, 4 -> brush
                        1 -> Typeface.create(serif, Typeface.ITALIC)
                        2 -> marker
                        else -> script
                    } else if (style == 3 || style == 5) sans else condensed
                    val size = if (highlight) when (style) {
                        0, 4 -> 124f
                        1 -> 82f
                        2 -> 98f
                        else -> 108f
                    } else if (style == 3 || style == 5) base * .8f else base
                    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        this.color = color
                        typeface = face
                        textSize = size * scale
                        isFakeBoldText = highlight && style == 4
                        letterSpacing = if (uppercase && !highlight) .16f else 0f
                    }
                    val content = if (uppercase) part.uppercase(Locale.US) else part
                    StaticLayout.Builder.obtain(content, 0, content.length, paint, 844)
                        .setAlignment(alignment).setIncludePad(true)
                        .setLineSpacing(4f * scale, 1.05f).build()
                }
                val height = layouts.sumOf { it.height } + (layouts.size - 1) * 24
                val widthFits = layouts.all { layout ->
                    (0 until layout.lineCount).all { layout.getLineWidth(it) <= 844f }
                }
                if (height <= 690 && widthFits) break
                scale -= .035f
                check(scale >= .56f) { "Text does not fit: ${design.getString("id")}" }
            }
            val height = layouts.sumOf { it.height } + (layouts.size - 1) * 24
            var top = 485f - height / 2f
            check(top >= 125)
            canvas.save()
            for (layout in layouts) {
                canvas.save()
                canvas.translate(118f, top)
                layout.draw(canvas)
                canvas.restore()
                top += layout.height + 24
            }
            canvas.restore()
            check(top < 865)
            val referencePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                typeface = sans
                textSize = 35f
                letterSpacing = .2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(design.getString("reference").uppercase(Locale.US) + " · WEB", 540f, 966f, referencePaint)
            return bitmap
        }
    }
}
