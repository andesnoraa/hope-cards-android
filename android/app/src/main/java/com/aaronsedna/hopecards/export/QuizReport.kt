package com.aaronsedna.hopecards.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.QuizReviewContent
import kotlin.math.ceil
import java.io.OutputStream

/** Immutable, localized snapshot. Sharing never changes the completed quiz. */
class QuizReport(context: Context, translation: Translation, session: QuizSession, questions: List<QuizQuestion>) {
    val review = QuizReviewContent(context, translation, session, questions)
    val resources = review.resources
    val title = review.title
    private val language = QuizLanguage.forTranslation(translation)
    private val edition = review.edition
    val score = review.score
    private val invitation = resources.getString(R.string.quiz_share_invite)
    private val install = resources.getString(R.string.quiz_report_install)
    private val attribution = resources.getString(R.string.quiz_pdf_share_attribution)
    val shareText = "$title\n$score\n$edition\n\n$attribution\n$invitation\n$install\n$PLAY_STORE_URL"
    private val regular = ResourcesCompat.getFont(context,
        if (language == QuizLanguage.MALAYALAM) R.font.noto_sans_malayalam_regular else R.font.poppins_regular)!!
    private val semibold = ResourcesCompat.getFont(context,
        if (language == QuizLanguage.MALAYALAM) R.font.noto_sans_malayalam_semibold else R.font.poppins_semibold)!!
    private val brandFont = ResourcesCompat.getFont(context, R.font.poppins_semibold)!!

    private data class Block(
        val text: String,
        val size: Float = 10.5f,
        val bold: Boolean = false,
        val color: Int = INK,
        val gap: Int = 8,
        val fill: Int? = null,
        val inset: Int = 0,
        val brand: Boolean = false,
        val compact: Boolean = false,
    )

    private val groups: List<List<Block>>
    init {
        groups = review.answers.map { answer ->
            val unanswered = answer.outcome == QuizReviewContent.Outcome.UNANSWERED
            val correct = answer.outcome == QuizReviewContent.Outcome.CORRECT
            val answerColor = when { unanswered -> MUTED; correct -> GREEN; else -> RED }
            buildList {
                add(Block(answer.prompt, size = 12f, bold = true, gap = 9))
                answer.passage?.let { add(Block(it, size = 11f, gap = 9)) }
                add(Block(answer.status, size = 9.5f, bold = true, color = answerColor, gap = 9,
                    fill = when { unanswered -> IVORY; correct -> GREEN_WASH; else -> RED_WASH }, inset = 8, compact = true))
                add(Block(answer.selectedAnswer,
                    color = answerColor,
                    fill = when { unanswered -> IVORY; correct -> GREEN_WASH; else -> RED_WASH }, inset = 10, gap = 5))
                add(Block(answer.correctAnswer,
                    bold = true, color = GREEN, fill = GREEN_WASH, inset = 10, gap = 10))
                if (answer.explanation.isNotBlank()) {
                    add(Block(review.explanationLabel, size = 9.5f, bold = true, gap = 3))
                    add(Block(answer.explanation, gap = 8))
                }
                add(Block(answer.reference, size = 9.5f, bold = true, color = INK, gap = 0, fill = IVORY, inset = 10))
            }
        }
    }

    data class Paper(val width: Int = 595, val height: Int = 842, val left: Int = 54, val top: Int = 54, val right: Int = 54, val bottom: Int = 54)
    data class Fragment(val layout: StaticLayout, val first: Int, val end: Int, val y: Int, val x: Int) {
        val height: Int get() = layout.getLineBottom(end - 1) - layout.getLineTop(first)
    }
    data class Panel(val left: Int, val top: Int, val width: Int, val height: Int, val color: Int, val border: Boolean = false, val radius: Float = 10f)
    data class Pages(val paper: Paper, val content: List<List<Fragment>>, val panels: List<List<Panel>>)

    private fun textLayout(block: Block, width: Int): StaticLayout {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = block.size
            typeface = if (block.brand) brandFont else if (block.bold) semibold else regular
            color = block.color
        }
        val layoutWidth = if (block.compact) minOf(width, ceil(Layout.getDesiredWidth(block.text, paint).toDouble()).toInt() + 1) else width
        return StaticLayout.Builder.obtain(block.text, 0, block.text.length, paint, layoutWidth).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(if (language == QuizLanguage.MALAYALAM) 4f else 2.5f, 1f)
            .setIncludePad(true).build()
    }

    fun layout(paper: Paper = Paper(), signal: CancellationSignal = CancellationSignal()): Pages {
        signal.throwIfCanceled()
        val width = paper.width - paper.left - paper.right
        val limit = paper.height - paper.bottom - FOOTER_SPACE
        require(width >= 140 && limit - paper.top >= 220) { "Page is too small for a readable quiz report." }
        val content = mutableListOf<MutableList<Fragment>>()
        val panels = mutableListOf<MutableList<Panel>>()
        val padding = 18
        val textWidth = width - padding * 2
        var y = paper.top
        var bodyTop = y

        fun place(block: Block, x: Int, availableWidth: Int) {
            val text = textLayout(block, availableWidth)
            content.last().add(Fragment(text, 0, text.lineCount, y, x))
            y += text.height + block.gap
        }
        fun newPage() {
            signal.throwIfCanceled()
            content.add(mutableListOf())
            panels.add(mutableListOf())
            y = paper.top
            place(Block("HOPE CARDS", 9f, bold = true, color = MUTED, gap = 10, brand = true), paper.left, width)
            if (content.size == 1) {
                val top = y
                y += padding
                place(Block(title, if (width < 300) 20f else 25f, bold = true, color = Color.WHITE, gap = 5),
                    paper.left + padding, textWidth)
                place(Block(score, 17f, bold = true, color = PALE_GOLD, gap = 5), paper.left + padding, textWidth)
                place(Block(edition, 9.5f, color = Color.WHITE, gap = 0), paper.left + padding, textWidth)
                y += padding
                panels.last().add(Panel(paper.left, top, width, y - top, INK, radius = 14f))
                panels.last().add(Panel(paper.left + padding, top + 7, 28, 2, GOLD, radius = 1f))
                y += 15
                place(Block(review.reviewTitle, 13f, bold = true, gap = 12), paper.left, width)
            } else {
                place(Block(title, 14f, bold = true, gap = 2), paper.left, width)
                place(Block(edition, 9f, color = MUTED, gap = 10), paper.left, width)
                panels.last().add(Panel(paper.left, y, width, 1, LINE, radius = 0f))
                y += 14
            }
            bodyTop = y
        }

        fun addCard(blocks: List<Block>, background: Int = Color.WHITE) {
            val layouts = blocks.map { it to textLayout(it, textWidth - it.inset * 2) }
            val measured = padding * 2 + layouts.sumOf { (block, text) ->
                text.height + block.gap + if (block.fill != null) 12 else 0
            }
            if (y + measured > limit && y > bodyTop) newPage()
            // Start a large question on a clean continuation page, rather than below the cover.
            if (y + measured > limit && content.size == 1) newPage()
            var cardTop = y
            var panelIndex = panels.last().size
            panels.last().add(Panel(paper.left, cardTop, width, 0, background, border = true))
            y += padding
            var cardHasText = false

            fun finishCard() {
                panels.last()[panelIndex] = panels.last()[panelIndex].copy(height = y - cardTop + padding)
            }
            fun continueCard() {
                check(cardHasText) { "Page is too small for a line of quiz text." }
                finishCard()
                newPage()
                cardTop = y
                panelIndex = panels.last().size
                panels.last().add(Panel(paper.left, cardTop, width, 0, background, border = true))
                y += padding
                cardHasText = false
            }

            for ((block, text) in layouts) {
                signal.throwIfCanceled()
                var first = 0
                val insetY = if (block.fill != null) 6 else 0
                // Keep each answer panel whole whenever it fits on a clean continuation page.
                val blockHeight = text.height + insetY * 2
                val continuationRoom = limit - bodyTop - padding * 2
                if (cardHasText && blockHeight <= continuationRoom && y + blockHeight > limit - padding) continueCard()
                while (first < text.lineCount) {
                    val available = limit - padding - y - insetY * 2
                    var end = first
                    while (end < text.lineCount && text.getLineBottom(end) - text.getLineTop(first) <= available) end++
                    if (end == first) { continueCard(); continue }
                    val height = text.getLineBottom(end - 1) - text.getLineTop(first)
                    block.fill?.let { fill ->
                        panels.last().add(Panel(paper.left + padding, y,
                            if (block.compact) text.width + block.inset * 2 else textWidth,
                            height + insetY * 2, fill, radius = 6f))
                    }
                    content.last().add(Fragment(text, first, end, y + insetY, paper.left + padding + block.inset))
                    y += height + insetY * 2
                    cardHasText = true
                    first = end
                    if (first < text.lineCount) continueCard()
                }
                // A gap can be reduced at a page edge, but text and card padding never overlap the footer.
                y += minOf(block.gap, maxOf(0, limit - padding - y))
            }
            finishCard()
            y += padding + 14
        }

        newPage()
        groups.forEach { addCard(it) }
        return Pages(paper, content, panels)
    }

    fun write(pages: Pages, output: OutputStream, signal: CancellationSignal = CancellationSignal()) {
        signal.throwIfCanceled()
        val pdf = PdfDocument()
        try {
            pages.content.forEachIndexed { index, fragments ->
                signal.throwIfCanceled()
                val paper = pages.paper
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(paper.width, paper.height, index + 1).create())
                val canvas = page.canvas
                canvas.drawColor(IVORY)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                for (panel in pages.panels[index]) {
                    paint.color = panel.color
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(panel.left.toFloat(), panel.top.toFloat(), (panel.left + panel.width).toFloat(),
                        (panel.top + panel.height).toFloat(), panel.radius, panel.radius, paint)
                    if (panel.border) {
                        paint.color = LINE
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 0.6f
                        canvas.drawRoundRect(panel.left.toFloat(), panel.top.toFloat(), (panel.left + panel.width).toFloat(),
                            (panel.top + panel.height).toFloat(), panel.radius, panel.radius, paint)
                    }
                }
                for (fragment in fragments) {
                    canvas.save()
                    canvas.translate(fragment.x.toFloat(), fragment.y.toFloat())
                    val top = fragment.layout.getLineTop(fragment.first)
                    canvas.clipRect(0, 0, fragment.layout.width, fragment.height)
                    canvas.translate(0f, -top.toFloat())
                    fragment.layout.draw(canvas)
                    canvas.restore()
                }
                val baseline = (paper.height - paper.bottom).toFloat()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.6f
                paint.color = LINE
                canvas.drawLine(paper.left.toFloat(), baseline - 17, (paper.width - paper.right).toFloat(), baseline - 17, paint)
                val footer = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8f; typeface = brandFont; color = MUTED }
                canvas.drawText("Hope Cards", paper.left.toFloat(), baseline, footer)
                val number = "${index + 1} / ${pages.content.size}"
                canvas.drawText(number, paper.width - paper.right - footer.measureText(number), baseline, footer)
                pdf.finishPage(page)
            }
            signal.throwIfCanceled()
            pdf.writeTo(output)
        } finally { pdf.close() }
        signal.throwIfCanceled()
    }

    private companion object {
        const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"
        const val FOOTER_SPACE = 30
        val INK = Color.rgb(26, 39, 71)
        val IVORY = Color.rgb(248, 246, 242)
        val GOLD = Color.rgb(200, 155, 60)
        val PALE_GOLD = Color.rgb(245, 234, 200)
        val MUTED = Color.rgb(91, 101, 120)
        val LINE = Color.rgb(231, 226, 216)
        val GREEN = Color.rgb(32, 102, 65)
        val RED = Color.rgb(151, 53, 45)
        val GREEN_WASH = Color.rgb(237, 246, 239)
        val RED_WASH = Color.rgb(252, 240, 236)
    }
}
