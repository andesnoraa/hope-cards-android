package com.aaronsedna.hopecards.notifications

import java.time.ZonedDateTime

data class DailyHopeRequest(val sequence: Int, val verseId: String?)

internal fun nextReminderTime(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
    require(hour in 0..23 && minute in 0..59)
    var next = now.toLocalDate().atTime(hour, minute).atZone(now.zone)
    if (!next.isAfter(now)) next = now.toLocalDate().plusDays(1).atTime(hour, minute).atZone(now.zone)
    return next
}

/** Preserve blocked/quiet channels and any explicitly chosen importance during migration. */
internal fun verseChannelImportance(legacyImportance: Int?, userChoseImportance: Boolean): Int =
    when {
        legacyImportance == null -> 4 // IMPORTANCE_HIGH: heads-up banner
        userChoseImportance || legacyImportance < 3 -> legacyImportance
        else -> 4
    }
