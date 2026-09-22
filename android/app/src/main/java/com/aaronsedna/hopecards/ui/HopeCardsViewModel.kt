package com.aaronsedna.hopecards.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aaronsedna.hopecards.ads.AdsManager
import com.aaronsedna.hopecards.billing.BillingManager
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.BackupManager
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.DailyHopeRecord
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.ui.forTranslation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class HopeUiState(
    val initialized: Boolean = false,
    val destination: Destination = Destination.HOME,
    val settings: AppSettings = AppSettings(),
    val favorites: Set<String> = emptySet(),
    val journalEntries: List<JournalEntry> = emptyList(),
    val currentVerse: Verse? = null,
    val dailyVerse: Verse? = null,
    val selectedVerse: Verse? = null,
    val notice: String? = null,
)

class HopeCardsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val verseRepository = VerseRepository(application)
    val billing = BillingManager(application, repository)
    val ads = AdsManager(application, repository)
    val backups = BackupManager(application, repository, verseRepository)
    private val reminders = ReminderScheduler(application)

    private val _uiState = MutableStateFlow(HopeUiState())
    val uiState: StateFlow<HopeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initialize()
            combine(repository.settings, repository.favorites, repository.journalEntries) {
                    settings, favorites, journal -> Triple(settings, favorites, journal)
                }
                .collect { (settings, favorites, journal) ->
                    val previous = _uiState.value
                    val current = previous.currentVerse?.let {
                        verseRepository.byId(it.id, settings.preferredTranslation)
                    } ?: verseRepository.random(settings.preferredTranslation)
                    _uiState.value = previous.copy(
                        initialized = true,
                        settings = settings,
                        favorites = favorites,
                        journalEntries = journal.sortedByDescending { it.updatedAt },
                        currentVerse = current,
                        selectedVerse = previous.selectedVerse?.let {
                            verseRepository.byId(it.id, settings.preferredTranslation)
                        },
                    )
                    if (
                        previous.dailyVerse == null ||
                        previous.settings.preferredTranslation != settings.preferredTranslation
                    ) {
                        loadDailyHope(settings)
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.settings
                .distinctUntilChanged { old, new ->
                    old.dailyHopeReminderEnabled == new.dailyHopeReminderEnabled &&
                        old.dailyHopeReminderHour == new.dailyHopeReminderHour &&
                        old.dailyHopeReminderMinute == new.dailyHopeReminderMinute &&
                        old.preferredTranslation == new.preferredTranslation
                }
                .collect { settings ->
                    if (settings.dailyHopeReminderEnabled) {
                        reminders.schedule(
                            settings.dailyHopeReminderHour,
                            settings.dailyHopeReminderMinute,
                            settings.preferredTranslation,
                        )
                    } else reminders.cancel()
                }
        }
    }

    fun navigate(destination: Destination) {
        _uiState.value = _uiState.value.copy(destination = destination, selectedVerse = null)
        if (destination == Destination.DAILY) {
            viewModelScope.launch(Dispatchers.IO) { loadDailyHope(_uiState.value.settings) }
        }
    }

    fun openVerse(verse: Verse) {
        _uiState.value = _uiState.value.copy(selectedVerse = verse)
    }

    fun favoriteVerses(): List<Verse> {
        val state = _uiState.value
        return state.favorites.mapNotNull { id ->
            verseRepository.byId(id, state.settings.preferredTranslation)
        }.sortedBy { it.displayReference }
    }

    fun journalVerse(verseId: String): Verse? =
        verseRepository.byId(verseId, _uiState.value.settings.preferredTranslation)

    fun closeVerse() {
        _uiState.value = _uiState.value.copy(selectedVerse = null)
    }

    fun nextCard() {
        val state = _uiState.value
        _uiState.value = state.copy(
            currentVerse = verseRepository.random(
                state.settings.preferredTranslation,
                state.currentVerse?.id,
            ),
        )
    }

    fun toggleFavorite(verse: Verse) {
        viewModelScope.launch { repository.toggleFavorite(verse.id) }
    }

    fun toggleArtworkFavorite(artwork: com.aaronsedna.hopecards.model.VerseArtwork) {
        viewModelScope.launch { repository.toggleFavorite(artwork.verseId) }
    }

    fun removeFavorites(ids: Set<String>) {
        viewModelScope.launch { repository.removeFavorites(ids) }
    }

    fun saveReflection(verse: Verse, note: String) {
        val today = LocalDate.now().toString()
        val entry = JournalEntry(
            id = "$today:${verse.id}",
            date = today,
            verseId = verse.id,
            reference = verse.reference,
            prompt = verse.category.lowercase(),
            note = note,
            updatedAt = Instant.now().toString(),
        )
        viewModelScope.launch { repository.saveJournalEntry(entry) }
    }

    fun saveJournalEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.saveJournalEntry(entry.copy(updatedAt = Instant.now().toString()))
        }
    }

    fun deleteJournalEntries(ids: Set<String>) {
        viewModelScope.launch { repository.deleteJournalEntries(ids) }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        updateSettings { it.copy(dailyHopeReminderEnabled = enabled) }
    }

    fun initializeAds(activity: Activity, isAdFree: Boolean) {
        ads.initialize(activity, isAdFree)
    }

    fun completedCard(activity: Activity) {
        ads.recordCompletedCard(activity, billing.state.value.isAdFree)
    }

    fun purchaseRemoveAds(activity: Activity) {
        billing.launchPurchase(activity)
    }

    fun restorePurchases() {
        billing.refresh(reportErrors = true)
    }

    suspend fun createBackup(): Pair<Uri, com.aaronsedna.hopecards.model.BackupInfo> =
        withContext(Dispatchers.IO) { backups.createBackup() }

    suspend fun createBackupIn(folderUri: Uri): Pair<Uri, com.aaronsedna.hopecards.model.BackupInfo> =
        withContext(Dispatchers.IO) { backups.createBackupIn(folderUri) }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun createAutomaticBackup(): Pair<Uri, com.aaronsedna.hopecards.model.BackupInfo> =
        withContext(Dispatchers.IO) { backups.createAutomaticBackup() }

    suspend fun savedBackupFolder(): Uri? =
        withContext(Dispatchers.IO) { backups.savedBackupTreeUri() }

    suspend fun restoreInitialUri(): Uri? =
        withContext(Dispatchers.IO) { backups.restoreInitialUri() }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val localized = getApplication<Application>().forTranslation(_uiState.value.settings.preferredTranslation)
            runCatching { backups.restore(uri) }
                .onSuccess { showNotice(localized.getString(R.string.backup_restored)) }
                .onFailure { showNotice(it.message ?: localized.getString(R.string.backup_restore_failed)) }
        }
    }

    fun showNotice(message: String?) {
        _uiState.value = _uiState.value.copy(notice = message)
    }

    private suspend fun loadDailyHope(settings: AppSettings) {
        val today = LocalDate.now(ZoneOffset.UTC).toString()
        val saved = repository.getDailyHopeRecord()
        val verse = if (saved?.date == today) {
            verseRepository.byId(saved.verseId, settings.preferredTranslation)
        } else null
        val daily = verse ?: verseRepository.random(settings.preferredTranslation, saved?.verseId)
        repository.setDailyHopeRecord(DailyHopeRecord(today, daily.id, settings.preferredTranslation.id))
        _uiState.value = _uiState.value.copy(dailyVerse = daily)
    }

    override fun onCleared() {
        billing.close()
        ads.close()
        super.onCleared()
    }

    companion object {
        val prompts = mapOf(
            "comfort" to "Take a moment to rest in these words.",
            "courage" to "One small step is enough for today.",
            "faith" to "You can move forward without having every answer.",
            "freedom" to "Consider what you can gently let go of today.",
            "grace" to "Receive grace, and offer it where you can.",
            "hope" to "Keep close the words that give you hope.",
            "joy" to "Notice one good thing in this day.",
            "life" to "Notice what helps you feel present and grateful.",
            "love" to "Let these words guide how you care for others today.",
            "peace" to "Place what feels heavy in God’s care.",
            "prayer" to "Let these words become a simple prayer.",
            "strength" to "Take the next step with the strength you have.",
            "trust" to "Place what you cannot control in God’s hands.",
            "wisdom" to "Pause and choose what is thoughtful and kind.",
        )
    }
}
