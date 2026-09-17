package com.aaronsedna.hopecards.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
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
        val name = backupName(createdAt)
        val file = File(directory, name)
        file.writeText(json.toString(2))
        directory.listFiles()
            ?.filter { it.name.startsWith("hope-cards-backup-") }
            ?.sortedByDescending(File::getName)
            ?.drop(10)
            ?.forEach(File::delete)

        val info = createBackupInfo(createdAt, name)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return uri to info
    }

    suspend fun createBackupIn(treeUri: Uri): Pair<Uri, BackupInfo> {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?.takeIf { it.isDirectory && it.canWrite() }
            ?: throw BackupLocationUnavailableException()
        val directory = if (root.name == BACKUP_DIRECTORY_NAME) {
            root
        } else {
            root.findFile(BACKUP_DIRECTORY_NAME)?.takeIf { it.isDirectory }
                ?: root.createDirectory(BACKUP_DIRECTORY_NAME)
                ?: throw BackupLocationUnavailableException()
        }
        check(directory.canWrite()) { BACKUP_LOCATION_HELP }

        val json = createPayload()
        val createdAt = json.getString("createdAt")
        val name = backupName(createdAt)
        val document = directory.createFile(BACKUP_MIME_TYPE, name)
            ?: throw BackupLocationUnavailableException()
        runCatching {
            context.contentResolver.openOutputStream(document.uri, "w")?.bufferedWriter()?.use {
                it.write(json.toString(2))
            } ?: throw BackupLocationUnavailableException()
        }.getOrElse {
            runCatching { document.delete() }
            if (it is BackupLocationUnavailableException) throw it
            throw BackupLocationUnavailableException(it)
        }

        runCatching {
            directory.listFiles()
                .filter { it.isFile && it.name?.startsWith(BACKUP_FILE_PREFIX) == true }
                .sortedByDescending { it.name }
                .drop(MAX_STORED_BACKUPS)
                .forEach(DocumentFile::delete)
        }

        repository.setBackupLocation(treeUri, directory.uri)
        val info = createBackupInfo(createdAt, document.name ?: name)
        return document.uri to info
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun createAutomaticBackup(): Pair<Uri, BackupInfo> {
        check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "A backup folder must be selected on this Android version."
        }
        val json = createPayload()
        val createdAt = json.getString("createdAt")
        val name = backupName(createdAt)
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, BACKUP_MIME_TYPE)
            put(MediaStore.Downloads.RELATIVE_PATH, MANAGED_BACKUP_RELATIVE_PATH)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(collection, values)
            ?: throw BackupLocationUnavailableException()
        runCatching {
            context.contentResolver.openOutputStream(uri, "w")?.bufferedWriter()?.use {
                it.write(json.toString(2))
            } ?: throw BackupLocationUnavailableException()
            context.contentResolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                null,
                null,
            )
        }.getOrElse {
            runCatching { context.contentResolver.delete(uri, null, null) }
            if (it is BackupLocationUnavailableException) throw it
            throw BackupLocationUnavailableException(it)
        }

        repository.setLatestBackupUri(uri)
        pruneManagedBackups(collection)
        return uri to createBackupInfo(createdAt, name)
    }

    suspend fun savedBackupTreeUri(): Uri? = repository.backupTreeUri()

    suspend fun restoreInitialUri(): Uri? {
        val lastRestore = repository.lastRestoreUri()
        if (lastRestore != null && canRead(lastRestore)) return lastRestore
        repository.backupDirectoryUri()?.let { return it }
        val latestBackup = repository.latestBackupUri()
        if (latestBackup != null && canRead(latestBackup)) return automaticBackupDirectoryUri()
        return null
    }

    suspend fun restore(uri: Uri) {
        val bytes = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
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
            } ?: throw BackupFileUnavailableException()
        }.getOrElse {
            if (it is BackupFileUnavailableException) throw it
            throw BackupFileUnavailableException(it)
        }
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
        repository.setLastRestoreUri(uri)
    }

    private suspend fun createBackupInfo(createdAt: String, name: String): BackupInfo =
        BackupInfo(
            createdAt = createdAt,
            fileName = name,
            version = 1,
            favoriteCount = repository.currentFavorites().size,
            journalEntryCount = repository.currentJournalEntries().size,
        ).also { repository.setBackupInfo(it) }

    private fun canRead(uri: Uri): Boolean = runCatching {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
    }.getOrDefault(false)

    private fun backupName(createdAt: String): String =
        "$BACKUP_FILE_PREFIX${createdAt.replace(':', '-').replace('.', '-')}.json"

    private fun automaticBackupDirectoryUri(): Uri = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY,
        "$PRIMARY_STORAGE_DOCUMENT_ID:${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_DIRECTORY_NAME",
    )

    private fun pruneManagedBackups(collection: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        runCatching {
            val entries = buildList {
                context.contentResolver.query(
                    collection,
                    arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME),
                    "${MediaStore.Downloads.RELATIVE_PATH} = ? AND ${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                    arrayOf(MANAGED_BACKUP_RELATIVE_PATH, "$BACKUP_FILE_PREFIX%"),
                    "${MediaStore.Downloads.DATE_ADDED} DESC",
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    while (cursor.moveToNext()) add(ContentUris.withAppendedId(collection, cursor.getLong(idColumn)))
                }
            }
            entries.drop(MAX_STORED_BACKUPS).forEach {
                context.contentResolver.delete(it, null, null)
            }
        }
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
        private const val MAX_STORED_BACKUPS = 10
        private const val BACKUP_FILE_PREFIX = "hope-cards-backup-"
        private const val BACKUP_MIME_TYPE = "application/json"
        private const val EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"
        private const val PRIMARY_STORAGE_DOCUMENT_ID = "primary"
        const val BACKUP_DIRECTORY_NAME = "Hope Cards Backups"
        private val MANAGED_BACKUP_RELATIVE_PATH =
            "${Environment.DIRECTORY_DOWNLOADS}/$BACKUP_DIRECTORY_NAME/"
        const val BACKUP_LOCATION_HELP =
            "The saved backup location is unavailable. Choose it again, or look for the Hope Cards Backups folder in Downloads, Documents, or your cloud storage."
        const val BACKUP_FILE_HELP =
            "That backup file is unavailable. Look in the Hope Cards Backups folder, Downloads, Documents, or your cloud storage."
    }
}

class BackupLocationUnavailableException(cause: Throwable? = null) :
    IllegalStateException(BackupManager.BACKUP_LOCATION_HELP, cause)

class BackupFileUnavailableException(cause: Throwable? = null) :
    IllegalStateException(BackupManager.BACKUP_FILE_HELP, cause)
