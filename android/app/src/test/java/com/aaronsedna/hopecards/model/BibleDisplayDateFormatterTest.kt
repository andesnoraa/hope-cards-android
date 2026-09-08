package com.aaronsedna.hopecards.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BibleDisplayDateFormatterTest {
    @Test
    fun `daily hope title follows selected Bible language`() {
        val expected = mapOf(
            Translation.BSB to "God’s Word",
            Translation.BBE to "God’s Word",
            Translation.KJV to "God’s Word",
            Translation.WEB to "God’s Word",
            Translation.LUT1912 to "Gottes Wort",
            Translation.LSG1910 to "Parole de Dieu",
            Translation.RIV1927 to "Parola di Dio",
            Translation.RV1909 to "Palabra de Dios",
            Translation.ADB1905 to "Salita ng Diyos",
            Translation.MAL1910 to "ദൈവവചനം",
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
