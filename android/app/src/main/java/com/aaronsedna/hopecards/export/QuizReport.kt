package com.aaronsedna.hopecards.export

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.print.PageRange
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.*
import java.io.OutputStream
import java.util.Locale

/** Immutable, localized snapshot. External activities must never change the exported round. */
class QuizReport(context: Context, translation: Translation, session: QuizSession, questions: List<QuizQuestion>) {
    val resources = context.createConfigurationContext(Configuration(context.resources.configuration).apply {
        setLocale(Locale.forLanguageTag(QuizLanguage.forTranslation(translation).code))
    }).resources
    val title = resources.getString(R.string.quiz_report_title)
    private val language = QuizLanguage.forTranslation(translation)
    private val edition = resources.getString(R.string.quiz_selected_edition, language.nativeName, translation.label)
    private val byId = questions.associateBy { it.id }
    val score = resources.getString(R.string.quiz_score, session.score(byId), session.questionIds.size)
    val shareText = "$title\n$score\n$edition\n\nHope Cards\nhttps://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"
    private val regular = ResourcesCompat.getFont(context, if (language == QuizLanguage.MALAYALAM) R.font.noto_sans_malayalam_regular else R.font.poppins_regular)!!
    private val bold = ResourcesCompat.getFont(context, if (language == QuizLanguage.MALAYALAM) R.font.noto_sans_malayalam_semibold else R.font.poppins_semibold)!!
    private data class Block(val text: String, val size: Float = 11f, val bold: Boolean = false, val color: Int = Color.rgb(26,39,71), val gap: Int = 5)
    private val groups: List<List<Block>>
    init {
        require(session.finished && session.answers.size == session.questionIds.size)
        groups = listOf(listOf(Block(title, 24f, true), Block(score, 18f, true), Block(edition, gap = 20))) +
            session.questionIds.mapIndexed { index, id ->
                val q = byId.getValue(id)
                val correct = session.answers[index] == q.correctIndex
                buildList {
                    add(Block("${index + 1}. ${q.question}", bold = true, gap = 8))
                    add(Block(resources.getString(if (correct) R.string.quiz_correct else R.string.quiz_incorrect), bold = true,
                        color = if (correct) Color.rgb(32,102,65) else Color.rgb(166,43,43)))
                    add(Block(resources.getString(R.string.quiz_your_answer, q.options[session.answers[index]])))
                    if (!correct) add(Block(resources.getString(R.string.quiz_correct_answer, q.options[q.correctIndex])))
                    if (q.explanation.isNotBlank()) add(Block(q.explanation))
                    add(Block("${resources.getString(R.string.quiz_reference)}: ${BibleReferenceFormatter.formatFull(q.reference, translation)}", color = Color.DKGRAY, gap = 20))
                }
            }
    }

    data class Paper(val width: Int = 595, val height: Int = 842, val left: Int = 40, val top: Int = 40, val right: Int = 40, val bottom: Int = 40)
    data class Fragment(val layout: StaticLayout, val first: Int, val end: Int, val y: Int)
    data class Pages(val paper: Paper, val content: List<List<Fragment>>)

    fun layout(paper: Paper = Paper(), signal: CancellationSignal = CancellationSignal()): Pages {
        val width = paper.width - paper.left - paper.right
        val limit = paper.height - paper.bottom - 30
        require(width >= 72 && limit - paper.top >= 72)
        val pages = mutableListOf(mutableListOf<Fragment>())
        var y = paper.top
        fun nextPage() { pages.add(mutableListOf()); y = paper.top }
        fun layout(block: Block): StaticLayout = StaticLayout.Builder.obtain(block.text, 0, block.text.length,
            TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textSize = block.size; typeface = if (block.bold) bold else regular; color = block.color
            }, width).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(3f, 1f).setIncludePad(true).build()
        for (group in groups) {
            signal.throwIfCanceled()
            val layouts = group.map { it to layout(it) }
            val height = layouts.sumOf { (block, text) -> text.height + block.gap }
            // Keep each answer with its question when it fits a page. Very long passages flow by line.
            if (height <= limit - paper.top && y + height > limit && pages.last().isNotEmpty()) nextPage()
            for ((block, text) in layouts) {
                var first = 0
                while (first < text.lineCount) {
                    signal.throwIfCanceled()
                    var end = first
                    while (end < text.lineCount && y + text.getLineBottom(end) - text.getLineTop(first) <= limit) end++
                    if (end == first) { nextPage(); continue }
                    pages.last().add(Fragment(text, first, end, y))
                    y += text.getLineBottom(end - 1) - text.getLineTop(first)
                    first = end
                    if (first < text.lineCount) nextPage()
                }
                y += block.gap
            }
        }
        return Pages(paper, pages)
    }

    fun write(pages: Pages, output: OutputStream, ranges: Array<out PageRange> = arrayOf(PageRange.ALL_PAGES), signal: CancellationSignal = CancellationSignal()): Array<PageRange> {
        val selected = pages.content.indices.filter { i -> ranges.any { i in it.start..it.end } }
        require(selected.isNotEmpty())
        val pdf = PdfDocument()
        try {
            for (index in selected) {
                signal.throwIfCanceled()
                val paper = pages.paper
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(paper.width, paper.height, index + 1).create())
                val canvas = page.canvas
                for (fragment in pages.content[index]) {
                    canvas.save()
                    canvas.translate(paper.left.toFloat(), fragment.y.toFloat())
                    val top = fragment.layout.getLineTop(fragment.first)
                    val height = fragment.layout.getLineBottom(fragment.end - 1) - top
                    canvas.clipRect(0, 0, fragment.layout.width, height)
                    canvas.translate(0f, -top.toFloat())
                    fragment.layout.draw(canvas)
                    canvas.restore()
                }
                val footer = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; typeface = regular; color = Color.DKGRAY }
                canvas.drawText("Hope Cards", paper.left.toFloat(), (paper.height - paper.bottom).toFloat(), footer)
                val number = "${index + 1} / ${pages.content.size}"
                canvas.drawText(number, paper.width - paper.right - footer.measureText(number), (paper.height - paper.bottom).toFloat(), footer)
                pdf.finishPage(page)
            }
            signal.throwIfCanceled()
            pdf.writeTo(output)
        } finally { pdf.close() }
        signal.throwIfCanceled()
        return selected.map { PageRange(it, it) }.toTypedArray()
    }
}
