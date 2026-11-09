package com.aaronsedna.hopecards.ui

import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.export.QuizReport
import com.aaronsedna.hopecards.model.*
import org.junit.Assert.*
import org.junit.Test

class QuizReviewParityTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun screenAndPdfUseIdenticalOrderedAnswersIncludingCorrectIncorrectAndSkippedQuestions() {
        listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910,
            Translation.RIV1927, Translation.RV1909, Translation.ADB1905).forEach { translation ->
            val bank = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation)).take(3)
            val questions = listOf(bank[2], bank[0], bank[1])
            val selections = listOf(questions[0].correctIndex, (questions[1].correctIndex + 1) % 4, -1)
            val session = QuizSession(questionIds = questions.map { it.id }, answers = selections,
                position = 2, selectedIndex = -1, finished = true, timedOut = true,
                answerTimesMillis = listOf(8_000L, 12_000L, 30_000L))
            val content = QuizReviewContent(context, translation, session, bank)
            val report = QuizReport(context, translation, session, bank)
            assertEquals(questions.map { it.id }, content.answers.map { it.id })
            assertEquals(content.answers, report.review.answers)
            assertEquals(listOf(QuizReviewContent.Outcome.CORRECT, QuizReviewContent.Outcome.INCORRECT,
                QuizReviewContent.Outcome.UNANSWERED), content.answers.map { it.outcome })
            val pages = report.layout()
            assertEquals(content.answers.flatMap { if (it.outcome == QuizReviewContent.Outcome.INCORRECT)
                listOf(it.outcome, QuizReviewContent.Outcome.CORRECT) else listOf(it.outcome) }, pages.panels.flatten().mapNotNull { it.marker })
            val texts = pages.content.flatten().distinctBy { it.layout }.map { it.layout.text.toString() }
            assertTrue(texts.containsAll(listOf(content.title, content.reviewTitle, content.score)))
            assertFalse(texts.contains(content.edition))
            content.answers.forEachIndexed { index, answer ->
                assertTrue(texts.containsAll(listOfNotNull(answer.prompt, answer.timeTaken, answer.selectedAnswer,
                    answer.correctAnswer, answer.reference)))
                assertEquals(answer.outcome == QuizReviewContent.Outcome.UNANSWERED, texts.contains(answer.status))
                assertEquals(content.resources.getString(R.string.quiz_elapsed_time,
                    listOf("00:08", "00:12", "00:30")[index]), answer.timeTaken)
                answer.passage?.let { assertTrue(texts.contains(it)) }
                if (answer.explanation.isNotBlank()) {
                    assertTrue(texts.contains(answer.explanation))
                    assertFalse(texts.contains(content.explanationLabel))
                }
                val expectedCorrect = content.resources.getString(R.string.quiz_correct_answer,
                    questions[index].options[questions[index].correctIndex])
                when (index) {
                    0 -> {
                        assertEquals(questions[index].options[selections[index]], answer.selectedAnswer)
                        assertNull(answer.correctAnswer)
                        assertFalse(texts.contains(expectedCorrect))
                    }
                    1 -> {
                        assertEquals(content.resources.getString(R.string.quiz_your_answer,
                            questions[index].options[selections[index]]), answer.selectedAnswer)
                        assertEquals(expectedCorrect, answer.correctAnswer)
                    }
                    2 -> {
                        assertNull(answer.selectedAnswer)
                        assertEquals(expectedCorrect, answer.correctAnswer)
                        assertFalse(texts.contains(content.resources.getString(R.string.quiz_your_answer, answer.status)))
                    }
                }
            }
            assertEquals(selections, session.answers)
        }
    }

    @Test fun ordinaryAnswerPanelsStayWholeEvenWhenTheirQuestionSpansPages() {
        val question = QuizQuestion("continued", "John 3:16", "Which answer is correct?\n\n" +
            (1..150).joinToString(" ") { "Passage $it gives a longer quotation for review." },
            listOf("The selected answer contains a short, readable explanation.",
                "The correct answer includes the complete original wording.", "Third answer", "Fourth answer"),
            1, "An explanation remains with the reference below the answer panels.")
        val session = QuizSession(questionIds = listOf(question.id), answers = listOf(0), finished = true)
        val report = QuizReport(context, Translation.BSB, session, listOf(question))
        val pages = report.layout()
        assertTrue(pages.content.size > 2)
        val answer = report.review.answers.single()
        listOfNotNull(answer.selectedAnswer, answer.correctAnswer, answer.reference).forEach { text ->
            val fragments = pages.content.flatten().filter { it.layout.text.toString() == text }
            assertEquals("A normal answer panel was split across pages", 1, fragments.size)
            assertEquals(fragments.single().layout.lineCount, fragments.single().end)
        }
    }
}
