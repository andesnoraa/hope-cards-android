package com.aaronsedna.hopecards.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class QuizStopwatchTest {
    @Test fun countdownRoundsRemainingSecondsUp() {
        assertEquals("00:30", formatQuizCountdownTime(QUIZ_COUNTDOWN_DURATION_MILLIS))
        assertEquals("01:00", formatQuizCountdownTime(59_999L))
        assertEquals("00:59", formatQuizCountdownTime(59_000L))
        assertEquals("00:01", formatQuizCountdownTime(1L))
        assertEquals("00:00", formatQuizCountdownTime(0L))
        assertEquals("00:00", formatQuizCountdownTime(-10L))
    }

    @Test fun disabledRoundsNeverAccumulateTime() {
        val timer = QuizStopwatch.start(enabled = false, nowMillis = 10_000L)
        assertFalse(timer.enabled)
        assertFalse(timer.running)
        assertEquals(0L, timer.elapsedMillis(90_000L))
        assertEquals(0L, timer.finish(100_000L).elapsedMillis(200_000L))
    }

    @Test fun elapsedTimeDoesNotDependOnReceivingTicks() {
        val timer = QuizStopwatch.start(enabled = true, nowMillis = 10_000L)
        // No work is needed while the screen is stopped or the app is backgrounded.
        assertEquals(125_000L, timer.elapsedMillis(135_000L))
    }

    @Test fun completionFreezesAndIsIdempotent() {
        val timer = QuizStopwatch.start(true, 10_000L).finish(25_500L)
        assertFalse(timer.running)
        assertEquals(15_500L, timer.elapsedMillis(100_000L))
        assertEquals(15_500L, timer.finish(150_000L).elapsedMillis(200_000L))
    }

    @Test fun aNewRoundAlwaysStartsFromZero() {
        val completed = QuizStopwatch.start(true, 10_000L).finish(90_000L)
        val nextRound = QuizStopwatch.start(completed.enabled, 100_000L)
        assertEquals(0L, nextRound.elapsedMillis(100_000L))
        assertEquals(3_000L, nextRound.elapsedMillis(103_000L))
    }

    @Test fun aSavedRunningRoundIncludesTimeDuringRestoration() {
        val saved = QuizStopwatch.start(true, 10_000L, bootCount = 7).checkpoint(20_000L)
        val restored = saved.copy().restore(25_000L, currentBootCount = 7)
        assertEquals(15_000L, restored.elapsedMillis(25_000L))
        assertEquals(18_000L, restored.elapsedMillis(28_000L))
    }

    @Test fun aSavedCompletedRoundNeverResumes() {
        val saved = QuizStopwatch.start(true, 10_000L).finish(20_000L).checkpoint(40_000L)
        val restored = saved.copy().restore(100_000L)
        assertFalse(restored.running)
        assertEquals(10_000L, restored.elapsedMillis(200_000L))
    }

    @Test fun rebootPreservesCheckpointWithoutComparingDifferentClockEpochs() {
        val saved = QuizStopwatch.start(true, 10_000L, bootCount = 7).checkpoint(20_000L)
        // New boot uptime may be either lower OR higher than the old saved uptime.
        listOf(2_000L, 50_000L).forEach { uptime ->
            val restored = saved.restore(uptime, currentBootCount = 8)
            assertEquals(10_000L, restored.elapsedMillis(uptime))
            assertEquals(13_000L, restored.elapsedMillis(uptime + 3_000L))
        }
    }

    @Test fun monotonicClockRollbackPreservesTheLastCheckpoint() {
        val saved = QuizStopwatch.start(true, 10_000L).checkpoint(20_000L)
        val restored = saved.restore(2_000L)
        assertEquals(10_000L, restored.elapsedMillis(2_000L))
        assertEquals(11_000L, restored.elapsedMillis(3_000L))
    }

    @Test fun displayHandlesMinutesHoursAndInvalidNegativeInput() {
        assertEquals("00:00", formatQuizElapsedTime(-1L))
        assertEquals("00:59", formatQuizElapsedTime(59_999L))
        assertEquals("01:00", formatQuizElapsedTime(60_000L))
        assertEquals("59:59", formatQuizElapsedTime(3_599_000L))
        assertEquals("1:00:00", formatQuizElapsedTime(3_600_000L))
        assertEquals("12:04:05", formatQuizElapsedTime(43_445_000L))
    }
}
