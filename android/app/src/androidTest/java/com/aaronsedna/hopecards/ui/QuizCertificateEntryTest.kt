package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.screens.BibleQuizScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizCertificateEntryTest {
    @get:Rule val compose = createComposeRule()

    private fun scroll(tag: String): SemanticsNodeInteraction {
        val target = compose.onNodeWithTag(tag)
        if (!target.isDisplayed()) compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag(tag))
        return target
    }

    @Test fun certificatesOpenBeforeOrAfterAQuizWithoutDiscardingTheScoreOrShowingAnAd() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val questions = BibleQuizRepository(context).load(QuizLanguage.ENGLISH).take(1)
        var completedExits = 0
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB,
                    onComplete = { _, proceed -> completedExits++; proceed() })
            }
        }
        scroll("quiz_certificates").assertIsDisplayed()
        capture("quiz-certificates-entry.png")
        scroll("quiz_certificates").performClick()
        compose.onNodeWithTag("certificate_screen").assertExists()
        compose.onNodeWithTag("certificate_close").performClick()
        scroll("quiz_start").performClick()
        scroll("quiz_option_${questions.single().correctIndex}").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_score").assertTextEquals("1 of 1 correct")
        scroll("quiz_certificates").assertIsDisplayed()
        capture("quiz-certificates-results-entry.png")
        scroll("quiz_certificates").performClick()
        compose.onNodeWithTag("certificate_screen").assertExists()
        compose.onNodeWithTag("certificate_close").performClick()
        scroll("quiz_score").assertTextEquals("1 of 1 correct")
        assertEquals(0, completedExits)
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "certificate-ui").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
