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
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain

class BibleQuizNavigationTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val settings = object : ExternalResource() {
        private lateinit var repository: AppRepository
        private var original: AppSettings? = null

        override fun before() = runBlocking {
            repository = AppRepository(InstrumentationRegistry.getInstrumentation().targetContext)
            repository.initialize()
            original = repository.currentSettings()
            // Set up before Activity launch so the first-run permission dialog cannot cover
            // the drawer. Preserve the device permission; notification behavior has its own tests.
            repository.updateSettings {
                it.copy(preferredTranslation = Translation.BSB, dailyHopeReminderEnabled = false)
            }
        }

        override fun after() {
            original?.let { saved ->
                runBlocking { repository.replaceSettings(saved) }
                val reminders = ReminderScheduler(InstrumentationRegistry.getInstrumentation().targetContext)
                if (saved.dailyHopeReminderEnabled) {
                    reminders.schedule(saved.dailyHopeReminderHour, saved.dailyHopeReminderMinute, saved.preferredTranslation)
                } else {
                    reminders.cancel()
                }
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(settings).around(compose)

    @Test fun drawerAndSettingsKeepTheQuizLanguageInSync() {
        try {
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            scroll("quiz_start").performClick()
            assertQuestionLanguage(Translation.BSB)
            openDestination("Settings")
            compose.onNodeWithText("Bible Translation").performScrollTo().performClick()
            compose.onNodeWithText("MAL · Sathyavedapusthakam (1910)").performScrollTo().performClick()
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            compose.onNodeWithText("ബൈബിൾ ക്വിസ്").assertIsDisplayed()
            scroll("quiz_start").assertTextEquals("ആരംഭിക്കാം").performClick()
            assertQuestionLanguage(Translation.MAL1910)
            capture("quiz-app-malayalam.png")

            // Changing the shared setting updates the quiz while retaining each language’s round.
            openDestination("Settings")
            compose.onNodeWithText("Bible Translation").performScrollTo().performClick()
            compose.onNodeWithText("LUT · Lutherbibel (1912)").performScrollTo().performClick()
            openDestination("Bible Quiz")
            waitFor("quiz_start")
            compose.onNodeWithText("Bibelquiz").assertIsDisplayed()
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
        val target = compose.onNodeWithTag(tag)
        if (!target.isDisplayed()) compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag(tag))
        return target
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
