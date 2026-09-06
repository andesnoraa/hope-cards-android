package com.aaronsedna.hopecards.data

import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupManagerInstrumentedTest {
    @Test
    fun backupRoundTripRestoresSettingsFavoritesAndJournal() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        val verses = VerseRepository(context)
        val manager = BackupManager(context, repository, verses)
        repository.initialize()

        val originalSettings = repository.currentSettings()
        val originalFavorites = repository.currentFavorites()
        val originalJournal = repository.currentJournalEntries()
        val verse = verses.verses(Translation.BSB).first()
        val expectedSettings = AppSettings(
            showDrawButton = false,
            enableHaptics = false,
            dailyHopeReminderEnabled = false,
            dailyHopeMusicEnabled = false,
            dailyHopeReminderHour = 17,
            dailyHopeReminderMinute = 42,
            themeName = ThemeName.STILL_WATER,
            preferredTranslation = Translation.WEB,
        )
        val expectedFavorites = setOf(verse.id)
        val expectedJournal = listOf(
            JournalEntry(
                id = "backup-test:${verse.id}",
                date = "2026-09-06",
                verseId = verse.id,
                reference = verse.reference,
                prompt = "What gives you hope?",
                note = "A verified round-trip reflection.",
                updatedAt = "2026-09-06T08:00:00Z",
            ),
        )

        try {
            repository.replaceBackupData(expectedSettings, expectedFavorites, expectedJournal)
            val (uri, info) = manager.createBackup()
            assertEquals(1, info.favoriteCount)
            assertEquals(1, info.journalEntryCount)

            repository.replaceBackupData(AppSettings(), emptySet(), emptyList())
            manager.restore(uri)

            assertEquals(expectedSettings, repository.currentSettings())
            assertEquals(expectedFavorites, repository.currentFavorites())
            assertEquals(expectedJournal, repository.currentJournalEntries())
        } finally {
            repository.replaceBackupData(originalSettings, originalFavorites, originalJournal)
        }
    }

    @Test
    fun invalidBackupIsRejectedWithoutChangingExistingData() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = AppRepository(context)
        val verses = VerseRepository(context)
        val manager = BackupManager(context, repository, verses)
        repository.initialize()
        val beforeSettings = repository.currentSettings()
        val beforeFavorites = repository.currentFavorites()
        val beforeJournal = repository.currentJournalEntries()

        val directory = File(context.filesDir, "backups").apply { mkdirs() }
        val invalid = File(directory, "invalid-backup.json")
        invalid.writeText(
            JSONObject()
                .put("version", 1)
                .put("favorites", JSONArray().put("not-a-real-verse"))
                .put("journalEntries", JSONArray())
                .put("settings", JSONObject())
                .toString(),
        )
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", invalid)
        val failure = runCatching { manager.restore(uri) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertEquals(beforeSettings, repository.currentSettings())
        assertEquals(beforeFavorites, repository.currentFavorites())
        assertEquals(beforeJournal, repository.currentJournalEntries())
        invalid.delete()
        Unit
    }
}
