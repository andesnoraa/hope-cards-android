package com.aaronsedna.hopecards.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats Daily Hope's visible date in the language of the selected Bible edition. */
object BibleDisplayDateFormatter {
    fun dailyHopeTitle(translation: Translation): String = when (translation) {
        Translation.LUT1912 -> "Gottes Wort"
        Translation.LSG1910 -> "Parole de Dieu"
        Translation.RIV1927 -> "Parola di Dio"
        Translation.RV1909 -> "Palabra de Dios"
        Translation.ADB1905 -> "Salita ng Diyos"
        Translation.MAL1910 -> "ദൈവവചനം"
        else -> "God’s Word"
    }

    fun format(date: LocalDate, translation: Translation): String {
        val (localeTag, pattern) = when (translation) {
            Translation.LUT1912 -> "de-DE" to "EEEE • d. MMMM"
            Translation.LSG1910 -> "fr-FR" to "EEEE • d MMMM"
            Translation.RIV1927 -> "it-IT" to "EEEE • d MMMM"
            Translation.RV1909 -> "es-419" to "EEEE • d 'de' MMMM"
            Translation.ADB1905 -> "fil-PH" to "EEEE • MMMM d"
            Translation.MAL1910 -> "ml-IN" to "MMMM d • EEEE"
            else -> "en-GB" to "EEEE • d MMMM"
        }
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag(localeTag)))
    }
}
