package com.aaronsedna.hopecards.data

import android.content.Context
import android.graphics.*
import android.graphics.text.LineBreaker
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.VerseArtContrast
import com.aaronsedna.hopecards.model.VerseArtRotation
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/** Native text over reusable photographs. Call on a worker, serialized by VerseArtImages. */
internal class VerseArtRenderer(private val context: Context) {
    private val root = "verse-art-renderer"
    private val faces = mutableMapOf<String, Typeface>()
    private val reviewed = JSONArray(context.assets.open("$root/reviewed-designs.json").bufferedReader().use { it.readText() })
    private val webEmphasis = JSONObject(context.assets.open("$root/web-emphasis.json").bufferedReader().use { it.readText() })
    private val photos = JSONObject(context.assets.open("$root/photo-profiles.json").bufferedReader().use { it.readText() })
    private val photoNames = photos.keys().asSequence().toList().sorted()
    private val decorative = listOf("caveatbrush", "bebas_neue")
    private val malayalam = listOf("manjari", "baloo_chettan", "noto_malayalam")

    internal data class Design(val background: String, val bodyFont: String, val highlightFont: String,
        val emphasis: String, val highlightText: String, val bodySize: Float, val highlightSize: Float)

    internal fun design(verse: Verse, rotation: Long? = null): Design {
        val base = baseDesign(verse)
        if (rotation == null) return base
        // Keep reviewed typography and exact emphasis; rotate only through roomy photo profiles.
        val candidates = photoNames.filter { name ->
            val profile = photos.getJSONObject(name)
            profile.getInt("height") >= 580 && profile.getInt("width") >= 780
        }
        return base.copy(background = VerseArtRotation.background(candidates + base.background, verse.id, rotation))
    }

    private fun baseDesign(verse: Verse): Design {
        val selected = (0 until reviewed.length()).map { reviewed.getJSONObject(it) }.firstOrNull {
            it.getString("edition") == verse.edition.id && it.getString("verseId") == verse.id &&
                it.getString("verifiedText") == verse.text
        }
        if (selected != null) return Design(selected.getString("background"), selected.getString("font"),
            selected.getString("highlightFont"), selected.getString("emphasis"),
            selected.optString("highlightText", selected.getString("emphasis")),
            selected.optDouble("bodySize", 47.0).toFloat(), selected.getDouble("highlightSize").toFloat())
        val index = verse.id.hashCode().and(Int.MAX_VALUE)
        val isMalayalam = verse.edition == Translation.MAL1910
        val candidate = webEmphasis.optString(verse.id)
        var position = if (candidate.isNotBlank()) verse.text.indexOf(candidate, ignoreCase = true) else -1
        if (position > 0 && verse.text[position-1].isLetterOrDigit()) position = -1
        if (position >= 0 && position + candidate.length < verse.text.length && verse.text[position+candidate.length].isLetterOrDigit()) position = -1
        val emphasis = if (!isMalayalam && position >= 0) {
            var end = position + candidate.length
            while (position > 0 && !verse.text[position-1].isWhitespace()) position--
            while (end < verse.text.length && !verse.text[end].isWhitespace()) end++
            verse.text.substring(position, end)
        } else firstPhrase(verse.text, if (isMalayalam) 65 else 55)
        val bodyFont = if (isMalayalam) malayalam[index % malayalam.size] else "bebas_neue"
        val highlightFont = if (isMalayalam) bodyFont else decorative[index % decorative.size]
        val size = if (isMalayalam) 90f else when {
            emphasis.length < 12 -> 175f
            emphasis.length < 24 -> 124f
            emphasis.length < 40 -> 98f
            else -> 80f
        }
        // Fit the complete verse into each photo's deliberately reserved text area.
        // Malayalam also gets the photo library, with more room for its shaped glyphs.
        val candidates = photoNames.filter { name ->
            val profile = photos.getJSONObject(name)
            val roomy = verse.text.length > (if (isMalayalam) 100 else 90)
            profile.getInt("height") >= (if (roomy) 580 else if (isMalayalam) 420 else 280) &&
                profile.getInt("width") >= (if (roomy) 760 else if (isMalayalam) 680 else 430)
        }
        val background = candidates[index % candidates.size]
        return Design(background, bodyFont, highlightFont, emphasis, emphasis,
            if (isMalayalam) 51f else 47f, size)
    }

    internal fun family(background: String): String = photos.getJSONObject(background).getString("family")

    private fun firstPhrase(text: String, limit: Int): String {
        val boundary = text.indexOfFirst { it in ",;:.!?" }
        if (boundary in 8 until limit) {
            var end = boundary + 1
            while (end < text.length && !text[end].isWhitespace()) end++
            return text.substring(0, end)
        }
        val words = text.split(' ')
        var phrase = ""
        for (word in words) {
            val next = if (phrase.isEmpty()) word else "$phrase $word"
            if (next.length > limit && phrase.isNotEmpty()) break
            phrase = next
            if (phrase.length >= limit * .7) break
        }
        return phrase
    }

    private fun face(name: String) = faces.getOrPut(name) {
        if (name == "noto_malayalam") checkNotNull(ResourcesCompat.getFont(context, R.font.noto_sans_malayalam_regular))
        else Typeface.createFromAsset(context.assets, "$root/fonts/$name.ttf")
    }
    private fun paint(font: String, size: Float, ink: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = face(font); textSize = size; color = ink
    }
    private fun layout(text: String, paint: TextPaint, width: Int, leading: Float = 1.12f) = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, width).setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setIncludePad(true).setLineSpacing(8f, leading).setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED)
        .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE).build()

    fun render(verse: Verse, size: Int, design: Design = design(verse)): Bitmap {
        require(size == 360 || size == 1080)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap).apply { scale(size / 1080f, size / 1080f) }
            val options = BitmapFactory.Options().apply { inSampleSize = if (size == 360) 2 else 1 }
            val background = context.assets.open("$root/backgrounds/${design.background}.webp").use {
                checkNotNull(BitmapFactory.decodeStream(it, null, options))
            }
            // Tiny luminance grid, discarded after rendering. No filtered copy or extra bitmap.
            val lightGrid = DoubleArray(64 * 64) { index ->
                VerseArtContrast.luminance(background.getPixel(
                    ((index % 64 + .5) * background.width / 64).toInt().coerceAtMost(background.width - 1),
                    ((index / 64 + .5) * background.height / 64).toInt().coerceAtMost(background.height - 1)))
            }
            fun contrast(left: Float, top: Float, width: Float, height: Float, ink: Int): VerseArtContrast.Choice {
                val samples = mutableListOf<Double>()
                val x1 = (left * 64 / 1080).toInt().coerceIn(0, 63)
                val x2 = ((left + width) * 64 / 1080).toInt().coerceIn(x1, 63)
                val y1 = (top * 64 / 1080).toInt().coerceIn(0, 63)
                val y2 = ((top + height) * 64 / 1080).toInt().coerceIn(y1, 63)
                for (y in y1..y2) for (x in x1..x2) samples.add(lightGrid[y * 64 + x])
                return VerseArtContrast.choose(samples, ink)
            }
            fun drawReadableLayout(text: StaticLayout, left: Float, top: Float, preferredInk: Int) {
                val lineLeft = (0 until text.lineCount).minOf(text::getLineLeft)
                val lineRight = (0 until text.lineCount).maxOf(text::getLineRight)
                val choice = contrast(left + lineLeft, top, lineRight - lineLeft, text.height.toFloat(), preferredInk)
                val p = text.paint
                p.clearShadowLayer()
                canvas.save(); canvas.translate(left, top)
                if (choice.outline) {
                    p.style = Paint.Style.STROKE
                    p.strokeJoin = Paint.Join.ROUND
                    p.strokeWidth = (p.textSize * .05f).coerceIn(2.8f, 4.5f)
                    p.color = choice.edge
                    text.draw(canvas)
                }
                p.style = Paint.Style.FILL
                p.color = choice.ink
                text.draw(canvas)
                canvas.restore()
            }
            try { canvas.drawBitmap(background, null, Rect(0, 0, 1080, 1080), Paint(Paint.FILTER_BITMAP_FLAG)) }
            finally { background.recycle() }
            val photo = photos.optJSONObject(design.background)
            val dark = design.background !in setOf("muted-wheat", "muted-flowers")
            val ink = Color.parseColor(photo?.getString("ink") ?: if (dark) "#FFF8E9" else if (design.background == "muted-wheat") "#403125" else "#473445")
            val ml = verse.edition == Translation.MAL1910
            val secondaryInk = if (photo?.has("supportingInk") == true) Color.parseColor(photo.getString("supportingInk"))
                else if (dark && !ml && photo == null) Color.parseColor("#EBDAB9") else ink
            val maxHeight = photo?.getInt("height") ?: 660
            val centerX = photo?.getDouble("centerX")?.toFloat() ?: 540f
            val centerY = photo?.getDouble("centerY")?.toFloat() ?: 470f
            val shadow = photo?.optBoolean("shadow", false) == true
            val start = verse.text.indexOf(design.emphasis)
            check(start >= 0 && design.emphasis.isNotEmpty())
            val pieces = listOf(verse.text.substring(0, start).trim() to false, design.emphasis to true,
                verse.text.substring(start + design.emphasis.length).trim() to false).filter { it.first.isNotEmpty() }
            check(pieces.joinToString(" ") { it.first } == verse.text) { "Artwork must preserve every source word: ${verse.id}/${verse.edition.id}" }
            check(design.highlightText.replace(Regex("\\s+"), " ") == design.emphasis)
            var factor = 1f
            var layouts: List<StaticLayout>
            while (true) {
                var wholeWordsFit = true
                layouts = pieces.map { (content, highlight) ->
                    val font = if (highlight) design.highlightFont else design.bodyFont
                    val p = paint(font, (if (highlight) design.highlightSize else design.bodySize) * factor,
                        if (highlight) ink else secondaryInk).apply {
                            letterSpacing = if (!highlight && !ml) .17f else 0f
                            isFakeBoldText = !highlight && font == "bebas_neue"
                            if (shadow && (highlight || photo?.has("supportingInk") != true)) setShadowLayer(4f, 0f, 2f, Color.BLACK)
                        }
                    val lettering = if (highlight) design.highlightText else content
                    val display = if (!ml && (!highlight || font in setOf("permanent_marker", "caveatbrush"))) lettering.uppercase(Locale.ROOT) else lettering
                    val width = photo?.getInt("width") ?: if (highlight) 900 else 760
                    if (display.split(Regex("\\s+")).any { p.measureText(it) > width }) wholeWordsFit = false
                    layout(display, p, width, if (highlight) .96f else 1.20f)
                }
                if (wholeWordsFit && layouts.sumOf { it.height } + (layouts.size - 1) * 22 <= maxHeight &&
                    layouts.all { l -> (0 until l.lineCount).all { l.getLineWidth(it) <= l.width } }) break
                factor -= .025f
                check(factor >= .5f) { "Verse cannot fit legibly: ${verse.id}/${verse.edition.id}" }
            }
            var top = centerY - (layouts.sumOf { it.height } + (layouts.size - 1) * 22) / 2f
            check(top >= 50f)
            for (l in layouts) {
                drawReadableLayout(l, centerX-l.width/2f, top, l.paint.color)
                top += l.height + 22
            }
            val ref = "${verse.displayReference} (${verse.edition.label})"
            val footerInk = if (photo != null) Color.parseColor(photo.optString("footerInk", "#FFF8E9")) else ink
            val citationInk = photo?.optString("citationInk")?.takeIf { it.isNotEmpty() }?.let(Color::parseColor) ?: footerInk
            val refPaint = paint(if (ml) "noto_malayalam" else "dm_sans", if (ml) 30f else 26f, citationInk)
                .apply {
                    letterSpacing = if (ml) 0f else .18f
                    if (photo != null && !photo.has("footerInk")) setShadowLayer(3f, 0f, 1f, Color.BLACK)
                }
            val citation = layout(if (ml) ref else ref.uppercase(Locale.ROOT), refPaint, 880)
            check(citation.height <= 110)
            drawReadableLayout(citation, 100f, photo?.optDouble("citationY", 856.0)?.toFloat() ?: 856f, citationInk)
            val brandInk = contrast(890f, 1000f, 140f, 30f, footerInk).ink
            val brand = paint("dm_sans", 22f, brandInk).apply { alpha = 153 }
            canvas.drawText("Hope Cards", 1026f-brand.measureText("Hope Cards"), 1026f, brand)
            return bitmap
        } catch (error: Throwable) { bitmap.recycle(); throw error }
    }
}
