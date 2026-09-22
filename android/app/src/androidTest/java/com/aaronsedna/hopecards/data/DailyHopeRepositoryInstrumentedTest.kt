package com.aaronsedna.hopecards.data

import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.DailyHopeRecord
import com.aaronsedna.hopecards.model.Translation
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DailyHopeRepositoryInstrumentedTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun missingPreferencesUseSixAmAndSavedOptOutAndTimeArePreserved() {
        val repository = AppRepository(context)
        for (json in listOf(null, "{}", "{\"themeName\":\"midnight\"}")) {
            val defaults = repository.decodeSettings(json)
            assertTrue(defaults.dailyHopeReminderEnabled)
            assertEquals(6, defaults.dailyHopeReminderHour)
            assertEquals(0, defaults.dailyHopeReminderMinute)
        }
        val saved = repository.decodeSettings("""{"dailyHopeReminderEnabled":false,"dailyHopeReminderHour":21,"dailyHopeReminderMinute":45,"dailyHopeMusicEnabled":false}""")
        assertFalse(saved.dailyHopeReminderEnabled)
        assertFalse(saved.dailyHopeMusicEnabled)
        assertEquals(21, saved.dailyHopeReminderHour)
        assertEquals(45, saved.dailyHopeReminderMinute)
    }

    @Test fun concurrentReceiverAndScreenKeepOneVerseAndTranslationsKeepReference() = runBlocking {
        val repository = AppRepository(context)
        repository.initialize()
        val verses = VerseRepository(context)
        val original = repository.getDailyHopeRecord()
        val date = LocalDate.now()
        try {
            repository.setDailyHopeRecord(DailyHopeRecord(date.minusDays(1).toString(), "philippians-4-13", "bsb"))
            val results = (1..12).map {
                async(Dispatchers.IO) { AppRepository(context).dailyHopeVerse(VerseRepository(context), Translation.BSB, date) }
            }.awaitAll()
            assertEquals(1, results.map { it.id }.distinct().size)
            assertNotEquals("philippians-4-13", results.first().id)
            Translation.entries.forEach { edition ->
                val localized = repository.dailyHopeVerse(verses, edition, date)
                assertEquals(results.first().id, localized.id)
                assertEquals(edition, localized.edition)
            }
            val tomorrow = repository.dailyHopeVerse(verses, Translation.BSB, date.plusDays(1))
            assertNotEquals(results.first().id, tomorrow.id)
            assertEquals(date.plusDays(1).toString(), repository.getDailyHopeRecord()?.date)
        } finally {
            if (original != null) repository.setDailyHopeRecord(original)
        }
    }
}
