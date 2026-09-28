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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
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

    private fun checkReview(translation: Translation, fontScale: Float, name: String, theme: ThemeName = ThemeName.CLASSIC) {
        val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(translation))
            .filter { it.explanation.isNotBlank() }.sortedBy { it.question.length }.take(3)
        assertEquals(3, questions.size)
        val session = QuizSession(questionIds = questions.map { it.id },
            answers = listOf(questions[0].correctIndex, (questions[1].correctIndex + 1) % 4, -1),
            position = 2, selectedIndex = -1, finished = true, timedOut = true)
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
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_review"))
        compose.onNodeWithTag("quiz_review").performClick()
        compose.onNodeWithTag("quiz_review_summary").assertExists()
        capture(directory, "$name-summary.png")
        content.answers.forEachIndexed { index, answer ->
            listOf(answer.prompt, answer.selectedAnswer, answer.correctAnswer, answer.explanation, answer.reference).forEach { text ->
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
                if (text == answer.correctAnswer) capture(directory, "$name-answer-${index + 1}.png")
            }
        }
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_export"))
        compose.onNodeWithTag("quiz_export").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag("quiz_back_results"))
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
