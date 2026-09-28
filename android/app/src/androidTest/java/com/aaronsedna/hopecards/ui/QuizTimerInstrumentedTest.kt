package com.aaronsedna.hopecards.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.QuizQuestion
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.screens.BibleQuizScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class QuizTimerInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private val preferences get() = InstrumentationRegistry.getInstrumentation().targetContext
        .getSharedPreferences("bible-quiz", Context.MODE_PRIVATE)
    private var previousPreference: Boolean? = null
    private var previousSoundPreference: Boolean? = null
    private val questions = listOf(QuizQuestion("timer-test", "John 3:16", "Who loved the world?",
        listOf("God", "Peter", "Paul", "Moses"), 0, ""))
    private fun node(tag: String) = compose.onNodeWithTag(tag)
    private fun visible(tag: String): SemanticsNodeInteraction {
        val target = node(tag)
        if (!target.isDisplayed()) node("bible_quiz").performScrollToNode(hasTestTag(tag))
        return target
    }
    private fun finishRound() {
        visible("quiz_option_0").performClick()
        node("quiz_action").performClick()
        node("quiz_action").performClick()
    }

    @Before fun resetQuizSettings() {
        previousPreference = if (preferences.contains("timer")) preferences.getBoolean("timer", false) else null
        previousSoundPreference = if (preferences.contains("sound")) preferences.getBoolean("sound", true) else null
        preferences.edit().remove("timer").remove("sound").commit()
    }

    @After fun restoreQuizSettings() {
        val edit = preferences.edit()
        previousPreference?.let { edit.putBoolean("timer", it) } ?: edit.remove("timer")
        previousSoundPreference?.let { edit.putBoolean("sound", it) } ?: edit.remove("sound")
        edit.commit()
    }

    @Test fun timerDefaultsOffAndUntimedRoundsDoNotShowDuration() {
        compose.setContent { HopeCardsTheme(ThemeName.CLASSIC) { BibleQuizScreen(questions, Translation.BSB) } }
        visible("quiz_timer").assertIsOff()
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertDoesNotExist()
        finishRound()
        node("quiz_elapsed_result").assertDoesNotExist()
    }

    @Test fun preferencePersistsAndTimedRoundRestoresFreezesAndResetsEvenWithIdenticalQuestions() {
        var now = 10_000L
        val showScreen = mutableStateOf(true)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                if (showScreen.value) BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now })
            }
        }
        visible("quiz_timer").performClick().assertIsOn()
        assertTrue(preferences.getBoolean("timer", false))
        // Removing the composition recreates the preference reader as reopening the quiz would.
        compose.runOnIdle { showScreen.value = false }
        compose.runOnIdle { showScreen.value = true }
        visible("quiz_timer").assertIsOn()
        capture("timer-english-intro.png")
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        compose.runOnIdle { now = 14_500L }
        compose.mainClock.advanceTimeBy(1_100L)
        // Round remaining seconds upward so a partly elapsed second is still playable.
        node("quiz_timer_display").assertTextEquals("00:26")
        compose.runOnIdle { now = 15_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        node("quiz_timer_display").assertTextEquals("00:25")
        capture("timer-english-playing.png")
        compose.runOnIdle { now = 18_000L }
        restoration.emulateSavedInstanceStateRestore()
        node("quiz_timer_display").assertTextEquals("00:22")
        compose.runOnIdle { now = 23_000L }
        finishRound()
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:13")
        compose.runOnIdle { now = 123_000L }
        restoration.emulateSavedInstanceStateRestore()
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:13")
        node("quiz_time_up").assertDoesNotExist()
        node("quiz_timer_display").assertDoesNotExist()
        visible("quiz_review").performClick()
        visible("quiz_back_results").performClick()
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:13")
        visible("quiz_restart").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        node("quiz_end_round").performClick()
        compose.onAllNodesWithText("End quiz").filter(hasClickAction()).onLast().performClick()
        visible("quiz_timer").assertIsOn().performClick().assertIsOff()
        assertFalse(preferences.getBoolean("timer", true))
    }

    @Test fun backgroundingStopsUiTicksButCountdownContinuesOnResume() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        var clockReads = 0
        lateinit var owner: TimerLifecycleOwner
        compose.runOnUiThread { owner = TimerLifecycleOwner() }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                HopeCardsTheme(ThemeName.CLASSIC) {
                    BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { clockReads++; now })
                }
            }
        }
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeBy(1_100L)
        val readsWhileStopped = clockReads
        compose.runOnIdle { now = 35_000L }
        compose.mainClock.advanceTimeBy(4_000L)
        org.junit.Assert.assertEquals(readsWhileStopped, clockReads)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        node("quiz_timer_display").assertTextEquals("00:05")
    }

    @Test fun enabledTimerAndActionRemainVisibleWithLongMalayalamOnAShortScreenAtLargeFontScale() {
        preferences.edit().putBoolean("timer", true).commit()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val question = BibleQuizRepository(context).load(QuizLanguage.MALAYALAM)
            .maxBy { it.question.length + it.options.sumOf(String::length) }
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                HopeCardsTheme(ThemeName.SERENITY) {
                    Box(Modifier.size(width = 320.dp, height = 480.dp)) {
                        BibleQuizScreen(listOf(question), Translation.MAL1910, hapticsEnabled = false,
                            elapsedRealtimeMillis = { 10_000L })
                    }
                }
            }
        }
        visible("quiz_timer").assertIsOn()
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertIsDisplayed().assertTextEquals("00:30")
            .assertContentDescriptionEquals(context.forQuizTranslation(Translation.MAL1910)
                .getString(R.string.quiz_time_remaining, "00:30"))
        node("quiz_action").assertIsDisplayed().assertIsNotEnabled()
        val initialActionBounds = node("quiz_action").fetchSemanticsNode().boundsInRoot
        repeat(4) { index ->
            visible("quiz_option_$index").assertIsDisplayed().performClick()
            node("quiz_timer_display").assertIsDisplayed()
            node("quiz_action").assertIsDisplayed().assertIsEnabled()
            val actionBounds = node("quiz_action").fetchSemanticsNode().boundsInRoot
            assertEquals(initialActionBounds.top, actionBounds.top, 1f)
            assertEquals(initialActionBounds.bottom, actionBounds.bottom, 1f)
        }
        capture("timer-malayalam-short-screen-playing.png")
        // Tap the pinned action directly, without scrolling it into view first.
        node("quiz_action").performClick()
        node("quiz_timer_display").assertIsDisplayed()
        node("quiz_action").assertIsDisplayed().assertIsEnabled()
        node("quiz_feedback").assertIsDisplayed()
        capture("timer-malayalam-short-screen-feedback.png")
    }

    @Test fun soundDefaultsOnAndBothSoundChoicesAreRememberedWhenTheQuizReopens() {
        val showScreen = mutableStateOf(true)
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                if (showScreen.value) BibleQuizScreen(questions, Translation.BSB)
            }
        }
        visible("quiz_sound").assertIsOn().performClick().assertIsOff()
        assertFalse(preferences.getBoolean("sound", true))
        compose.runOnIdle { showScreen.value = false }
        compose.runOnIdle { showScreen.value = true }
        visible("quiz_sound").assertIsOff().performClick().assertIsOn()
        assertTrue(preferences.getBoolean("sound", false))
        compose.runOnIdle { showScreen.value = false }
        compose.runOnIdle { showScreen.value = true }
        visible("quiz_sound").assertIsOn()
        visible("quiz_timer").assertIsOff()
    }

    @Test fun tenQuestionRoundGivesEachQuestionThirtySecondsAndExpiresOnlyOneAtATime() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        val tenQuestions = (1..10).map { questions.single().copy(id = "timer-test-$it") }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(tenQuestions, Translation.BSB, elapsedRealtimeMillis = { now },
                    onCountdownCue = { cues += it })
            }
        }
        compose.onNodeWithText(context.getString(R.string.quiz_timer_description, 30)).assertExists()
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertIsDisplayed().assertTextEquals("00:30")
            .assertContentDescriptionEquals(context.getString(R.string.quiz_time_remaining, "00:30"))
        val firstQuestionSelection = requireNotNull(visible("quiz_option_0").fetchSemanticsNode()
            .config[SemanticsActions.OnClick].action)
        compose.runOnIdle { now = 39_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        node("quiz_timer_display").assertTextEquals("00:01")
        assertEquals(listOf(1), cues)
        node("quiz_time_up").assertDoesNotExist()
        for (completedQuestions in 1..10) {
            compose.runOnIdle { now = 10_000L + completedQuestions * 30_000L }
            compose.mainClock.advanceTimeBy(1_100L)
            assertEquals(listOf(1) + List(completedQuestions) { 0 }, cues)
            if (completedQuestions < 10) {
                compose.onNodeWithText(context.getString(R.string.quiz_progress, completedQuestions + 1, 10))
                    .assertIsDisplayed()
                node("quiz_timer_display").assertTextEquals("00:30")
                if (completedQuestions == 1) {
                    // A queued option click from the expired question cannot answer the next one.
                    compose.runOnIdle { assertTrue(firstQuestionSelection()) }
                }
                node("quiz_action").assertIsNotEnabled()
                node("quiz_score").assertDoesNotExist()
                // A stale ticker sample must neither expire nor beep on the new question.
                compose.mainClock.advanceTimeBy(2_100L)
                node("quiz_timer_display").assertTextEquals("00:30")
                assertEquals(listOf(1) + List(completedQuestions) { 0 }, cues)
            }
        }
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("0 of 10 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 05:00")
        node("quiz_timer_display").assertDoesNotExist()
        node("quiz_action").assertDoesNotExist()
    }

    @Test fun checkingAnswerPausesCountdownAndNextQuestionStartsFreshThirtySeconds() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        val twoQuestions = (1..2).map { questions.single().copy(id = "feedback-timer-$it") }
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(twoQuestions, Translation.BSB, elapsedRealtimeMillis = { now },
                    onCountdownCue = { cues += it })
            }
        }
        visible("quiz_start").performClick()
        compose.runOnIdle { now = 15_000L }
        visible("quiz_option_0").performClick()
        node("quiz_action").performClick()
        node("quiz_feedback").assertIsDisplayed()
        node("quiz_timer_display").assertTextEquals("00:25")
        compose.runOnIdle { now = 115_000L }
        compose.mainClock.advanceTimeBy(2_100L)
        restoration.emulateSavedInstanceStateRestore()
        node("quiz_feedback").assertIsDisplayed()
        node("quiz_timer_display").assertTextEquals("00:25")
        assertTrue(cues.isEmpty())
        node("quiz_action").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        node("quiz_action").assertIsNotEnabled()
        compose.runOnIdle { now = 140_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        node("quiz_timer_display").assertTextEquals("00:05")
        assertEquals(listOf(5), cues)
        visible("quiz_option_0").performClick()
        node("quiz_action").performClick()
        compose.runOnIdle { now = 240_000L }
        compose.mainClock.advanceTimeBy(2_100L)
        node("quiz_feedback").assertIsDisplayed()
        node("quiz_timer_display").assertTextEquals("00:05")
        assertEquals(listOf(5), cues)
        node("quiz_action").performClick()
        visible("quiz_score").assertTextEquals("2 of 2 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        node("quiz_time_up").assertDoesNotExist()
    }

    @Test fun resumingAfterALongAbsenceExpiresCurrentQuestionAndPreservesUnseenQuestions() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        val threeQuestions = (1..3).map { questions.single().copy(id = "background-timer-$it") }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var owner: TimerLifecycleOwner
        compose.runOnUiThread { owner = TimerLifecycleOwner() }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                HopeCardsTheme(ThemeName.CLASSIC) {
                    BibleQuizScreen(threeQuestions, Translation.BSB, elapsedRealtimeMillis = { now },
                        onCountdownCue = { cues += it })
                }
            }
        }
        visible("quiz_start").performClick()
        visible("quiz_option_0").performClick()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.runOnIdle { now = 310_000L }
        compose.mainClock.advanceTimeBy(2_100L)
        assertTrue(cues.isEmpty())
        node("quiz_score").assertDoesNotExist()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        compose.onNodeWithText(context.getString(R.string.quiz_progress, 2, 3)).assertIsDisplayed()
        node("quiz_timer_display").assertTextEquals("00:30")
        node("quiz_action").assertIsNotEnabled()
        assertEquals(listOf(0), cues)
        compose.mainClock.advanceTimeBy(2_100L)
        node("quiz_timer_display").assertTextEquals("00:30")
        assertEquals(listOf(0), cues)
        compose.runOnIdle { now = 340_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.onNodeWithText(context.getString(R.string.quiz_progress, 3, 3)).assertIsDisplayed()
        node("quiz_timer_display").assertTextEquals("00:30")
        assertEquals(listOf(0, 0), cues)
        compose.runOnIdle { now = 370_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("1 of 3 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 01:30")
        assertEquals(listOf(0, 0, 0), cues)
    }

    @Test fun unansweredTimeoutShowsReviewAndRestoresWithoutAutomaticallyLeavingResults() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        var completedExits = 0
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now },
                    onComplete = { _, proceed -> completedExits++; proceed() })
            }
        }
        visible("quiz_start").performClick()
        compose.runOnIdle { now = 40_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("0 of 1 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        node("quiz_timer_display").assertDoesNotExist()
        node("quiz_action").assertDoesNotExist()
        assertEquals(0, completedExits)
        capture("timer-time-up-results.png")
        compose.runOnIdle { now = 170_000L }
        restoration.emulateSavedInstanceStateRestore()
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("0 of 1 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        visible("quiz_review").performClick()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val unanswered = context.getString(R.string.quiz_your_answer, context.getString(R.string.quiz_unanswered))
        node("bible_quiz").performScrollToNode(hasText(unanswered))
        compose.onNodeWithText(unanswered).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.quiz_correct_answer, "God")).assertExists()
        capture("timer-time-up-review.png")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText(unanswered).assertExists()
        visible("quiz_back_results").performClick()
        visible("quiz_score").assertTextEquals("0 of 1 correct")
        assertEquals(0, completedExits)
    }

    @Test fun selectedAnswerIsCountedWhenTheSubmissionTapArrivesAtTheDeadline() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now })
            }
        }
        visible("quiz_start").performClick()
        visible("quiz_option_0").performClick()
        // The click handler must check the clock even before the next visual timer tick.
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { now = 40_000L }
        node("quiz_action").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.autoAdvance = true
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("1 of 1 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        visible("quiz_review").performClick()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.quiz_your_answer, "God")).assertExists()
    }

    @Test fun selectingAnAnswerAfterTheDeadlineDoesNotReplaceAnUnansweredResult() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now })
            }
        }
        visible("quiz_start").performClick()
        val lateSelection = requireNotNull(visible("quiz_option_0").fetchSemanticsNode()
            .config[SemanticsActions.OnClick].action)
        // Capture the displayed option before the deadline. Deliver its queued click in
        // the same UI task as the clock jump so test synchronization cannot expire first.
        compose.runOnIdle {
            now = 40_000L
            assertTrue(lateSelection())
        }
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("0 of 1 correct")
    }

    @Test fun resumingAfterTheDeadlineAutomaticallyFinishesAndClampsTimeTaken() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        lateinit var owner: TimerLifecycleOwner
        compose.runOnUiThread { owner = TimerLifecycleOwner() }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                HopeCardsTheme(ThemeName.CLASSIC) {
                    BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now })
                }
            }
        }
        visible("quiz_start").performClick()
        visible("quiz_option_0").performClick()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.runOnIdle { now = 130_000L }
        compose.mainClock.advanceTimeBy(2_000L)
        node("quiz_time_up").assertDoesNotExist()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_score").assertTextEquals("1 of 1 correct")
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        capture("timer-background-deadline-results.png")
    }

    @Test fun countdownCuesOccurOncePerFinalSecondSurviveRestorationAndResetForTheNextRound() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now },
                    onCountdownCue = { cues += it })
            }
        }
        visible("quiz_start").performClick()
        compose.runOnIdle { now = 35_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        assertEquals(listOf(5), cues)
        // Repeated ticker reads within the same monotonic second must not replay a beep.
        compose.mainClock.advanceTimeBy(2_100L)
        assertEquals(listOf(5), cues)
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(listOf(5), cues)
        for (secondsRemaining in 4 downTo 0) {
            compose.runOnIdle { now = 40_000L - secondsRemaining * 1_000L }
            compose.mainClock.advanceTimeBy(1_100L)
            assertEquals((5 downTo secondsRemaining).toList(), cues)
        }
        visible("quiz_time_up").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle { now = 45_000L }
        compose.mainClock.advanceTimeBy(2_100L)
        assertEquals(listOf(5, 4, 3, 2, 1, 0), cues)
        visible("quiz_restart").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        compose.runOnIdle { now = 70_000L }
        compose.mainClock.advanceTimeBy(1_100L)
        node("quiz_timer_display").assertTextEquals("00:05")
        assertEquals(listOf(5, 4, 3, 2, 1, 0, 5), cues)
    }

    @Test fun disablingSoundMutesFinalCountdownAndExpiryCues() {
        preferences.edit().putBoolean("timer", true).putBoolean("sound", false).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now },
                    onCountdownCue = { cues += it })
            }
        }
        visible("quiz_sound").assertIsOff()
        visible("quiz_start").performClick()
        for (secondsRemaining in 5 downTo 0) {
            compose.runOnIdle { now = 40_000L - secondsRemaining * 1_000L }
            compose.mainClock.advanceTimeBy(1_100L)
        }
        visible("quiz_time_up").assertIsDisplayed()
        assertTrue(cues.isEmpty())
    }

    @Test fun countdownCuesStaySilentInBackgroundAndResumeWithoutReplayingMissedSeconds() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        val cues = mutableListOf<Int>()
        lateinit var owner: TimerLifecycleOwner
        compose.runOnUiThread { owner = TimerLifecycleOwner() }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                HopeCardsTheme(ThemeName.CLASSIC) {
                    BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = { now },
                        onCountdownCue = { cues += it })
                }
            }
        }
        visible("quiz_start").performClick()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.runOnIdle { now = 38_000L }
        compose.mainClock.advanceTimeBy(3_100L)
        assertTrue(cues.isEmpty())
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        node("quiz_timer_display").assertTextEquals("00:02")
        assertEquals(listOf(2), cues)
        compose.mainClock.advanceTimeBy(1_100L)
        assertEquals(listOf(2), cues)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.runOnIdle { now = 90_000L }
        compose.mainClock.advanceTimeBy(2_100L)
        assertEquals(listOf(2), cues)
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        visible("quiz_time_up").assertIsDisplayed()
        assertEquals(listOf(2, 0), cues)
    }

    @Test fun delayedFinalCountdownEffectEmitsOnlyExpiryCueWhenTheDeadlineHasAlreadyPassed() {
        preferences.edit().putBoolean("timer", true).commit()
        var now = 10_000L
        var jumpOnNextRead = false
        var finalSecondSampleRead = false
        val cues = mutableListOf<Int>()
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                BibleQuizScreen(questions, Translation.BSB, elapsedRealtimeMillis = {
                    if (jumpOnNextRead) {
                        jumpOnNextRead = false
                        finalSecondSampleRead = true
                        // The ticker samples five seconds remaining. By the time its effect
                        // executes, the monotonic clock has passed the question's deadline.
                        now = 41_000L
                        35_000L
                    } else now
                }, onCountdownCue = { cues += it })
            }
        }
        visible("quiz_start").performClick()
        node("quiz_timer_display").assertTextEquals("00:30")
        compose.waitForIdle()
        compose.runOnIdle { jumpOnNextRead = true }
        compose.mainClock.advanceTimeBy(1_100L)
        compose.waitForIdle()
        assertTrue("The ticker must exercise the final-second sample before clock expiry", finalSecondSampleRead)
        visible("quiz_time_up").assertIsDisplayed()
        visible("quiz_elapsed_result").assertTextEquals("Time taken: 00:30")
        assertEquals(listOf(0), cues)
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "quiz-timer").apply { mkdirs() }
        File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private class TimerLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle get() = registry
    }
}
