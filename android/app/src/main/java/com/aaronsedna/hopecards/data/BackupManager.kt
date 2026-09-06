package com.aaronsedna.hopecards.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.BackupInfo
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.time.Instant

class BackupManager(
    private val context: Context,
    private val repository: AppRepository,
    private val verseRepository: VerseRepository,
) {
    suspend fun createBackup(): Pair<Uri, BackupInfo> {
        val json = createPayload()
        val directory = File(context.filesDir, "backups").apply { mkdirs() }
        val createdAt = json.getString("createdAt")
        val name = "hope-cards-backup-${createdAt.replace(':', '-').replace('.', '-')}.json"
        val file = File(directory, name)
        file.writeText(json.toString(2))
        directory.listFiles()
            ?.filter { it.name.startsWith("hope-cards-backup-") }
            ?.sortedByDescending(File::getName)
            ?.drop(10)
            ?.forEach(File::delete)

        val info = BackupInfo(
            createdAt = createdAt,
            fileName = name,
            version = 1,
            favoriteCount = repository.currentFavorites().size,
            journalEntryCount = repository.currentJournalEntries().size,
        )
        repository.setBackupInfo(info)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return uri to info
    }

    suspend fun restore(uri: Uri) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (total <= MAX_BACKUP_BYTES) {
                val count = input.read(buffer, 0, minOf(buffer.size, MAX_BACKUP_BYTES + 1 - total))
                if (count < 0) break
                output.write(buffer, 0, count)
                total += count
            }
            output.toByteArray()
        } ?: error("Unable to read the selected backup.")
        require(bytes.size <= MAX_BACKUP_BYTES) { "Backup file is too large." }
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        require(root.optInt("version") == 1) { "Unsupported backup version." }

        val validIds = verseRepository.verses(Translation.BSB).mapTo(hashSetOf()) { it.id }
        val favoritesJson = root.optJSONArray("favorites") ?: JSONArray()
        require(favoritesJson.length() <= 5_000) { "Too many favorites in backup." }
        val favorites = buildSet {
            repeat(favoritesJson.length()) {
                val id = favoritesJson.getString(it)
                require(id in validIds) { "Backup contains an unknown verse." }
                add(id)
            }
        }

        val journal = decodeJournal(root.optJSONArray("journalEntries") ?: JSONArray(), validIds)
        val settingsJson = root.getJSONObject("settings")
        val settings = AppSettings(
            showDrawButton = settingsJson.optBoolean("showDrawButton", true),
            enableHaptics = settingsJson.optBoolean("enableHaptics", true),
            dailyHopeReminderEnabled = settingsJson.optBoolean("dailyHopeReminderEnabled", false),
            dailyHopeMusicEnabled = settingsJson.optBoolean("dailyHopeMusicEnabled", true),
            dailyHopeReminderHour = settingsJson.optInt("dailyHopeReminderHour", 8).coerceIn(0, 23),
            dailyHopeReminderMinute = settingsJson.optInt("dailyHopeReminderMinute", 0).coerceIn(0, 59),
            themeName = ThemeName.fromId(settingsJson.optString("themeName", "classic")),
            preferredTranslation = Translation.fromId(settingsJson.optString("preferredTranslation", "bsb")),
        )

        repository.replaceBackupData(settings, favorites, journal)
    }

    private suspend fun createPayload(): JSONObject {
        val settings = repository.currentSettings()
        val favorites = repository.currentFavorites()
        val journal = repository.currentJournalEntries()
        return JSONObject()
            .put("version", 1)
            .put("createdAt", Instant.now().toString())
            .put("favorites", JSONArray(favorites.toList()))
            .put("journalEntries", encodeJournal(journal))
            .put(
                "settings",
                JSONObject()
                    .put("showDrawButton", settings.showDrawButton)
                    .put("enableHaptics", settings.enableHaptics)
                    .put("dailyHopeReminderEnabled", settings.dailyHopeReminderEnabled)
                    .put("dailyHopeMusicEnabled", settings.dailyHopeMusicEnabled)
                    .put("dailyHopeReminderHour", settings.dailyHopeReminderHour)
                    .put("dailyHopeReminderMinute", settings.dailyHopeReminderMinute)
                    .put("themeName", settings.themeName.id)
                    .put("preferredTranslation", settings.preferredTranslation.id),
            )
    }

    private fun encodeJournal(entries: List<JournalEntry>) = JSONArray().apply {
        entries.forEach { entry ->
            put(
                JSONObject()
                    .put("id", entry.id)
                    .put("date", entry.date)
                    .put("verseId", entry.verseId)
                    .put("reference", entry.reference)
                    .put("prompt", entry.prompt)
                    .put("note", entry.note)
                    .put("updatedAt", entry.updatedAt),
            )
        }
    }

    private fun decodeJournal(array: JSONArray, validIds: Set<String>): List<JournalEntry> {
        require(array.length() <= 5_000) { "Too many journal entries in backup." }
        return buildList {
            repeat(array.length()) { index ->
                val value = array.getJSONObject(index)
                val note = value.getString("note")
                val verseId = value.getString("verseId")
                val id = value.getString("id")
                val date = value.getString("date")
                val reference = value.getString("reference")
                val prompt = value.getString("prompt")
                val updatedAt = value.getString("updatedAt")
                require(
                    note.length <= 20_000 &&
                        verseId in validIds &&
                        id.length in 1..200 &&
                        date.length in 1..40 &&
                        reference.length in 1..200 &&
                        prompt.length <= 2_000 &&
                        updatedAt.length in 1..80,
                ) { "Invalid journal entry." }
                add(
                    JournalEntry(
                        id = id,
                        date = date,
                        verseId = verseId,
                        reference = reference,
                        prompt = prompt,
                        note = note,
                        updatedAt = updatedAt,
                    ),
                )
            }
        }.distinctBy { it.id }
    }

    companion object {
        private const val MAX_BACKUP_BYTES = 5 * 1024 * 1024
    }
}
