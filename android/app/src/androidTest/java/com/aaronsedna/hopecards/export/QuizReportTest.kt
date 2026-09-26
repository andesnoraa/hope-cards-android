package com.aaronsedna.hopecards.export

import android.graphics.pdf.PdfRenderer
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.os.ParcelFileDescriptor
import android.print.PageRange
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.*
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

    @Test fun allLanguagesProduceReadableMultipageReportsAndRespectPaperBounds() {
        listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910,
            Translation.RIV1927, Translation.RV1909, Translation.ADB1905).forEach { translation ->
            val report = report(translation)
            assertTrue(report.shareText.contains(report.score))
            assertTrue(report.shareText.contains(translation.label))
            for (paper in listOf(QuizReport.Paper(), QuizReport.Paper(842, 595), QuizReport.Paper(298, 420))) {
                val pages = report.layout(paper)
                assertTrue(pages.content.size > 1)
                pages.content.forEach { fragments ->
                    assertTrue(fragments.isNotEmpty())
                    fragments.forEach {
                        assertTrue(it.y >= paper.top)
                        assertTrue(it.y + it.layout.getLineBottom(it.end - 1) - it.layout.getLineTop(it.first) <= paper.height - paper.bottom - 30)
                    }
                }
                val file = File(context.getExternalFilesDir(null), "quiz-report-${translation.id}-${paper.width}.pdf")
                file.outputStream().use { report.write(pages, it) }
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use {
                    assertEquals(pages.content.size, it.pageCount)
                    it.openPage(0).use { page -> assertEquals(paper.width, page.width) }
                }
            }
        }
    }

    @Test fun selectedPrintPagesAndCancellationAreHonored() {
        val report = report(Translation.MAL1910)
        val pages = report.layout()
        val file = File(context.cacheDir, "quiz-selected-test.pdf")
        try {
            val written = file.outputStream().use { report.write(pages, it, arrayOf(PageRange(1, 1))) }
            assertArrayEquals(arrayOf(PageRange(1, 1)), written)
            PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { assertEquals(1, it.pageCount) }
            val signal = CancellationSignal().apply { cancel() }
            try { report.layout(signal = signal); fail("Cancelled layout succeeded") } catch (_: OperationCanceledException) { }
            val output = ByteArrayOutputStream()
            try { report.write(pages, output, signal = signal); fail("Cancelled write succeeded") } catch (_: OperationCanceledException) { }
            assertEquals(0, output.size())
        } finally { file.delete() }
    }
}
