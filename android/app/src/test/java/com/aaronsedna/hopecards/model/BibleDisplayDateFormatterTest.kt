package com.aaronsedna.hopecards.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BibleDisplayDateFormatterTest {
    @Test
    fun `daily hope title stays in English for every Bible edition`() {
        val expected = mapOf(
            Translation.BSB to "Today’s Hope",
            Translation.BBE to "Today’s Hope",
            Translation.KJV to "Today’s Hope",
            Translation.WEB to "Today’s Hope",
            Translation.LUT1912 to "Today’s Hope",
            Translation.LSG1910 to "Today’s Hope",
            Translation.RIV1927 to "Today’s Hope",
            Translation.RV1909 to "Today’s Hope",
            Translation.ADB1905 to "Today’s Hope",
            Translation.MAL1910 to "ദൈവ വചനം",
        )

        expected.forEach { (translation, title) ->
            assertEquals(translation.id, title, BibleDisplayDateFormatter.dailyHopeTitle(translation))
        }
    }

    private val date = LocalDate.of(2026, 9, 8)

    @Test
    fun formatsDailyHopeDateNaturallyForEverySupportedLanguage() {
        val expected = mapOf(
            Translation.BSB to "Tuesday • 8 September",
            Translation.BBE to "Tuesday • 8 September",
            Translation.KJV to "Tuesday • 8 September",
            Translation.WEB to "Tuesday • 8 September",
            Translation.LUT1912 to "Dienstag • 8. September",
            Translation.LSG1910 to "mardi • 8 septembre",
            Translation.RIV1927 to "martedì • 8 settembre",
            Translation.RV1909 to "martes • 8 de septiembre",
            Translation.ADB1905 to "Martes • Setyembre 8",
            Translation.MAL1910 to "സെപ്റ്റംബർ 8 • ചൊവ്വാഴ്ച",
        )

        expected.forEach { (translation, formatted) ->
            assertEquals(translation.id, formatted, BibleDisplayDateFormatter.format(date, translation))
        }
    }
}
