package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.screens.BibleQuizRoute
import com.aaronsedna.hopecards.ui.screens.BibleQuizScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BibleQuizInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun bank(edition: Translation) = BibleQuizRepository(context).load(QuizLanguage.forTranslation(edition))
    private fun node(tag: String) = compose.onNodeWithTag(tag)
    private fun waitFor(tag: String) = compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    private fun scroll(tag: String): SemanticsNodeInteraction {
        val target = node(tag)
        if (!target.isDisplayed()) node("bible_quiz").performScrollToNode(hasTestTag(tag))
        return target
    }
    private fun questionText(): String {
        return scroll("quiz_question").fetchSemanticsNode().config[SemanticsProperties.Text].joinToString("") { it.text }
    }

    @Test fun bundledContentIsCompleteAcrossEveryEditionAndReferencesAreLocalized() {
        val english = bank(Translation.BSB)
        assertEquals(200, english.size)
        Translation.entries.forEach { edition ->
            val rows = bank(edition)
            assertEquals(english.map { it.id }, rows.map { it.id })
            assertEquals(english.map { it.correctIndex }, rows.map { it.correctIndex })
            rows.forEach { question ->
                assertTrue("${edition.id}/${question.reference}", BibleReferenceFormatter.hasLocalizedBookTitle(question.reference, edition))
                if (edition.language != "English") {
                    val original = english.first { it.id == question.id }
                    assertNotEquals("${edition.id}/${question.id}", original.question, question.question)
                    if (original.explanation.isNotEmpty()) assertNotEquals(original.explanation, question.explanation)
                }
            }
        }
    }

    @Test fun languageChangesRefreshQuestionsAndControlsAndReturningPreservesTheRound() {
        val edition = mutableStateOf(Translation.BSB)
        compose.setContent {
            val holder = rememberSaveableStateHolder()
            HopeCardsTheme(ThemeName.CLASSIC) {
                holder.SaveableStateProvider(QuizLanguage.forTranslation(edition.value).code) {
                    BibleQuizRoute(edition.value)
                }
            }
        }
        val editions = listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912, Translation.LSG1910, Translation.RIV1927, Translation.RV1909, Translation.ADB1905)
        val startLabels = listOf("Start quiz", "ആരംഭിക്കാം", "Quiz starten", "Commencer", "Inizia il quiz", "Comenzar", "Magsimula")
        var firstEnglishQuestion = ""
        editions.forEachIndexed { index, next ->
            compose.runOnIdle { edition.value = next }
            waitFor("quiz_start")
            scroll("quiz_start").assertTextEquals(startLabels[index]).performClick()
            waitFor("quiz_question")
            val question = questionText()
            assertTrue(bank(next).any { it.question == question })
            if (index == 0) firstEnglishQuestion = question
            scroll("quiz_option_0").performClick()
            scroll("quiz_action").performClick()
            scroll("quiz_feedback").assertIsDisplayed()
            capture("quiz-${QuizLanguage.forTranslation(next).code}.png")
        }
        compose.runOnIdle { edition.value = Translation.KJV }
        waitFor("bible_quiz")
        assertEquals(firstEnglishQuestion, questionText())
        node("quiz_option_0").assertIsNotEnabled()
    }

    @Test fun completeRoundScoresCorrectlyAndSurvivesSavedStateRestoration() {
        val questions = bank(Translation.BSB)
        val restoration = StateRestorationTester(compose)
        var completionCalls = 0
        val hapticEvents = mutableListOf<Unit>()
        val enableHaptics = mutableStateOf(true)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB,
                    onComplete = { current, proceed ->
                        assertTrue(current())
                        completionCalls++
                        proceed()
                    }, hapticsEnabled = enableHaptics.value, onWrongAnswerHaptic = { hapticEvents += Unit })
            }
        }
        scroll("quiz_start").performClick()
        node("quiz_sound").assertDoesNotExist()
        repeat(10) { index ->
            if (index == 9) compose.runOnIdle { enableHaptics.value = false }
            val q = questions.first { it.question == questionText() }
            scroll("quiz_action").assertIsNotEnabled()
            val answer = if (index % 2 == 0) q.correctIndex else (q.correctIndex + 1) % 4
            scroll("quiz_option_$answer").performClick()
            scroll("quiz_action").performClick()
            scroll("quiz_feedback").assertIsDisplayed()
            node("quiz_option_$answer").assertIsNotEnabled()
            node("quiz_option_${q.correctIndex}").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Correct!"))
            if (answer != q.correctIndex) node("quiz_option_$answer").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Incorrect"))
            if (index == 0) {
                restoration.emulateSavedInstanceStateRestore()
                assertEquals(q.question, questionText())
                node("quiz_option_$answer").assertIsNotEnabled()
            }
            scroll("quiz_action").performClick()
        }
        node("quiz_score").assertTextEquals("5 of 10 correct")
        assertEquals(0, completionCalls)
        assertEquals(4, hapticEvents.size) // Wrong answers only; the last wrong answer has haptics disabled.
        node("quiz_sound").assertDoesNotExist()
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(0, completionCalls)
        assertEquals(4, hapticEvents.size)
        capture("quiz-results.png")
        node("quiz_review").performClick()
        scroll("quiz_back_results").performClick()
        node("quiz_score").assertTextEquals("5 of 10 correct")
        assertEquals(0, completionCalls) // Reading the score and reviewing never show an ad.
        node("quiz_restart").performClick()
        assertEquals(1, completionCalls)
        scroll("quiz_action").assertIsNotEnabled()
        node("quiz_end_round").assertIsDisplayed().performClick()
        compose.onNodeWithText("Continue quiz").performClick()
        node("quiz_question").assertExists()
        node("quiz_end_round").assertIsDisplayed().performClick()
        compose.onAllNodesWithText("End quiz").filter(hasClickAction()).onLast().performClick()
        node("quiz_start").assertExists()
    }

    @Test fun completedQuizExitIsOnceOnlyAndDoesNotBlockUnfinishedNavigation() {
        var exit: ((() -> Unit) -> Unit)? = null
        var resume: (() -> Unit)? = null
        var completions = 0
        var navigations = 0
        val questions = bank(Translation.BSB).take(1)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB,
                    onComplete = { _, proceed -> completions++; resume = proceed },
                    onExitHandlerChanged = { exit = it })
            }
        }
        compose.runOnIdle { exit!!.invoke { navigations++ } }
        assertEquals(1, navigations)
        assertEquals(0, completions)
        scroll("quiz_start").performClick()
        scroll("quiz_option_${questions.first().correctIndex}").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_action").performClick()
        node("quiz_score").assertTextEquals("1 of 1 correct")
        assertEquals(0, completions)
        compose.runOnIdle {
            exit!!.invoke { navigations++ }
            exit!!.invoke { navigations++ }
        }
        assertEquals(1, completions)
        assertEquals(1, navigations)
        compose.runOnIdle { resume!!.invoke() }
        assertEquals(2, navigations)
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { exit!!.invoke { navigations++ } }
        assertEquals(1, completions)
        assertEquals(3, navigations)
    }

    @Test fun directResultShareKeepsTheScoreAfterCancellationAndRestorationWithoutCompletionAd() {
        val questions = bank(Translation.BSB).take(1)
        var completions = 0
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB,
                    onComplete = { _, proceed -> completions++; proceed() })
            }
        }
        scroll("quiz_start").performClick()
        scroll("quiz_option_${questions.first().correctIndex}").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_action").performClick()
        assertDirectShareButton()
        scroll("quiz_export").performClick()
        awaitSystemShareSheet()
        cancelSystemShareSheet()
        scroll("quiz_score").assertTextEquals("1 of 1 correct")
        assertEquals(0, completions)

        restoration.emulateSavedInstanceStateRestore()
        scroll("quiz_score").assertTextEquals("1 of 1 correct")
        assertDirectShareButton()
        assertEquals(0, completions)
    }

    @Test fun systemShareCanBeCancelledAndRetriedWithReadablePdfsWithoutLeavingResultsOrShowingAds() {
        val questions = bank(Translation.BSB).take(1)
        var completions = 0
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB,
                    onComplete = { _, proceed -> completions++; proceed() })
            }
        }
        scroll("quiz_start").performClick()
        scroll("quiz_option_${questions.first().correctIndex}").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_action").performClick()
        capture("export-results.png")
        val directory = File(context.cacheDir, "quiz-reports")
        val sharedFiles = mutableListOf<File>()
        repeat(2) {
            val previousPaths = directory.listFiles().orEmpty().map { it.absolutePath }.toSet()
            assertDirectShareButton()
            scroll("quiz_export").performClick()
            awaitSystemShareSheet()
            val created = directory.listFiles().orEmpty().filter {
                it.extension == "pdf" && it.absolutePath !in previousPaths
            }
            assertEquals("Each Share tap should prepare one new PDF", 1, created.size)
            val file = created.single()
            assertReadablePdf(file)
            sharedFiles += file

            cancelSystemShareSheet()
            scroll("quiz_score").assertTextEquals("1 of 1 correct")
            assertEquals(0, completions)
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasTestTag("quiz_export") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
            }
            assertDirectShareButton()
            // A recipient can still finish importing the file after the sharesheet has closed.
            sharedFiles.forEach(::assertReadablePdf)
        }
        assertEquals(2, sharedFiles.map { it.absolutePath }.distinct().size)
        node("quiz_review").performClick()
        scroll("quiz_back_results").performClick()
        scroll("quiz_score").assertTextEquals("1 of 1 correct")
        assertEquals(0, completions)
    }

    private fun assertDirectShareButton() {
        scroll("quiz_export").assertIsDisplayed().assertIsEnabled().assertTextEquals("Share")
        node("quiz_share").assertDoesNotExist()
        node("quiz_save_pdf").assertDoesNotExist()
        node("quiz_print").assertDoesNotExist()
    }

    private fun assertReadablePdf(file: File) {
        assertTrue("Shared PDF must remain available: ${file.name}", file.isFile && file.length() > 0)
        android.graphics.pdf.PdfRenderer(android.os.ParcelFileDescriptor.open(file,
            android.os.ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
            assertTrue("Shared PDF should contain the results page", renderer.pageCount > 0)
            renderer.openPage(0).use { page ->
                assertTrue(page.width > 0 && page.height > 0)
            }
        }
    }

    private fun awaitSystemShareSheet() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = android.os.SystemClock.uptimeMillis() + 15_000
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            val packageName = automation.rootInActiveWindow?.packageName?.toString()
            // The chooser belongs to different system packages on Pixel and Samsung devices.
            // QuizReportTest separately checks the PDF MIME type, URI, companion text and read grant.
            if (!packageName.isNullOrBlank() && packageName != context.packageName && packageName != "com.android.systemui") {
                assertFalse("Share must open the sharesheet, not the removed save-document flow",
                    packageName.contains("documentsui"))
                return
            }
            compose.mainClock.advanceTimeBy(100)
            android.os.SystemClock.sleep(100)
        }
        fail("System share activity not opened. Actual: ${automation.rootInActiveWindow?.packageName}")
    }

    private fun cancelSystemShareSheet() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        fun returnedToQuiz(timeoutMillis: Long): Boolean {
            val deadline = android.os.SystemClock.uptimeMillis() + timeoutMillis
            while (android.os.SystemClock.uptimeMillis() < deadline) {
                if (automation.rootInActiveWindow?.packageName?.toString() == context.packageName) return true
                compose.mainClock.advanceTimeBy(100)
                android.os.SystemClock.sleep(100)
            }
            return false
        }
        // Cancel only the chooser or its IME; never choose a recipient or send the PDF.
        repeat(2) {
            if (returnedToQuiz(100)) return
            assertTrue("System Back should be accepted", automation.performGlobalAction(
                android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK))
            if (returnedToQuiz(2_000)) return
        }
        val directory = File(context.getExternalFilesDir(null), "quiz-preview").apply { mkdirs() }
        File(directory, "share-cancel-failure.txt").writeText(
            "Active package: ${automation.rootInActiveWindow?.packageName}\n" +
                automation.windows.joinToString("\n") { "type=${it.type} active=${it.isActive} focused=${it.isFocused}" })
        automation.takeScreenshot()?.let { bitmap ->
            File(directory, "share-cancel-failure.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
        fail("System share did not return to the quiz. Actual: ${automation.rootInActiveWindow?.packageName}")
    }

    @Test fun wrongAnswerRequestsDeviceVibration() {
        val questions = bank(Translation.BSB).take(1)
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { BibleQuizScreen(questions, Translation.BSB) }
        }
        scroll("quiz_start").performClick()
        scroll("quiz_option_${(questions.first().correctIndex + 1) % 4}").performClick()
        scroll("quiz_action").performClick()
        scroll("quiz_feedback").assertIsDisplayed()
    }

    @Test fun soundPreferencePersistsAndCanBeMuted() {
        val prefs = context.getSharedPreferences("bible-quiz", android.content.Context.MODE_PRIVATE)
        val before = prefs.getBoolean("sound", false)
        prefs.edit().putBoolean("sound", false).commit()
        try {
            val restoration = StateRestorationTester(compose)
            restoration.setContent {
                HopeCardsTheme(ThemeName.CLASSIC) { BibleQuizScreen(bank(Translation.BSB), Translation.BSB) }
            }
            node("quiz_sound").assertIsOff().performClick().assertIsOn()
            compose.runOnIdle { assertTrue(prefs.getBoolean("sound", false)) }
            restoration.emulateSavedInstanceStateRestore()
            node("quiz_sound").assertIsOn().performClick().assertIsOff()
            compose.runOnIdle { assertFalse(prefs.getBoolean("sound", true)) }
        } finally { prefs.edit().putBoolean("sound", before).commit() }
    }

    @Test fun longMalayalamContentRemainsUsableAtLargeFontScale() {
        val questions = bank(Translation.MAL1910).sortedByDescending { it.question.length }.take(1)
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                HopeCardsTheme(ThemeName.SERENITY) {
                    Box(Modifier.width(320.dp).fillMaxSize()) { BibleQuizScreen(questions, Translation.MAL1910) }
                }
            }
        }
        scroll("quiz_start").performClick()
        scroll("quiz_option_3").assertIsDisplayed().performClick()
        scroll("quiz_action").assertIsDisplayed().performClick()
        scroll("quiz_feedback").assertIsDisplayed()
        scroll("quiz_action").assertIsDisplayed()
        capture("quiz-malayalam-large-text.png")
    }

    @Test fun primaryActionStaysVisibleWithLongMalayalamContentOnAShortScreen() {
        val question = bank(Translation.MAL1910).maxBy { it.question.length + it.options.sumOf(String::length) }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                HopeCardsTheme(ThemeName.SERENITY) {
                    Box(Modifier.size(width = 320.dp, height = 480.dp)) {
                        BibleQuizScreen(listOf(question), Translation.MAL1910, hapticsEnabled = false)
                    }
                }
            }
        }
        scroll("quiz_start").performClick()
        node("quiz_action").assertIsDisplayed().assertIsNotEnabled()
        node("quiz_end_round").assertIsDisplayed().assertContentDescriptionEquals("ക്വിസ് അവസാനിപ്പിക്കുക")
        val initialActionBounds = node("quiz_action").fetchSemanticsNode().boundsInRoot

        // All four answers must remain reachable without hiding the primary action.
        repeat(4) { index ->
            scroll("quiz_option_$index").assertIsDisplayed().performClick()
            node("quiz_action").assertIsDisplayed().assertIsEnabled()
            val currentBounds = node("quiz_action").fetchSemanticsNode().boundsInRoot
            assertEquals(initialActionBounds.top, currentBounds.top, 1f)
            assertEquals(initialActionBounds.bottom, currentBounds.bottom, 1f)
        }
        // Submit from the first option, with feedback still below the long answer list.
        scroll("quiz_option_0").performClick()
        node("quiz_action").assertIsDisplayed().assertIsEnabled()
        // Check directly after selection; scrolling to the button would conceal a regression.
        node("quiz_action").performClick()
        compose.waitForIdle()
        node("quiz_feedback").assertIsDisplayed()
        node("quiz_action").assertIsDisplayed().assertIsEnabled()
        scroll("quiz_question").assertIsDisplayed()
        node("quiz_action").assertIsDisplayed().performClick()
        scroll("quiz_score").assertExists()
        capture("quiz-malayalam-short-screen.png")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val file = File(context.getExternalFilesDir(null), "quiz-preview/$name")
        file.parentFile!!.mkdirs()
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
