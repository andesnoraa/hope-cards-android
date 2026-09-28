package com.aaronsedna.hopecards.model

import java.util.Locale

/** Monotonic elapsed time, independent of UI ticks and changes to the wall clock. */
data class QuizStopwatch(
    val enabled: Boolean = false,
    val running: Boolean = false,
    val anchorMillis: Long = 0L,
    val elapsedBeforeAnchorMillis: Long = 0L,
    val bootCount: Int = 0,
) {
    fun elapsedMillis(nowMillis: Long, currentBootCount: Int = bootCount): Long {
        if (!enabled) return 0L
        val sinceAnchor = if (running && bootCount == currentBootCount) {
            (nowMillis - anchorMillis).coerceAtLeast(0L)
        } else 0L
        return elapsedBeforeAnchorMillis + sinceAnchor
    }

    /** Checkpoint when saving state, so a reboot never invents elapsed time. */
    fun checkpoint(nowMillis: Long, currentBootCount: Int = bootCount): QuizStopwatch = copy(
        anchorMillis = nowMillis,
        elapsedBeforeAnchorMillis = elapsedMillis(nowMillis, currentBootCount),
        bootCount = currentBootCount,
    )

    fun finish(nowMillis: Long, currentBootCount: Int = bootCount): QuizStopwatch =
        checkpoint(nowMillis, currentBootCount).copy(running = false)

    fun restore(nowMillis: Long, currentBootCount: Int = bootCount): QuizStopwatch =
        if (currentBootCount != bootCount || nowMillis < anchorMillis) {
            copy(anchorMillis = nowMillis, bootCount = currentBootCount)
        } else this

    companion object {
        fun start(enabled: Boolean, nowMillis: Long, bootCount: Int = 0): QuizStopwatch =
            QuizStopwatch(enabled = enabled, running = enabled, anchorMillis = nowMillis, bootCount = bootCount)
    }
}

fun formatQuizElapsedTime(elapsedMillis: Long): String {
    val seconds = elapsedMillis.coerceAtLeast(0L) / 1_000L
    return if (seconds < 3_600L) String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L)
    else String.format(Locale.ROOT, "%d:%02d:%02d", seconds / 3_600L, seconds / 60L % 60L, seconds % 60L)
}

/** Each question gets a fresh thirty-second allowance when its timer is enabled. */
const val QUIZ_COUNTDOWN_DURATION_MILLIS = 30_000L

/** Round up, so a newly started minute remains 01:00 until a full second has passed. */
fun formatQuizCountdownTime(remainingMillis: Long): String =
    formatQuizElapsedTime(((remainingMillis.coerceAtLeast(0L) + 999L) / 1_000L) * 1_000L)
