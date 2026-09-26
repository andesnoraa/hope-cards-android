package com.aaronsedna.hopecards.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.BackupInfo
import com.aaronsedna.hopecards.model.DailyHopeRecord
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.hopeCardsDataStore by preferencesDataStore("hope_cards_native")

internal object InterstitialPolicy {
    const val COMPLETED_CARDS_BETWEEN_ADS = 10
    const val MIN_INTERVAL_MS = 10 * 60 * 1000L

    fun shouldShow(completedCards: Int, elapsedMs: Long): Boolean =
        completedCards >= COMPLETED_CARDS_BETWEEN_ADS && elapsedMs >= MIN_INTERVAL_MS
}

internal object QuizInterstitialPolicy {
    const val COMPLETED_QUIZZES_BETWEEN_ADS = 1
    fun shouldShow(completedQuizzes: Int, elapsedMs: Long): Boolean =
        completedQuizzes >= COMPLETED_QUIZZES_BETWEEN_ADS && elapsedMs >= InterstitialPolicy.MIN_INTERVAL_MS
}

class AppRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.hopeCardsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map(::decodeSettings).distinctUntilChanged()
    val favorites: Flow<Set<String>> = dataStore.data.map { decodeStringSet(it[Keys.favorites]) }.distinctUntilChanged()
    val journalEntries: Flow<List<JournalEntry>> = dataStore.data.map { decodeJournal(it[Keys.journal]) }.distinctUntilChanged()
    val cachedAdFree: Flow<Boolean> = dataStore.data.map { it[Keys.adFree] ?: false }.distinctUntilChanged()

    suspend fun currentSettings(): AppSettings = settings.first()

    /** Persist before launching the dialog, so dismissing it does not prompt on every launch. */
    suspend fun claimReminderPermissionPrompt(): Boolean {
        var shouldPrompt = false
        dataStore.edit { preferences ->
            if (preferences[Keys.reminderPermissionRequested] != true) {
                preferences[Keys.reminderPermissionRequested] = true
                shouldPrompt = true
            }
        }
        return shouldPrompt
    }
    suspend fun currentFavorites(): Set<String> = favorites.first()
    suspend fun currentJournalEntries(): List<JournalEntry> = journalEntries.first()

    suspend fun initialize() {
        val legacy = LegacyDataMigrator(appContext).read()
        dataStore.edit { preferences ->
            if (preferences[Keys.migrated] == true) return@edit

            legacy[LEGACY_SETTINGS]?.let { preferences[Keys.settings] = it }
            legacy[LEGACY_FAVORITES]?.let { preferences[Keys.favorites] = it }
            legacy[LEGACY_JOURNAL]?.let { preferences[Keys.journal] = it }
            legacy[LEGACY_DAILY]?.let { preferences[Keys.dailyHope] = it }
            legacy[LEGACY_BACKUP_INFO]?.let { preferences[Keys.backupInfo] = it }
            legacy[LEGACY_AD_COUNT]?.toIntOrNull()?.let { preferences[Keys.adCount] = it }
            legacy[LEGACY_AD_TIME]?.toLongOrNull()?.let { preferences[Keys.lastAdTime] = it }
            preferences[Keys.migrated] = true
        }
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.settings] = encodeSettings(transform(decodeSettings(preferences)))
        }
    }

    suspend fun replaceSettings(settings: AppSettings) {
        dataStore.edit { it[Keys.settings] = encodeSettings(settings) }
    }

    suspend fun toggleFavorite(id: String): Boolean {
        var saved = false
        dataStore.edit { preferences ->
            val favorites = decodeStringSet(preferences[Keys.favorites]).toMutableSet()
            saved = if (id in favorites) {
                favorites.remove(id)
                false
            } else {
                favorites.add(id)
                true
            }
            preferences[Keys.favorites] = JSONArray(favorites.toList()).toString()
        }
        return saved
    }

    suspend fun replaceFavorites(favorites: Set<String>) {
        dataStore.edit { it[Keys.favorites] = JSONArray(favorites.toList()).toString() }
    }

    suspend fun removeFavorites(ids: Set<String>) {
        if (ids.isEmpty()) return
        dataStore.edit { preferences ->
            val favorites = decodeStringSet(preferences[Keys.favorites]) - ids
            preferences[Keys.favorites] = JSONArray(favorites.toList()).toString()
        }
    }

    suspend fun saveJournalEntry(entry: JournalEntry) {
        dataStore.edit { preferences ->
            val entries = decodeJournal(preferences[Keys.journal]).filterNot { it.id == entry.id }.toMutableList()
            if (entry.note.trim().isNotEmpty()) entries += entry.copy(note = entry.note.trim())
            preferences[Keys.journal] = encodeJournal(entries)
        }
    }

    suspend fun replaceJournalEntries(entries: List<JournalEntry>) {
        dataStore.edit { it[Keys.journal] = encodeJournal(entries) }
    }

    suspend fun deleteJournalEntries(ids: Set<String>) {
        if (ids.isEmpty()) return
        dataStore.edit { preferences ->
            preferences[Keys.journal] = encodeJournal(
                decodeJournal(preferences[Keys.journal]).filterNot { it.id in ids },
            )
        }
    }

    /** Replaces all user-authored backup data in one DataStore transaction. */
    suspend fun replaceBackupData(
        settings: AppSettings,
        favorites: Set<String>,
        journalEntries: List<JournalEntry>,
    ) {
        // Encode first so a malformed value can never leave a partially updated store.
        val encodedSettings = encodeSettings(settings)
        val encodedFavorites = JSONArray(favorites.toList()).toString()
        val encodedJournal = encodeJournal(journalEntries)
        dataStore.edit { preferences ->
            preferences[Keys.settings] = encodedSettings
            preferences[Keys.favorites] = encodedFavorites
            preferences[Keys.journal] = encodedJournal
        }
    }

    suspend fun getDailyHopeRecord(): DailyHopeRecord? =
        dataStore.data.map { decodeDailyHope(it[Keys.dailyHope]) }.firstValue()

    /** The receiver and UI select one verse atomically for the user's local calendar day. */
    suspend fun dailyHopeVerse(
        verses: VerseRepository,
        translation: Translation,
        date: LocalDate = LocalDate.now(),
    ): Verse {
        // Load assets before taking the DataStore transaction.
        verses.verses(translation)
        lateinit var selected: Verse
        dataStore.edit { preferences ->
            val saved = decodeDailyHope(preferences[Keys.dailyHope])
            selected = saved?.takeIf { it.date == date.toString() }
                ?.let { verses.byId(it.verseId, translation) }
                ?: verses.random(translation, saved?.verseId)
            preferences[Keys.dailyHope] = JSONObject()
                .put("date", date.toString())
                .put("verseId", selected.id)
                .put("translation", translation.id)
                .toString()
        }
        return selected
    }

    suspend fun setDailyHopeRecord(record: DailyHopeRecord) {
        dataStore.edit { preferences ->
            preferences[Keys.dailyHope] = JSONObject()
                .put("date", record.date)
                .put("verseId", record.verseId)
                .put("translation", record.translation)
                .toString()
        }
    }

    suspend fun backupInfo(): BackupInfo? =
        dataStore.data.map { decodeBackupInfo(it[Keys.backupInfo]) }.firstValue()

    suspend fun setBackupInfo(info: BackupInfo?) {
        dataStore.edit { preferences ->
            if (info == null) preferences.remove(Keys.backupInfo)
            else preferences[Keys.backupInfo] = encodeBackupInfo(info)
        }
    }

    suspend fun backupTreeUri(): Uri? =
        dataStore.data.map { it[Keys.backupTreeUri]?.let(Uri::parse) }.firstValue()

    suspend fun backupDirectoryUri(): Uri? =
        dataStore.data.map { it[Keys.backupDirectoryUri]?.let(Uri::parse) }.firstValue()

    suspend fun lastRestoreUri(): Uri? =
        dataStore.data.map { it[Keys.lastRestoreUri]?.let(Uri::parse) }.firstValue()

    suspend fun latestBackupUri(): Uri? =
        dataStore.data.map { it[Keys.latestBackupUri]?.let(Uri::parse) }.firstValue()

    suspend fun setBackupLocation(treeUri: Uri, directoryUri: Uri) {
        dataStore.edit { preferences ->
            preferences[Keys.backupTreeUri] = treeUri.toString()
            preferences[Keys.backupDirectoryUri] = directoryUri.toString()
        }
    }

    suspend fun clearBackupLocation() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.backupTreeUri)
            preferences.remove(Keys.backupDirectoryUri)
        }
    }

    suspend fun setLastRestoreUri(uri: Uri) {
        dataStore.edit { it[Keys.lastRestoreUri] = uri.toString() }
    }

    suspend fun setLatestBackupUri(uri: Uri) {
        dataStore.edit { it[Keys.latestBackupUri] = uri.toString() }
    }

    suspend fun setAdFree(value: Boolean) {
        dataStore.edit { it[Keys.adFree] = value }
    }

    suspend fun recordCompletedCard(now: Long): Boolean {
        var shouldShow = false
        dataStore.edit { preferences ->
            val count = ((preferences[Keys.adCount] ?: 0) + 1)
                .coerceAtMost(InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS)
            val elapsed = now - (preferences[Keys.lastAdTime] ?: 0L)
            shouldShow = InterstitialPolicy.shouldShow(count, elapsed)
            // Keep the threshold reached until an ad is actually shown. A temporarily unavailable
            // network/ad must not discard the next eligible impression.
            preferences[Keys.adCount] = count
        }
        return shouldShow
    }

    suspend fun recordCompletedQuiz(now: Long): Boolean {
        var shouldShow = false
        dataStore.edit { preferences ->
            val count = ((preferences[Keys.quizAdCount] ?: 0) + 1)
                .coerceAtMost(QuizInterstitialPolicy.COMPLETED_QUIZZES_BETWEEN_ADS)
            shouldShow = QuizInterstitialPolicy.shouldShow(count, now - (preferences[Keys.lastAdTime] ?: 0L))
            preferences[Keys.quizAdCount] = count
        }
        return shouldShow
    }

    suspend fun recordInterstitialShown(now: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.adCount] = 0
            preferences[Keys.quizAdCount] = 0
            preferences[Keys.lastAdTime] = now
        }
    }

    private fun decodeSettings(preferences: Preferences): AppSettings =
        decodeSettings(preferences[Keys.settings])

    internal fun decodeSettings(json: String?): AppSettings = runCatching {
        val value = JSONObject(json ?: "{}")
        AppSettings(
            showDrawButton = value.optBoolean("showDrawButton", true),
            enableHaptics = value.optBoolean("enableHaptics", true),
            dailyHopeReminderEnabled = value.optBoolean("dailyHopeReminderEnabled", AppSettings().dailyHopeReminderEnabled),
            dailyHopeMusicEnabled = value.optBoolean("dailyHopeMusicEnabled", true),
            dailyHopeReminderHour = value.optInt("dailyHopeReminderHour", AppSettings().dailyHopeReminderHour).coerceIn(0, 23),
            dailyHopeReminderMinute = value.optInt("dailyHopeReminderMinute", 0).coerceIn(0, 59),
            themeName = ThemeName.fromId(value.optString("themeName", "classic")),
            preferredTranslation = Translation.fromId(value.optString("preferredTranslation", "bsb")),
        )
    }.getOrDefault(AppSettings())

    private fun encodeSettings(settings: AppSettings): String = JSONObject()
        .put("showDrawButton", settings.showDrawButton)
        .put("enableHaptics", settings.enableHaptics)
        .put("dailyHopeReminderEnabled", settings.dailyHopeReminderEnabled)
        .put("dailyHopeMusicEnabled", settings.dailyHopeMusicEnabled)
        .put("dailyHopeReminderHour", settings.dailyHopeReminderHour)
        .put("dailyHopeReminderMinute", settings.dailyHopeReminderMinute)
        .put("themeName", settings.themeName.id)
        .put("preferredTranslation", settings.preferredTranslation.id)
        .toString()

    private fun decodeStringSet(json: String?): Set<String> = runCatching {
        val array = JSONArray(json ?: "[]")
        buildSet { repeat(array.length()) { add(array.getString(it)) } }
    }.getOrDefault(emptySet())

    private fun decodeJournal(json: String?): List<JournalEntry> = runCatching {
        val array = JSONArray(json ?: "[]")
        buildList {
            repeat(array.length()) { index ->
                val value = array.getJSONObject(index)
                add(
                    JournalEntry(
                        id = value.getString("id"),
                        date = value.getString("date"),
                        verseId = value.getString("verseId"),
                        reference = value.getString("reference"),
                        prompt = value.getString("prompt"),
                        note = value.getString("note"),
                        updatedAt = value.getString("updatedAt"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun encodeJournal(entries: List<JournalEntry>): String = JSONArray().apply {
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
    }.toString()

    private fun decodeDailyHope(json: String?): DailyHopeRecord? = runCatching {
        if (json.isNullOrBlank()) return@runCatching null
        val value = JSONObject(json)
        DailyHopeRecord(
            date = value.getString("date"),
            verseId = value.getString("verseId"),
            translation = value.optString("translation", "bsb"),
        )
    }.getOrNull()

    private fun decodeBackupInfo(json: String?): BackupInfo? = runCatching {
        if (json.isNullOrBlank()) return@runCatching null
        val value = JSONObject(json)
        BackupInfo(
            createdAt = value.getString("createdAt"),
            fileName = value.getString("fileName"),
            version = value.optInt("version", 1),
            favoriteCount = value.optInt("favoriteCount", 0),
            journalEntryCount = value.optInt("journalEntryCount", 0),
        )
    }.getOrNull()

    private fun encodeBackupInfo(info: BackupInfo): String = JSONObject()
        .put("createdAt", info.createdAt)
        .put("fileName", info.fileName)
        .put("version", info.version)
        .put("favoriteCount", info.favoriteCount)
        .put("journalEntryCount", info.journalEntryCount)
        .toString()

    private object Keys {
        val reminderPermissionRequested = booleanPreferencesKey("reminder_permission_requested")
        val migrated = booleanPreferencesKey("legacy_migrated")
        val settings = stringPreferencesKey(LEGACY_SETTINGS)
        val favorites = stringPreferencesKey(LEGACY_FAVORITES)
        val journal = stringPreferencesKey(LEGACY_JOURNAL)
        val dailyHope = stringPreferencesKey(LEGACY_DAILY)
        val backupInfo = stringPreferencesKey(LEGACY_BACKUP_INFO)
        val backupTreeUri = stringPreferencesKey("backup_tree_uri")
        val backupDirectoryUri = stringPreferencesKey("backup_directory_uri")
        val lastRestoreUri = stringPreferencesKey("last_restore_uri")
        val latestBackupUri = stringPreferencesKey("latest_backup_uri")
        val adFree = booleanPreferencesKey("ad_free_entitlement")
        val adCount = intPreferencesKey(LEGACY_AD_COUNT)
        val quizAdCount = intPreferencesKey("completed_quizzes_since_interstitial")
        val lastAdTime = longPreferencesKey(LEGACY_AD_TIME)
    }

    companion object {
        const val LEGACY_SETTINGS = "hope_cards_settings"
        const val LEGACY_FAVORITES = "hope_cards_favorites"
        const val LEGACY_JOURNAL = "hope_cards_journal_entries"
        const val LEGACY_DAILY = "hope_cards_daily_hope"
        const val LEGACY_BACKUP_INFO = "hope_cards_backup_info"
        const val LEGACY_AD_COUNT = "hope-cards:ads:completed-cards-since-interstitial"
        const val LEGACY_AD_TIME = "hope-cards:ads:last-interstitial-at"
    }
}

private suspend fun <T> Flow<T>.firstValue(): T = first()
