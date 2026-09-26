package com.aaronsedna.hopecards.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import com.aaronsedna.hopecards.notifications.DailyHopeRequest
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.ads.AdPlacementPolicy
import com.aaronsedna.hopecards.ads.BannerAd
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.ui.screens.BibleQuizRoute
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.ui.screens.VerseArtScreen
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.screens.AboutScreen
import com.aaronsedna.hopecards.ui.screens.BibleTranslationDialog
import com.aaronsedna.hopecards.ui.screens.DailyHopeScreen
import com.aaronsedna.hopecards.ui.screens.DailySharePresentation
import com.aaronsedna.hopecards.ui.screens.DeleteJournalDialog
import com.aaronsedna.hopecards.ui.screens.FavoritesScreen
import com.aaronsedna.hopecards.ui.screens.HomeScreen
import com.aaronsedna.hopecards.ui.screens.JournalEditorDialog
import com.aaronsedna.hopecards.ui.screens.JournalScreen
import com.aaronsedna.hopecards.ui.screens.PrivacyScreen
import com.aaronsedna.hopecards.ui.screens.RemoveAdsScreen
import com.aaronsedna.hopecards.ui.screens.SettingsScreen
import com.aaronsedna.hopecards.ui.screens.VerseDetailScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import com.aaronsedna.hopecards.ui.theme.ClassicHopeColors
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

private data class DrawerEntry(
    val destination: Destination,
)

private const val SHARE_ATTRIBUTION = "Shared from Hope Cards ❤️"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"

private class OpenBackupDocumentContract : ActivityResultContract<Uri?, Uri?>() {
    override fun createIntent(context: Context, input: Uri?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/json", "text/plain", "application/octet-stream"),
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                input?.let { putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) }
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}

private val primaryEntries = listOf(
    DrawerEntry(Destination.HOME),
    DrawerEntry(Destination.DAILY),
    DrawerEntry(Destination.VERSE_ART),
    DrawerEntry(Destination.BIBLE_QUIZ),
    DrawerEntry(Destination.FAVORITES),
    DrawerEntry(Destination.JOURNAL),
    DrawerEntry(Destination.SETTINGS),
    DrawerEntry(Destination.REMOVE_ADS),
)

private val informationEntries = listOf(
    DrawerEntry(Destination.PRIVACY),
    DrawerEntry(Destination.ABOUT),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HopeCardsApp(
    dailyHopeRequests: StateFlow<DailyHopeRequest?>,
    updateReady: StateFlow<Boolean>,
    onCompleteUpdate: () -> Unit,
    viewModel: HopeCardsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val billing by viewModel.billing.state.collectAsStateWithLifecycle()
    val adsReady by viewModel.ads.ready.collectAsStateWithLifecycle()
    val privacyOptionsRequired by viewModel.ads.privacyOptionsRequired.collectAsStateWithLifecycle()
    val dailyRequest by dailyHopeRequests.collectAsStateWithLifecycle()
    val isUpdateReady by updateReady.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val localizedContext = remember(state.settings.preferredTranslation) {
        context.forTranslation(state.settings.preferredTranslation)
    }
    val updateReadyMessage = localizedContext.getString(com.aaronsedna.hopecards.R.string.update_ready)
    val restartToUpdateLabel = localizedContext.getString(com.aaronsedna.hopecards.R.string.restart_to_update)
    val activity = LocalActivity.current ?: return
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val quizStateHolder = rememberSaveableStateHolder()
    var quizExitHandler by remember { mutableStateOf<((() -> Unit) -> Unit)?>(null) }
    val registerQuizExitHandler = remember { { handler: ((() -> Unit) -> Unit)? -> quizExitHandler = handler } }
    val navigate: (Destination) -> Unit = { destination ->
        val proceed = { viewModel.navigate(destination) }
        if (state.destination == Destination.BIBLE_QUIZ && destination != Destination.BIBLE_QUIZ) {
            quizExitHandler?.invoke(proceed) ?: proceed()
        } else proceed()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val adsSuppressed = billing.isAdFree || BuildConfig.SCREENSHOT_MODE
    var drawerLoaded by rememberSaveable { mutableStateOf(false) }
    var dailySharePresentation by remember { mutableStateOf<Verse?>(null) }
    var journalEntryInDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var editingJournalEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var deletingJournalEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var translationPickerOpen by rememberSaveable { mutableStateOf(false) }
    var artCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var artworkId by rememberSaveable { mutableStateOf<String?>(null) }
    var artworkSession by rememberSaveable { mutableStateOf(0L) }
    var artworkCompletionRecorded by rememberSaveable { mutableStateOf(false) }
    var artworkExitPending by remember { mutableStateOf(false) }
    val artHasBack = state.destination == Destination.VERSE_ART && (artCategoryId != null || artworkId != null)
    val hasBack = state.selectedVerse != null || artHasBack
    val backFromContent = {
        when {
            state.selectedVerse != null -> { journalEntryInDetail = null; viewModel.closeVerse() }
            state.destination == Destination.VERSE_ART && artworkId != null -> {
                if (!artworkExitPending) {
                    if (artworkCompletionRecorded || adsSuppressed) {
                        artworkId = null
                    } else {
                        val exitingArtwork = artworkId
                        val exitingSession = artworkSession
                        // Record once per visit, including across recreation or repeated Back taps.
                        artworkCompletionRecorded = true
                        artworkExitPending = true
                        val stillViewingArtwork = {
                            artworkSession == exitingSession && artworkId == exitingArtwork &&
                                viewModel.uiState.value.destination == Destination.VERSE_ART &&
                                viewModel.uiState.value.selectedVerse == null && !activity.isDestroyed
                        }
                        viewModel.completedArtwork(
                            activity,
                            isCurrentArtwork = { stillViewingArtwork() && !drawerState.isOpen && !billing.isAdFree },
                            continueToGallery = {
                                if (stillViewingArtwork()) artworkId = null
                                if (artworkSession == exitingSession) artworkExitPending = false
                            },
                        )
                    }
                }
            }
            state.destination == Destination.VERSE_ART && artCategoryId != null -> artCategoryId = null
            else -> navigate(Destination.HOME)
        }
    }

    val closeVerse = {
        journalEntryInDetail = null
        viewModel.closeVerse()
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setReminderEnabled(granted)
        if (!granted) viewModel.showNotice(localizedContext.getString(com.aaronsedna.hopecards.R.string.notifications_disabled))
    }
    LaunchedEffect(state.initialized) {
        if (state.initialized && !BuildConfig.SCREENSHOT_MODE &&
            state.settings.dailyHopeReminderEnabled && Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            viewModel.claimReminderPermissionPrompt()
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val restoreBackup = rememberLauncherForActivityResult(
        OpenBackupDocumentContract(),
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            pendingRestoreUri = it
        }
    }
    val backupFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }.onFailure {
                viewModel.showNotice(localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_folder_access_failed))
                return@let
            }
            scope.launch {
                runCatching { viewModel.createBackupIn(it) }
                    .onSuccess {
                        viewModel.showNotice(
                            localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_cloud_connected),
                        )
                    }
                    .onFailure { error ->
                        viewModel.showNotice(error.message ?: localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_save_failed))
                    }
            }
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.billing.refresh()
                    viewModel.ads.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.ads.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        viewModel.billing.connect()
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.initialized, billing.loading, adsSuppressed) {
        if (state.initialized && !billing.loading) {
            delay(500)
            viewModel.initializeAds(activity, adsSuppressed)
        }
    }
    LaunchedEffect(dailyRequest, state.initialized) {
        if (dailyRequest != null && state.initialized) {
            drawerState.close()
            translationPickerOpen = false
            editingJournalEntry = null
            deletingJournalEntry = null
            pendingRestoreUri = null
        }
    }
    LaunchedEffect(isUpdateReady) {
        if (isUpdateReady) {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = updateReadyMessage,
                actionLabel = restartToUpdateLabel,
                withDismissAction = false,
                duration = SnackbarDuration.Indefinite,
            )
            if (result == SnackbarResult.ActionPerformed) onCompleteUpdate()
        }
    }

    LaunchedEffect(state.notice) {
        val notice = state.notice ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(notice, withDismissAction = true, duration = SnackbarDuration.Long)
        viewModel.clearNotice(notice)
    }
    LaunchedEffect(billing.message, state.destination) {
        if (state.destination == Destination.REMOVE_ADS) {
            val message = billing.message ?: return@LaunchedEffect
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, withDismissAction = true, duration = SnackbarDuration.Long)
            viewModel.billing.clearMessage()
        }
    }

    BackHandler(enabled = state.selectedVerse != null || state.destination != Destination.HOME) {
        backFromContent()
    }

    ProvideAppTranslation(state.settings.preferredTranslation) {
    HopeCardsTheme(state.settings.themeName) {
        val colors = LocalHopeColors.current
        val screenColors = if (state.destination == Destination.DAILY) ClassicHopeColors else colors
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerLoaded && state.selectedVerse == null,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(305.dp),
                    drawerContainerColor = colors.homeBackground,
                ) {
                    if (drawerLoaded) Column(Modifier.fillMaxHeight()) {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            primaryEntries.forEach { entry ->
                                DrawerItem(entry, state.destination) { destination ->
                                    journalEntryInDetail = null
                                    artCategoryId = null
                                    artworkId = null
                                    scope.launch { drawerState.close(); navigate(destination) }
                                }
                                if (entry.destination == Destination.SETTINGS) {
                                    HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 6.dp), color = colors.divider)
                                }
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 6.dp), color = colors.divider)
                            DrawerItems(informationEntries, state.destination) { destination ->
                                journalEntryInDetail = null
                                scope.launch { drawerState.close(); navigate(destination) }
                            }
                        }
                        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
                            Text(appString(com.aaronsedna.hopecards.R.string.app_name), color = colors.textSecondary, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp)
                            Text(
                                appString(com.aaronsedna.hopecards.R.string.version_format, BuildConfig.VERSION_NAME.removeSuffix("-debug")),
                                color = colors.textTertiary,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            },
        ) {
            Scaffold(
                containerColor = when (state.destination) {
                    Destination.HOME -> colors.homeBackground
                    Destination.DAILY -> ClassicHopeColors.background
                    else -> colors.background
                },
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        CalmSnackbar(data)
                    }
                },
                topBar = {
                    if (dailySharePresentation == null) TopAppBar(
                        title = {
                            Text(
                                when {
                                    state.selectedVerse != null -> appString(com.aaronsedna.hopecards.R.string.verse)
                                    state.destination == Destination.VERSE_ART && artworkId == null -> VerseArtCatalog.title(artCategoryId)
                                    else -> destinationTitle(state.destination)
                                },
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (hasBack) backFromContent()
                                else scope.launch {
                                    drawerLoaded = true
                                    delay(16)
                                    drawerState.open()
                                }
                            }) {
                                AppIcon(
                                    if (hasBack) AppIconGlyph.ArrowBack else AppIconGlyph.Menu,
                                    if (hasBack) appString(com.aaronsedna.hopecards.R.string.back) else appString(com.aaronsedna.hopecards.R.string.open_navigation),
                                    screenColors.text,
                                    size = 25.dp,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = when (state.destination) {
                                Destination.HOME -> colors.homeBackground
                                Destination.DAILY -> ClassicHopeColors.background
                                else -> colors.background
                            },
                            titleContentColor = screenColors.text,
                            navigationIconContentColor = screenColors.text,
                        ),
                    )
                },
                bottomBar = {
                    if (
                        dailySharePresentation == null && adsReady && !adsSuppressed && state.selectedVerse == null &&
                        AdPlacementPolicy.showsBanner(state.destination)
                    ) {
                        Column(Modifier.fillMaxWidth().background(colors.background).navigationBarsPadding()) {
                            BannerAd()
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    val sharingVerse = dailySharePresentation
                    if (sharingVerse != null) {
                        DailySharePresentation(sharingVerse)
                    } else if (state.initialized) {
                        val selected = state.selectedVerse
                        if (selected != null) {
                            val selectedJournalEntry = journalEntryInDetail?.let { selectedEntry ->
                                state.journalEntries.firstOrNull { it.id == selectedEntry.id }
                            }
                            VerseDetailScreen(
                                verse = selected,
                                favorite = selected.id in state.favorites,
                                onFavorite = { viewModel.toggleFavorite(selected) },
                                onShare = { shareVerse(activity, selected) },
                                onEditJournal = selectedJournalEntry?.let { entry ->
                                    { editingJournalEntry = entry }
                                },
                                hapticsEnabled = state.settings.enableHaptics,
                                onChangeTranslation = { translationPickerOpen = true },
                            )
                        } else {
                            when (state.destination) {
                                Destination.HOME -> state.currentVerse?.let { verse ->
                                    HomeScreen(
                                        verse = verse,
                                        favorite = verse.id in state.favorites,
                                        settings = state.settings,
                                        onNextVerse = viewModel::nextCard,
                                        onFavorite = { viewModel.toggleFavorite(verse) },
                                        onShare = { shareVerse(activity, verse) },
                                        onCompletedCard = { completedActivity ->
                                            if (!BuildConfig.SCREENSHOT_MODE) viewModel.completedCard(completedActivity)
                                        },
                                        onChangeTranslation = { translationPickerOpen = true },
                                    )
                                }
                                Destination.DAILY -> state.dailyVerse?.let { verse ->
                                    val existing = state.journalEntries.firstOrNull {
                                        it.date == LocalDate.now().toString() && it.verseId == verse.id
                                    }?.note.orEmpty()
                                    DailyHopeScreen(
                                        verse = verse,
                                        settings = state.settings,
                                        favorite = verse.id in state.favorites,
                                        existingNote = existing,
                                        onFavorite = { viewModel.toggleFavorite(verse) },
                                        onShare = {
                                            dailySharePresentation = verse
                                            scope.launch {
                                                delay(180)
                                                runCatching { shareDailyHopeImage(activity, verse) }
                                                    .onFailure { shareVerse(activity, verse) }
                                                dailySharePresentation = null
                                            }
                                        },
                                        onSaveReflection = {
                                            viewModel.saveReflection(verse, it)
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    when {
                                                        it.isBlank() -> localizedContext.getString(com.aaronsedna.hopecards.R.string.removed_from_journal)
                                                        existing.isBlank() -> localizedContext.getString(com.aaronsedna.hopecards.R.string.added_to_journal)
                                                        else -> localizedContext.getString(com.aaronsedna.hopecards.R.string.journal_updated)
                                                    },
                                                )
                                            }
                                        },
                                        onChangeTranslation = { translationPickerOpen = true },
                                    )
                                }
                                Destination.VERSE_ART -> VerseArtScreen(
                                    categoryId = artCategoryId,
                                    artworkId = artworkId,
                                    favorites = state.favorites,
                                    onCategory = { artCategoryId = it },
                                    onArtwork = {
                                        artworkSession += 1
                                        artworkCompletionRecorded = false
                                        artworkExitPending = false
                                        artworkId = it
                                    },
                                    onFavorite = viewModel::toggleArtworkFavorite,
                                    onNotice = viewModel::showNotice,
                                )
                                Destination.BIBLE_QUIZ -> {
                                    val translation = state.settings.preferredTranslation
                                    // Keep each language’s round when visiting settings or changing editions.
                                    quizStateHolder.SaveableStateProvider("quiz-${QuizLanguage.forTranslation(translation).code}") {
                                        BibleQuizRoute(translation,
                                            hapticsEnabled = state.settings.enableHaptics,
                                            onExitHandlerChanged = registerQuizExitHandler,
                                            onComplete = { isCurrent, proceed ->
                                                if (adsSuppressed) proceed()
                                                else viewModel.completedQuiz(activity,
                                                    isCurrentQuiz = {
                                                        isCurrent() && !drawerState.isOpen &&
                                                            viewModel.uiState.value.destination == Destination.BIBLE_QUIZ &&
                                                            !translationPickerOpen
                                                    }, continueNavigation = proceed)
                                            })
                                    }
                                }
                                Destination.FAVORITES -> FavoritesScreen(
                                    verses = viewModel.favoriteVerses(),
                                    onOpen = { verse ->
                                        journalEntryInDetail = null
                                        viewModel.openVerse(verse)
                                    },
                                    onRemove = { verse -> viewModel.removeFavorites(setOf(verse.id)) },
                                    onRemoveSelected = viewModel::removeFavorites,
                                )
                                Destination.JOURNAL -> JournalScreen(
                                    entries = state.journalEntries,
                                    verseFor = viewModel::journalVerse,
                                    onOpenEntry = { entry, verse ->
                                        journalEntryInDetail = entry
                                        viewModel.openVerse(verse)
                                    },
                                    onDeleteEntry = { entry -> viewModel.deleteJournalEntries(setOf(entry.id)) },
                                    onDeleteSelected = viewModel::deleteJournalEntries,
                                )
                                Destination.REMOVE_ADS -> RemoveAdsScreen(
                                    billing = billing,
                                    privacyOptionsRequired = privacyOptionsRequired,
                                    onPurchase = { viewModel.purchaseRemoveAds(activity) },
                                    onRestore = viewModel::restorePurchases,
                                    onPrivacyOptions = { viewModel.ads.showPrivacyOptions(activity) },
                                )
                                Destination.SETTINGS -> SettingsScreen(
                                    settings = state.settings,
                                    onUpdate = viewModel::updateSettings,
                                    onNotificationSettings = {
                                        ReminderScheduler(context).openSystemSettings(state.settings.preferredTranslation)
                                    },
                                    onEnableReminder = {
                                        if (Build.VERSION.SDK_INT < 33 ||
                                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            viewModel.setReminderEnabled(true)
                                        } else {
                                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    onBackup = {
                                        scope.launch {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                runCatching { viewModel.createAutomaticBackup() }
                                                    .onSuccess {
                                                        val cloudFolder = viewModel.savedBackupFolder()
                                                        if (cloudFolder == null) {
                                                            viewModel.showNotice(
                                                                localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_local_ready),
                                                            )
                                                        } else {
                                                            runCatching { viewModel.createBackupIn(cloudFolder) }
                                                                .onSuccess {
                                                                    viewModel.showNotice(localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_local_cloud_ready))
                                                                }
                                                                .onFailure { error ->
                                                                    viewModel.showNotice(
                                                                        error.message ?: localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_local_cloud_failed),
                                                                    )
                                                                }
                                                        }
                                                    }
                                                    .onFailure { error ->
                                                        viewModel.showNotice(error.message ?: localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_create_failed))
                                                    }
                                            } else {
                                                val savedFolder = viewModel.savedBackupFolder()
                                                if (savedFolder == null) {
                                                    backupFolderPicker.launch(null)
                                                } else {
                                                    runCatching { viewModel.createBackupIn(savedFolder) }
                                                        .onSuccess {
                                                            viewModel.showNotice(localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_folder_ready))
                                                        }
                                                        .onFailure { error ->
                                                            viewModel.showNotice(error.message ?: localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_choose_again))
                                                            backupFolderPicker.launch(savedFolder)
                                                        }
                                                }
                                            }
                                        }
                                    },
                                    onBackupLocation = {
                                        scope.launch {
                                            backupFolderPicker.launch(viewModel.savedBackupFolder())
                                        }
                                    },
                                    onExport = {
                                        scope.launch {
                                            runCatching { viewModel.createBackup() }
                                                .onSuccess { (uri, _) -> shareBackup(activity, uri, localizedContext.getString(com.aaronsedna.hopecards.R.string.save_backup_title)) }
                                                .onFailure { viewModel.showNotice(it.message ?: localizedContext.getString(com.aaronsedna.hopecards.R.string.backup_export_failed)) }
                                        }
                                    },
                                    onRestore = {
                                        scope.launch {
                                            restoreBackup.launch(viewModel.restoreInitialUri())
                                        }
                                    },
                                )
                                Destination.PRIVACY -> PrivacyScreen(
                                    if (privacyOptionsRequired && !billing.isAdFree) {
                                        { viewModel.ads.showPrivacyOptions(activity) }
                                    } else null,
                                )
                                Destination.ABOUT -> AboutScreen()
                            }
                        }
                    }
                }
            }
        }

        pendingRestoreUri?.let { uri ->
            RestoreBackupDialog(
                onDismiss = { pendingRestoreUri = null },
                onRestore = {
                    pendingRestoreUri = null
                    viewModel.restoreBackup(uri)
                },
            )
        }
        editingJournalEntry?.let { entry ->
            JournalEditorDialog(
                entry = entry,
                displayReference = viewModel.journalVerse(entry.verseId)?.displayReference ?: entry.reference,
                onDismiss = { editingJournalEntry = null },
                onSave = { updated ->
                    viewModel.saveJournalEntry(updated)
                    editingJournalEntry = null
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(localizedContext.getString(com.aaronsedna.hopecards.R.string.journal_updated))
                    }
                },
                onDeleteRequest = {
                    editingJournalEntry = null
                    deletingJournalEntry = entry
                },
            )
        }
        deletingJournalEntry?.let { entry ->
            DeleteJournalDialog(
                onDismiss = { deletingJournalEntry = null },
                onDelete = {
                    viewModel.saveJournalEntry(entry.copy(note = ""))
                    deletingJournalEntry = null
                    closeVerse()
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(localizedContext.getString(com.aaronsedna.hopecards.R.string.removed_from_journal))
                    }
                },
            )
        }
        if (translationPickerOpen) {
            BibleTranslationDialog(
                selected = state.settings.preferredTranslation,
                onDismiss = { translationPickerOpen = false },
                onSelect = { translation ->
                    viewModel.updateSettings { it.copy(preferredTranslation = translation) }
                    translationPickerOpen = false
                },
            )
        }
    }
    }
}

@Composable
private fun RestoreBackupDialog(onDismiss: () -> Unit, onRestore: () -> Unit) {
    val colors = LocalHopeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(shape = CircleShape, color = colors.accentSoft, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(AppIconGlyph.RefreshOutline, null, colors.accent, size = 22.dp)
                }
            }
        },
        title = {
            Text(
                appString(com.aaronsedna.hopecards.R.string.restore_backup_title),
                color = colors.text,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
            )
        },
        text = {
            Text(
                appString(com.aaronsedna.hopecards.R.string.restore_backup_message),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onRestore) {
                Text(appString(com.aaronsedna.hopecards.R.string.restore), fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(appString(com.aaronsedna.hopecards.R.string.cancel), fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surface,
        tonalElevation = 0.dp,
    )
}

@Composable
private fun DrawerItems(
    entries: List<DrawerEntry>,
    selected: Destination,
    onSelect: (Destination) -> Unit,
) {
    entries.forEach { entry ->
        DrawerItem(entry, selected, onSelect)
    }
}

@Composable
private fun DrawerItem(entry: DrawerEntry, selected: Destination, onSelect: (Destination) -> Unit) {
    val colors = LocalHopeColors.current
    NavigationDrawerItem(
        label = { Text(destinationTitle(entry.destination), fontFamily = Poppins, fontWeight = FontWeight.SemiBold) },
        selected = selected == entry.destination,
        onClick = { onSelect(entry.destination) },
        icon = {
            AppIcon(
                drawerIcon(entry.destination),
                null,
                if (selected == entry.destination) colors.text else colors.cardText,
                size = 24.dp,
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.drawerActiveBackground,
            selectedIconColor = colors.text,
            selectedTextColor = colors.text,
            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            unselectedIconColor = colors.cardText,
            unselectedTextColor = colors.cardText,
        ),
    )
}

private fun drawerIcon(destination: Destination) = when (destination) {
    Destination.HOME -> AppIconGlyph.HomeOutline
    Destination.DAILY -> AppIconGlyph.SunnyOutline
    Destination.VERSE_ART -> AppIconGlyph.ImagesOutline
    Destination.BIBLE_QUIZ -> AppIconGlyph.PuzzleOutline
    Destination.FAVORITES -> AppIconGlyph.HeartOutline
    Destination.JOURNAL -> AppIconGlyph.JournalOutline
    Destination.REMOVE_ADS -> AppIconGlyph.SparklesOutline
    Destination.SETTINGS -> AppIconGlyph.SettingsOutline
    Destination.PRIVACY -> AppIconGlyph.ShieldCheckmarkOutline
    Destination.ABOUT -> AppIconGlyph.InformationCircleOutline
}

@Composable
private fun destinationTitle(destination: Destination): String = appString(
    when (destination) {
        Destination.HOME -> com.aaronsedna.hopecards.R.string.nav_home
        Destination.DAILY -> com.aaronsedna.hopecards.R.string.nav_daily_hope
        Destination.VERSE_ART -> com.aaronsedna.hopecards.R.string.nav_verse_art
        Destination.BIBLE_QUIZ -> com.aaronsedna.hopecards.R.string.nav_bible_quiz
        Destination.FAVORITES -> com.aaronsedna.hopecards.R.string.nav_favorites
        Destination.JOURNAL -> com.aaronsedna.hopecards.R.string.nav_journal
        Destination.REMOVE_ADS -> com.aaronsedna.hopecards.R.string.nav_remove_ads
        Destination.SETTINGS -> com.aaronsedna.hopecards.R.string.nav_settings
        Destination.PRIVACY -> com.aaronsedna.hopecards.R.string.nav_privacy
        Destination.ABOUT -> com.aaronsedna.hopecards.R.string.nav_about
    },
)

@Composable
private fun CalmSnackbar(data: SnackbarData) {
    val colors = LocalHopeColors.current
    androidx.compose.material3.Snackbar(
        snackbarData = data,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        containerColor = colors.text,
        contentColor = colors.background,
        actionColor = colors.background,
        dismissActionContentColor = colors.background,
    )
}

internal fun verseShareText(verse: Verse, attribution: String = SHARE_ATTRIBUTION): String =
    "${verse.text}\n\n— ${verse.displayReference} • ${verse.translation}\n\n" +
        "$attribution\n" +
        PLAY_STORE_URL

internal fun dailyHopeImageShareText(attribution: String = SHARE_ATTRIBUTION): String = "$attribution\n$PLAY_STORE_URL"

private fun shareVerse(activity: Activity, verse: Verse) {
    val localized = activity.forTranslation(verse.edition)
    activity.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, verse.displayReference)
                putExtra(Intent.EXTRA_TEXT, verseShareText(verse, localized.getString(com.aaronsedna.hopecards.R.string.share_attribution)))
            },
            localized.getString(com.aaronsedna.hopecards.R.string.share_card_title),
        ),
    )
}

private fun shareBackup(activity: Activity, uri: android.net.Uri, chooserTitle: String) {
    activity.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            chooserTitle,
        ),
    )
}

private suspend fun shareDailyHopeImage(activity: Activity, verse: Verse) {
    val localized = activity.forTranslation(verse.edition)
    val bitmap = withContext(Dispatchers.Main.immediate) {
        val view = activity.window.decorView.rootView
        check(view.width > 0 && view.height > 0) { "Daily Hope is not ready to share." }
        val scale = minOf(1f, 1440f / view.width, 2560f / view.height)
        createBitmap(
            (view.width * scale).toInt().coerceAtLeast(1),
            (view.height * scale).toInt().coerceAtLeast(1),
            Bitmap.Config.RGB_565,
        ).also { output ->
            Canvas(output).run {
                scale(scale, scale)
                view.draw(this)
            }
        }
    }
    val uri = withContext(Dispatchers.IO) {
        val directory = File(activity.cacheDir, "shared").apply { mkdirs() }
        directory.listFiles()?.forEach(File::delete)
        val file = File(directory, "daily-hope-${verse.id.hashCode().toUInt()}.png")
        file.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Daily Hope image could not be created." }
        }
        bitmap.recycle()
        FileProvider.getUriForFile(activity, "${activity.packageName}.files", file)
    }
    withContext(Dispatchers.Main.immediate) {
        activity.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_SUBJECT, verse.displayReference)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        dailyHopeImageShareText(localized.getString(com.aaronsedna.hopecards.R.string.share_attribution)),
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("Daily Hope", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                localized.getString(com.aaronsedna.hopecards.R.string.share_daily_title),
            ),
        )
    }
}
