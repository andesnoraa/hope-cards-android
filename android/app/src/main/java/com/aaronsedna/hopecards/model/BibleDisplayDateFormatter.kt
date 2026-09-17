package com.aaronsedna.hopecards.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats Daily Hope's UI date consistently, with the Malayalam heading kept natural. */
object BibleDisplayDateFormatter {
    fun dailyHopeTitle(translation: Translation): String =
        if (translation == Translation.MAL1910) "ദൈവ വചനം" else "Today’s Hope"

    fun format(date: LocalDate, translation: Translation): String {
        return date.format(DateTimeFormatter.ofPattern("EEEE • d MMMM", Locale.US))
    }
}
