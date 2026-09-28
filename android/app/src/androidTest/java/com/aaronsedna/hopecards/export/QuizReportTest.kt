package com.aaronsedna.hopecards.export

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.os.ParcelFileDescriptor
import androidx.core.content.res.ResourcesCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.*
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class QuizReportTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun report(translation: Translation): QuizReport {
        val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation))
            .sortedByDescending { it.question.length }.take(10)
        val session = QuizSession(questionIds = questions.map { it.id },
            answers = questions.mapIndexed { index, q -> if (index % 2 == 0) q.correctIndex else (q.correctIndex + 1) % 4 },
            position = 9, finished = true)
        return QuizReport(context, translation, session, questions)
    }

    private fun assertPaperBounds(pages: QuizReport.Pages) {
        val paper = pages.paper
        val contentBottom = paper.height - paper.bottom - 30
        assertTrue(pages.content.isNotEmpty())
        assertEquals(pages.content.size, pages.panels.size)
        pages.content.forEachIndexed { pageIndex, fragments ->
            assertTrue("Empty page ${pageIndex + 1}", fragments.isNotEmpty())
            fragments.forEach { fragment ->
                assertTrue(fragment.first >= 0)
                assertTrue(fragment.end > fragment.first)
                assertTrue(fragment.end <= fragment.layout.lineCount)
                assertTrue(fragment.height > 0)
                assertTrue(fragment.layout.width > 0)
                assertTrue(fragment.x >= paper.left)
                assertTrue(fragment.x + fragment.layout.width <= paper.width - paper.right)
                assertTrue(fragment.y >= paper.top)
                assertTrue("Text overlaps the footer on page ${pageIndex + 1}", fragment.y + fragment.height <= contentBottom)
            }
            fragments.zipWithNext().forEach { (previous, next) ->
                assertTrue("Overlapping text on page ${pageIndex + 1}", previous.y + previous.height <= next.y)
            }
            pages.panels[pageIndex].forEach { panel ->
                assertTrue(panel.width > 0)
                assertTrue(panel.height > 0)
                assertTrue(panel.left >= paper.left)
                assertTrue(panel.left + panel.width <= paper.width - paper.right)
                assertTrue(panel.top >= paper.top)
                assertTrue("Panel overlaps the footer on page ${pageIndex + 1}", panel.top + panel.height <= contentBottom)
                if (panel.border) {
                    val cardText = fragments.filter { it.y >= panel.top && it.y < panel.top + panel.height }
                    assertTrue("A question card must contain text", cardText.isNotEmpty())
                    cardText.forEach { fragment ->
                        assertTrue("Text touches the card's left edge", fragment.x >= panel.left + 18)
                        assertTrue("Text touches the card's right edge", fragment.x + fragment.layout.width <= panel.left + panel.width - 18)
                        assertTrue("Text touches the card's top edge", fragment.y >= panel.top + 18)
                        assertTrue("Text touches the card's bottom edge", fragment.y + fragment.height <= panel.top + panel.height - 18)
                    }
                }
            }
        }
    }

    @Test fun allLanguagesProduceReadableMultipageReportsAndRespectPaperBounds() {
        listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910,
            Translation.RIV1927, Translation.RV1909, Translation.ADB1905).forEach { translation ->
            val report = report(translation)
            assertTrue(report.shareText.contains(report.score))
            assertTrue(report.shareText.contains(translation.label))
            for (paper in listOf(QuizReport.Paper(), QuizReport.Paper(842, 595), QuizReport.Paper(298, 420))) {
                val pages = report.layout(paper)
                assertTrue(pages.content.size > 1)
                assertPaperBounds(pages)
                if (paper == QuizReport.Paper()) {
                    assertTrue("A4 report needs generous outside margins", listOf(paper.left, paper.top, paper.right, paper.bottom).all { it >= 54 })
                }
                val pdfText = pages.content.flatten().joinToString("\n") { it.layout.text.toString() }
                assertFalse("Play Store link leaked into the ${translation.id} PDF", pdfText.contains("play.google.com"))
                listOf(R.string.quiz_pdf_share_attribution, R.string.quiz_share_invite, R.string.quiz_report_install).forEach { resource ->
                    assertFalse("Sharing promotion leaked into the ${translation.id} PDF",
                        pdfText.contains(report.resources.getString(resource)))
                }
                val file = File(context.getExternalFilesDir(null), "quiz-report-${translation.id}-${paper.width}.pdf")
                file.outputStream().use { report.write(pages, it) }
                if (paper == QuizReport.Paper()) {
                    val sharedFile = File.createTempFile("quiz-share-language-", ".pdf",
                        File(context.cacheDir, "quiz-reports").apply { mkdirs() })
                    try {
                        file.copyTo(sharedFile, overwrite = true)
                        val sharedText = requireNotNull(QuizReportExport.shareIntent(context, report, sharedFile).getStringExtra(Intent.EXTRA_TEXT))
                        assertEquals(report.shareText, sharedText)
                        assertTrue(sharedText.contains(report.resources.getString(R.string.quiz_pdf_share_attribution)))
                        assertTrue(sharedText.contains(report.resources.getString(R.string.quiz_share_invite)))
                        assertTrue(sharedText.contains(report.resources.getString(R.string.quiz_report_install)))
                        assertTrue(sharedText.contains("https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"))
                        assertTrue(sharedText.contains(report.score))
                    } finally { sharedFile.delete() }
                }
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use {
                    assertEquals(pages.content.size, it.pageCount)
                    repeat(it.pageCount) { pageIndex ->
                        it.openPage(pageIndex).use { page ->
                            assertEquals(paper.width, page.width)
                            assertEquals(paper.height, page.height)
                        }
                    }
                }
            }
        }
    }

    @Test fun cancellationStopsLayoutAndWritingBeforeBytesAreEmitted() {
        val report = report(Translation.MAL1910)
        val pages = report.layout()
        val signal = CancellationSignal().apply { cancel() }
        try { report.layout(signal = signal); fail("Cancelled layout succeeded") } catch (_: OperationCanceledException) { }
        val output = ByteArrayOutputStream()
        try { report.write(pages, output, signal = signal); fail("Cancelled write succeeded") } catch (_: OperationCanceledException) { }
        assertEquals(0, output.size())
    }

    @Test fun oversizedQuestionAndAnswersContinueWithoutLosingOrRepeatingLines() {
        fun longText(prefix: String) = (1..180).joinToString(" ") { "$prefix $it: Scripture gives us hope and encouragement." }
        val prompt = "Which Bible reference matches this Scripture passage?"
        val quotation = longText("Quotation")
        val question = QuizQuestion("long-report", "John 3:16", "$prompt\n\n$quotation",
            listOf(longText("Selected answer"), longText("Correct answer"), "Third answer", "Fourth answer"),
            correctIndex = 1, explanation = longText("Explanation"))
        val session = QuizSession(questionIds = listOf(question.id), answers = listOf(0), finished = true)
        val report = QuizReport(context, Translation.BSB, session, listOf(question))
        val pages = report.layout()
        assertPaperBounds(pages)
        assertTrue("The oversized content must exercise page continuation", pages.content.size > 3)
        val allFragments = pages.content.flatten()
        val layouts = allFragments.groupBy { it.layout }
        assertTrue("Expected a text block to span multiple pages", layouts.values.any { it.size > 1 })
        layouts.forEach { (layout, fragments) ->
            val emittedLines = fragments.flatMap { (it.first until it.end).toList() }
            assertEquals("Missing, duplicated or reordered lines in ${layout.text.take(50)}",
                (0 until layout.lineCount).toList(), emittedLines)
        }
        val expectedText = listOf("1. $prompt", quotation,
            report.resources.getString(R.string.quiz_your_answer, question.options[0]),
            report.resources.getString(R.string.quiz_correct_answer, question.options[1]), question.explanation)
        expectedText.forEach { text ->
            assertEquals("A long block was omitted or recreated", 1, layouts.keys.count { it.text.toString() == text })
        }
        val promptLayout = layouts.keys.single { it.text.toString() == "1. $prompt" }
        val quotationLayout = layouts.keys.single { it.text.toString() == quotation }
        assertEquals("The complete question must survive the style split", question.question,
            promptLayout.text.toString().removePrefix("1. ") + "\n\n" + quotationLayout.text)
        assertEquals(ResourcesCompat.getFont(context, R.font.poppins_semibold), promptLayout.paint.typeface)
        assertEquals(ResourcesCompat.getFont(context, R.font.poppins_regular), quotationLayout.paint.typeface)
        assertTrue("The quotation should be lighter in scale than the prompt", quotationLayout.paint.textSize < promptLayout.paint.textSize)
        val file = File(context.cacheDir, "quiz-long-question-test.pdf")
        try {
            file.outputStream().use { report.write(pages, it) }
            PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use {
                assertEquals(pages.content.size, it.pageCount)
            }
        } finally { file.delete() }
    }

    @Test fun questionsThatFitOnOnePageKeepTheirAnswersAndReferenceTogether() {
        val questions = (1..5).map { index ->
            QuizQuestion("short-$index", "John 3:${15 + index}", "Question $index: Which answer is correct?\n\nA short Scripture passage for question $index.",
                listOf("Selected $index", "Correct $index", "Third $index", "Fourth $index"), 1, "Explanation $index.")
        }
        val session = QuizSession(questionIds = questions.map { it.id }, answers = List(questions.size) { 0 },
            position = questions.lastIndex, finished = true)
        val report = QuizReport(context, Translation.BSB, session, questions)
        val pages = report.layout()
        assertPaperBounds(pages)
        assertTrue("The questions must exercise moving a whole card to the next page", pages.content.size > 1)
        questions.forEachIndexed { index, question ->
            val expectedText = setOf("${index + 1}. ${question.question.substringBefore("\n\n")}", question.question.substringAfter("\n\n"),
                report.resources.getString(R.string.quiz_your_answer, question.options[0]),
                report.resources.getString(R.string.quiz_correct_answer, question.options[1]), question.explanation,
                "${report.resources.getString(R.string.quiz_reference)}: ${BibleReferenceFormatter.formatFull(question.reference, Translation.BSB)}")
            val matchingFragments = pages.content.flatMapIndexed { pageIndex, fragments ->
                fragments.filter { it.layout.text.toString() in expectedText }.map { pageIndex to it }
            }
            assertEquals(expectedText, matchingFragments.map { it.second.layout.text.toString() }.toSet())
            assertEquals("Question ${index + 1} was split despite fitting on a page", 1, matchingFragments.map { it.first }.distinct().size)
        }
    }

    @Test fun everyQuestionIncludesSelectedAndCorrectAnswersInTheSelectedBibleLanguage() {
        listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910,
            Translation.RIV1927, Translation.RV1909, Translation.ADB1905).forEach { translation ->
            val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation)).take(2)
            val session = QuizSession(questionIds = questions.map { it.id },
                answers = questions.mapIndexed { index, q -> if (index == 0) q.correctIndex else (q.correctIndex + 1) % q.options.size },
                position = 1, finished = true)
            val report = QuizReport(context, translation, session, questions)
            val text = report.layout().content.flatten().distinctBy { it.layout }.map { it.layout.text.toString() }
            questions.forEachIndexed { index, question ->
                val start = text.indexOf("${index + 1}. ${question.question.substringBefore("\n\n")}")
                assertTrue("Missing question in ${translation.id}", start >= 0)
                val end = if (index == questions.lastIndex) text.size else text.indexOf("${index + 2}. ${questions[index + 1].question.substringBefore("\n\n")}")
                val answer = text.subList(start, end)
                if ("\n\n" in question.question) assertTrue("Missing quotation in ${translation.id}", answer.contains(question.question.substringAfter("\n\n")))
                assertTrue(answer.contains(report.resources.getString(R.string.quiz_your_answer, question.options[session.answers[index]])))
                assertTrue(answer.contains(report.resources.getString(R.string.quiz_correct_answer, question.options[question.correctIndex])))
                assertTrue(answer.contains(report.resources.getString(if (index == 0) R.string.quiz_correct else R.string.quiz_incorrect)))
                assertTrue(answer.contains("${report.resources.getString(R.string.quiz_reference)}: ${BibleReferenceFormatter.formatFull(question.reference, translation)}"))
                if (question.explanation.isNotBlank()) assertTrue(answer.contains(question.explanation))
            }
        }
    }

    @Test fun expiredRoundsExportUnansweredQuestionsAndCorrectAnswersInAllSevenLanguages() {
        listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910,
            Translation.RIV1927, Translation.RV1909, Translation.ADB1905).forEach { translation ->
            val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation)).take(3)
            val session = QuizSession(questionIds = questions.map { it.id })
                .choose(questions.first().correctIndex).submit().advance().finishOnTimeout()
            assertEquals(listOf(questions.first().correctIndex, -1, -1), session.answers)
            val report = QuizReport(context, translation, session, questions)
            val pages = report.layout()
            assertPaperBounds(pages)
            val text = pages.content.flatten().distinctBy { it.layout }.map { it.layout.text.toString() }
            val unanswered = report.resources.getString(R.string.quiz_unanswered)
            assertEquals("Unanswered result labels missing for ${translation.id}", 2, text.count { it == unanswered })
            assertEquals("Unanswered selections missing for ${translation.id}", 2,
                text.count { it == report.resources.getString(R.string.quiz_your_answer, unanswered) })
            assertFalse("Unanswered must not be labelled Incorrect", text.contains(report.resources.getString(R.string.quiz_incorrect)))
            questions.forEach { question ->
                assertTrue(text.contains(report.resources.getString(R.string.quiz_correct_answer, question.options[question.correctIndex])))
            }
            assertFalse(text.any { it.contains("play.google.com") })
            val file = File.createTempFile("quiz-expired-${translation.id}-", ".pdf", context.cacheDir)
            try {
                file.outputStream().use { report.write(pages, it) }
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
                    assertEquals(pages.content.size, pdf.pageCount)
                    repeat(pdf.pageCount) { index -> pdf.openPage(index).use { page ->
                        assertEquals(595, page.width)
                        assertEquals(842, page.height)
                    } }
                }
            } finally { file.delete() }
        }
    }

    @Suppress("DEPRECATION")
    @Test fun sharingAttachesAReadablePdfWithTemporaryReadPermissionAndAUniqueFile() = runBlocking {
        val report = report(Translation.MAL1910)
        val files = mutableListOf<File>()
        try {
            repeat(2) { files += QuizReportExport.createCachedPdf(context, report) }
            assertNotEquals(files[0], files[1])
            files.forEach { file ->
                val intent = QuizReportExport.shareIntent(context, report, file)
                assertEquals(Intent.ACTION_SEND, intent.action)
                assertEquals("application/pdf", intent.type)
                assertEquals(report.title, intent.getStringExtra(Intent.EXTRA_SUBJECT))
                val sharedText = requireNotNull(intent.getStringExtra(Intent.EXTRA_TEXT))
                assertEquals(report.shareText, sharedText)
                assertTrue(sharedText.contains(report.resources.getString(R.string.quiz_pdf_share_attribution)))
                assertTrue(sharedText.contains("https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"))
                assertTrue(sharedText.contains(report.score))
                assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
                assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val uri = requireNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
                assertEquals("content", uri.scheme)
                assertEquals("${context.packageName}.files", uri.authority)
                assertEquals(uri, intent.clipData?.getItemAt(0)?.uri)
                assertEquals("application/pdf", context.contentResolver.getType(uri))
                context.contentResolver.openInputStream(uri)!!.use { input ->
                    val header = ByteArray(5)
                    assertEquals(5, input.read(header))
                    assertEquals("%PDF-", String(header, Charsets.US_ASCII))
                }
                PdfRenderer(context.contentResolver.openFileDescriptor(uri, "r")!!).use { pdf ->
                    assertEquals(report.layout().content.size, pdf.pageCount)
                    pdf.openPage(0).use { assertEquals(595, it.width) }
                }
            }
        } finally { files.forEach { it.delete() } }
    }
}
