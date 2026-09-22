package com.aaronsedna.hopecards.notifications

import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderPolicyTest {
    @Test fun sixAmUsesLocalTimeAndRollsToTomorrowAfterDelivery() {
        val now = ZonedDateTime.parse("2026-09-22T05:59:00+05:30[Asia/Kolkata]")
        val first = nextReminderTime(now, 6, 0)
        assertEquals("2026-09-22T06:00+05:30[Asia/Kolkata]", first.toString())
        assertEquals(first.plusDays(1), nextReminderTime(first, 6, 0))
    }

    @Test fun customizedTimeSurvivesDaylightSavingGapOnPreviousDay() {
        // March 8 has no 02:30; the following day's reminder must return to 02:30, not 03:30.
        val now = ZonedDateTime.parse("2026-03-08T04:00:00-04:00[America/New_York]")
        assertEquals("2026-03-09T02:30-04:00[America/New_York]", nextReminderTime(now, 2, 30).toString())
    }

    @Test fun migrationEnablesBannersButPreservesUserRestrictions() {
        assertEquals(4, verseChannelImportance(null, false))
        assertEquals(4, verseChannelImportance(3, false))
        assertEquals(3, verseChannelImportance(3, true))
        assertEquals(0, verseChannelImportance(0, false))
        assertEquals(2, verseChannelImportance(2, false))
        assertEquals(1, verseChannelImportance(1, true))
    }
}
