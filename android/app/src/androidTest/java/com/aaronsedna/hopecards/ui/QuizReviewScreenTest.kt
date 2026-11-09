package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.export.QuizReport
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.screens.BibleQuizScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class QuizReviewScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun englishReviewAndPdfShowAllAnswerStates() = checkReview(Translation.BSB, 1f, "english")
    @Test fun malayalamReviewAndPdfShowAllAnswerStates() = checkReview(Translation.MAL1910, 1f, "malayalam")
    @Test fun malayalamReviewRemainsReadableAtDoubleFontScale() = checkReview(Translation.MAL1910, 2f, "malayalam-large-font")
    @Test fun midnightThemeKeepsSkippedAnswerAndReferenceReadable() = checkReview(Translation.BSB, 1f, "midnight", ThemeName.MIDNIGHT)

    @Test fun emptyFilterRestoresWithoutChangingScoreAndShareStaysVisible() {
        val question = BibleQuizRepository(context).load(QuizLanguage.ENGLISH).first()
        val session = QuizSession(questionIds = listOf(question.id)).choose(question.correctIndex).submit().advance()
        val restore = StateRestorationTester(compose)
        restore.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(listOf(question), Translation.BSB, onStartRound = { session })
            }
        }
        compose.onNodeWithTag("quiz_start").performClick()
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_review"))
        compose.onNodeWithTag("quiz_review").performClick()
        compose.onNodeWithTag("quiz_review_filter_INCORRECT").performClick()
        compose.onNodeWithTag("quiz_review_empty").assertIsDisplayed()
        compose.onNodeWithTag("quiz_export").assertIsDisplayed().assertIsEnabled()
        restore.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("quiz_review_filter_INCORRECT").assertIsSelected()
        compose.onNodeWithTag("quiz_review_empty").assertIsDisplayed()
        compose.onNodeWithTag("quiz_review_filter_ALL").performClick()
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_review_card_${question.id}"))
        compose.onNodeWithTag("quiz_review_card_${question.id}").assertExists()
        compose.onNodeWithTag("quiz_back_results").performClick()
        compose.onNodeWithTag("quiz_score").assertTextEquals("1 of 1 correct")
    }

    private fun checkReview(translation: Translation, fontScale: Float, name: String, theme: ThemeName = ThemeName.CLASSIC) {
        val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation))
            .filter { it.explanation.isNotBlank() }.sortedBy { it.question.length }.take(3)
        assertEquals(3, questions.size)
        val session = QuizSession(questionIds = questions.map { it.id },
            answers = listOf(questions[0].correctIndex, (questions[1].correctIndex + 1) % 4, -1),
            position = 2, selectedIndex = -1, finished = true, timedOut = true,
            answerTimesMillis = listOf(8_000L, 12_000L, 30_000L))
        val content = QuizReviewContent(context, translation, session, questions)
        val directory = File(context.getExternalFilesDir(null), "quiz-review").apply { mkdirs() }
        if (fontScale == 1f && theme == ThemeName.CLASSIC) {
            val report = QuizReport(context, translation, session, questions)
            File(directory, "$name.pdf").outputStream().use { report.write(report.layout(), it) }
        }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                HopeCardsTheme(theme) {
                    Box(Modifier.safeDrawingPadding()) {
                        BibleQuizScreen(questions, translation, hapticsEnabled = false, onStartRound = { session })
                    }
                }
            }
        }
        compose.onNodeWithTag("quiz_start").performClick()
        compose.onNodeWithText(content.edition).assertDoesNotExist()
        capture(directory, "$name-results.png")
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_review"))
        compose.onNodeWithTag("quiz_review").performClick()
        compose.onNodeWithTag("quiz_review_summary").assertExists()
        compose.onNodeWithText(content.edition).assertDoesNotExist()
        capture(directory, "$name-summary.png")
        content.answers.forEachIndexed { index, answer ->
            compose.onNodeWithTag("bible_quiz").performScrollToNode(hasContentDescription(answer.status))
            compose.onNodeWithContentDescription(answer.status).assertIsDisplayed()
            if (answer.outcome != QuizReviewContent.Outcome.UNANSWERED) compose.onNodeWithText(answer.status).assertDoesNotExist()
            val card = hasAnyAncestor(hasTestTag("quiz_review_card_${answer.id}"))
            val question = questions[index]
            if (answer.outcome == QuizReviewContent.Outcome.CORRECT) {
                listOf(R.string.quiz_your_answer, R.string.quiz_correct_answer).forEach { label ->
                    compose.onNode(card and hasText(content.resources.getString(label,
                        question.options[question.correctIndex]))).assertDoesNotExist()
                }
            } else if (answer.outcome == QuizReviewContent.Outcome.UNANSWERED) {
                compose.onNode(card and hasText(content.resources.getString(R.string.quiz_your_answer,
                    answer.status))).assertDoesNotExist()
            }
            listOfNotNull(answer.prompt, answer.selectedValue, answer.correctValue.takeIf { answer.correctAnswer != null }, answer.explanation, answer.shortReference, answer.duration).forEach { text ->
                compose.onNodeWithTag("bible_quiz").performScrollToNode(hasText(text))
                val node = compose.onNodeWithText(text)
                node.assertIsDisplayed()
                val results = mutableListOf<TextLayoutResult>()
                node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                assertTrue("Missing text layout", results.isNotEmpty())
                assertFalse("Review text was truncated: $text. Layouts=" + results.joinToString { result ->
                    "size=${result.size}, paragraph=${result.multiParagraph.width}x${result.multiParagraph.height}, " +
                        "lines=${result.lineCount}, widthOverflow=${result.didOverflowWidth}, heightOverflow=${result.didOverflowHeight}, " +
                        "right=${result.getLineRight(result.lineCount - 1)}, bottom=${result.getLineBottom(result.lineCount - 1)}"
                }, results.any { it.hasVisualOverflow })
                if (text == (answer.correctValue.takeIf { answer.correctAnswer != null } ?: answer.selectedValue)) capture(directory, "$name-answer-${index + 1}.png")
            }
        }
        compose.onNodeWithTag("quiz_export").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_review_filter_INCORRECT"))
        compose.onNodeWithTag("quiz_review_filter_INCORRECT").performClick().assertIsSelected()
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasText(content.answers[1].prompt))
        compose.onNodeWithText(content.answers[1].prompt).assertIsDisplayed()
        compose.onNodeWithText(content.answers[0].prompt).assertDoesNotExist()
        compose.onNodeWithText(content.answers[2].prompt).assertDoesNotExist()
        compose.onNodeWithTag("quiz_export").assertIsDisplayed()
        compose.onNodeWithTag("quiz_back_results").performClick()
        compose.onNodeWithTag("quiz_score").assertTextEquals(content.score)
    }

    private fun capture(directory: File, name: String) {
        compose.waitForIdle()
        val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
