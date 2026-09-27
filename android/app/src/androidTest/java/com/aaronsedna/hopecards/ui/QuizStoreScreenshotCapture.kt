package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.*
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Captures the actual app with the existing debug-only ad suppression. */
class QuizStoreScreenshotCapture {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun captureQuizInEveryLanguage() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(BuildConfig.SCREENSHOT_MODE &&
            InstrumentationRegistry.getArguments().getString("captureQuizStore") == "true")
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val editions = mapOf("en-US" to Translation.BSB, "de-DE" to Translation.LUT1912,
            "es-419" to Translation.RV1909, "fil" to Translation.ADB1905,
            "fr-FR" to Translation.LSG1910, "it-IT" to Translation.RIV1927,
            "ml-IN" to Translation.MAL1910)
        try {
            editions.forEach { (locale, edition) ->
                repository.replaceSettings(original.copy(preferredTranslation = edition,
                    enableHaptics = false, dailyHopeMusicEnabled = false, themeName = ThemeName.CLASSIC))
                val questions = BibleQuizRepository(context).load(QuizLanguage.forTranslation(edition))
                val output = File(context.getExternalFilesDir(null), "quiz-store/$locale").apply { mkdirs() }
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    lateinit var vm: HopeCardsViewModel
                    scenario.onActivity {
                        vm = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                        it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    compose.waitUntil(20_000) { vm.uiState.value.initialized }
                    compose.runOnIdle { vm.navigate(Destination.BIBLE_QUIZ) }
                    compose.waitUntil(15_000) {
                        runCatching { compose.onNodeWithTag("quiz_start").assertExists() }.isSuccess
                    }
                    scroll("quiz_start").performClick()
                    var capturedQuestion = false
                    repeat(10) { index ->
                        val text = scroll("quiz_question").fetchSemanticsNode()
                            .config[SemanticsProperties.Text].joinToString("") { it.text }
                        val question = questions.first { it.question == text }
                        val answer = if (index < 8) question.correctIndex else (question.correctIndex + 1) % 4
                        scroll("quiz_option_$answer").performClick()
                        // Prefer a complete short question for store artwork, using the real round.
                        val captureQuestion = !capturedQuestion && index < 8 &&
                            !question.question.contains('\n') && question.question.length < 150
                        if (captureQuestion) capture(output, "01-quiz-question.png")
                        scroll("quiz_action").performClick()
                        if (captureQuestion) {
                            scroll("quiz_feedback").assertIsDisplayed()
                            capture(output, "02-answer-feedback.png")
                            capturedQuestion = true
                        }
                        scroll("quiz_action").performClick()
                    }
                    compose.onNodeWithTag("quiz_score").assertIsDisplayed()
                    capture(output, "03-quiz-results.png")
                }
            }
        } finally { repository.replaceSettings(original) }
    }

    private fun scroll(tag: String): SemanticsNodeInteraction {
        compose.onNodeWithTag("bible_quiz").performScrollToNode(hasTestTag(tag))
        return compose.onNodeWithTag(tag)
    }

    private fun capture(output: File, name: String) {
        compose.waitForIdle()
        android.os.SystemClock.sleep(350)
        val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(output, name).outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
        println("Captured ${output.name}/$name")
    }
}
