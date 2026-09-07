package com.aaronsedna.hopecards.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.JournalEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppRepositoryInstrumentedTest {
    @Test
    fun individualAndBulkRemovalOnlyDeleteSelectedRecords() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val originalFavorites = repository.currentFavorites()
        val originalJournal = repository.currentJournalEntries()
        val journal = listOf(
            journalEntry("journal-one", "verse-one"),
            journalEntry("journal-two", "verse-two"),
            journalEntry("journal-three", "verse-three"),
        )

        try {
            repository.replaceFavorites(setOf("verse-one", "verse-two", "verse-three"))
            repository.replaceJournalEntries(journal)

            repository.removeFavorites(setOf("verse-two"))
            assertEquals(setOf("verse-one", "verse-three"), repository.currentFavorites())
            repository.removeFavorites(setOf("verse-one", "verse-three"))
            assertEquals(emptySet<String>(), repository.currentFavorites())

            repository.deleteJournalEntries(setOf("journal-two"))
            assertEquals(
                listOf("journal-one", "journal-three"),
                repository.currentJournalEntries().map(JournalEntry::id),
            )
            repository.deleteJournalEntries(setOf("journal-one", "journal-three"))
            assertEquals(emptyList<JournalEntry>(), repository.currentJournalEntries())
        } finally {
            repository.replaceFavorites(originalFavorites)
            repository.replaceJournalEntries(originalJournal)
        }
    }

    private fun journalEntry(id: String, verseId: String) = JournalEntry(
        id = id,
        date = "2026-09-07",
        verseId = verseId,
        reference = "Psalm 1:1",
        prompt = "Keep these words close today.",
        note = "A journal entry used to verify deletion.",
        updatedAt = "2026-09-07T08:00:00Z",
    )
}
