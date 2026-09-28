package com.aaronsedna.hopecards.ui

import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.ui.screens.formatCertificateDate
import java.time.LocalDate
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class CertificateDateTest {
    @Test fun selectedCalendarDayDoesNotShiftInDeviceTimezones() {
        val originalZone = TimeZone.getDefault()
        try {
            val date = LocalDate.of(2026, 12, 31).toEpochDay() * 86_400_000L
            listOf("America/Los_Angeles", "Pacific/Kiritimati", "Asia/Kolkata").forEach { zone ->
                TimeZone.setDefault(TimeZone.getTimeZone(zone))
                assertEquals("December 31, 2026", formatCertificateDate(date, QuizLanguage.ENGLISH))
                assertEquals("31 décembre 2026", formatCertificateDate(date, QuizLanguage.FRENCH))
            }
        } finally {
            TimeZone.setDefault(originalZone)
        }
    }
}
