package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.*
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BibleQuizNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun drawerAndSettingsKeepTheQuizLanguageInSync() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        val before = runBlocking { repository.currentSettings() }
        try {
            runBlocking { repository.updateSettings { it.copy(preferredTranslation = Translation.BSB) } }
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            scroll("quiz_start").performClick()
            assertQuestionLanguage(Translation.BSB)
            openDestination("Settings")
            compose.onNodeWithText("Bible Translation").performScrollTo().performClick()
            compose.onNodeWithText("MAL · Sathyavedapusthakam (1910)").performScrollTo().performClick()
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            scroll("quiz_start").assertTextEquals("ആരംഭിക്കാം").performClick()
            assertQuestionLanguage(Translation.MAL1910)
            capture("quiz-app-malayalam.png")

            // Changing the shared setting updates the quiz while retaining each language’s round.
            openDestination("Settings")
            compose.onNodeWithText("Bible Translation").performScrollTo().performClick()
            compose.onNodeWithText("LUT · Lutherbibel (1912)").performScrollTo().performClick()
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            scroll("quiz_start").assertTextEquals("Quiz starten").performClick()
            assertQuestionLanguage(Translation.LUT1912)
            capture("quiz-app-german.png")

            // Visiting another feature must preserve the unfinished German round.
            val question = scroll("quiz_question").fetchSemanticsNode().config[SemanticsProperties.Text].first().text
            openDestination("Settings")
            openDestination("Bible Quiz")
            waitFor("quiz_question")
            scroll("quiz_question").assertTextEquals(question)
        } catch (failure: Throwable) {
            runCatching { compose.onRoot(useUnmergedTree = true).printToLog("QuizNavigation") }
            capture("quiz-navigation-failure.png")
            throw failure
        } finally {
            runBlocking { repository.replaceSettings(before) }
        }
    }

    private fun openDestination(label: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Open navigation").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Open navigation").performClick()
        compose.onAllNodesWithText(label).filter(hasClickAction()).onFirst().performScrollTo().performClick()
    }

    private fun waitFor(tag: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("bible_quiz").fetchSemanticsNodes().isNotEmpty() }
        scroll(tag).assertExists()
    }
    private fun scroll(tag: String): SemanticsNodeInteraction {
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag(tag))
        return compose.onNodeWithTag(tag)
    }

    private fun assertQuestionLanguage(edition: Translation) {
        waitFor("quiz_question")
        val actual = scroll("quiz_question").fetchSemanticsNode().config[SemanticsProperties.Text].first().text
        val questions = BibleQuizRepository(InstrumentationRegistry.getInstrumentation().targetContext).load(QuizLanguage.forTranslation(edition))
        assertTrue(questions.any { it.question == actual })
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val image = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "quiz-preview/$name")
        file.parentFile!!.mkdirs()
        file.outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }
}
