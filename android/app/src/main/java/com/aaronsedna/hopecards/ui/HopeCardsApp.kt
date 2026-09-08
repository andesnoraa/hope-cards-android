package com.aaronsedna.hopecards.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.ads.BannerAd
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.Verse
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

private val primaryEntries = listOf(
    DrawerEntry(Destination.HOME),
    DrawerEntry(Destination.DAILY),
    DrawerEntry(Destination.FAVORITES),
    DrawerEntry(Destination.JOURNAL),
    DrawerEntry(Destination.REMOVE_ADS),
    DrawerEntry(Destination.SETTINGS),
)

private val informationEntries = listOf(
    DrawerEntry(Destination.PRIVACY),
    DrawerEntry(Destination.ABOUT),
)

private val bannerDestinations = setOf(
    Destination.FAVORITES,
    Destination.JOURNAL,
    Destination.SETTINGS,
    Destination.ABOUT,
    Destination.PRIVACY,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HopeCardsApp(
    dailyHopeRequests: StateFlow<Int>,
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
    val updateReadyMessage = stringResource(com.aaronsedna.hopecards.R.string.update_ready)
    val restartToUpdateLabel = stringResource(com.aaronsedna.hopecards.R.string.restart_to_update)
    val context = LocalContext.current
    val activity = LocalActivity.current ?: return
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val adsSuppressed = billing.isAdFree || BuildConfig.SCREENSHOT_MODE
    var drawerLoaded by rememberSaveable { mutableStateOf(false) }
    var dailySharePresentation by remember { mutableStateOf<Verse?>(null) }
    var journalEntryInDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var editingJournalEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var deletingJournalEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var translationPickerOpen by rememberSaveable { mutableStateOf(false) }

    val closeVerse = {
        journalEntryInDetail = null
        viewModel.closeVerse()
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setReminderEnabled(granted)
        if (!granted) viewModel.showNotice("Notifications are disabled. You can enable them later in Android Settings.")
    }
    val restoreBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(viewModel::restoreBackup) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.billing.refresh()
        }
        lifecycle.addObserver(observer)
        viewModel.billing.connect()
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.initialized, billing.loading, adsSuppressed, state.destination) {
        if (
            state.initialized && !billing.loading && !adsSuppressed &&
            state.destination in bannerDestinations
        ) {
            delay(500)
            viewModel.initializeAds(activity, adsSuppressed)
        }
    }
    LaunchedEffect(dailyRequest, state.initialized) {
        if (dailyRequest > 0 && state.initialized) viewModel.navigate(Destination.DAILY)
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

    BackHandler(enabled = state.selectedVerse != null || state.destination != Destination.HOME) {
        if (state.selectedVerse != null) closeVerse() else viewModel.navigate(Destination.HOME)
    }

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
                        primaryEntries.forEach { entry ->
                            DrawerItem(entry, state.destination) { destination ->
                                journalEntryInDetail = null
                                viewModel.navigate(destination)
                                scope.launch { drawerState.close() }
                            }
                            if (entry.destination == Destination.REMOVE_ADS) {
                                HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 6.dp), color = colors.divider)
                            }
                        }
                        HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 6.dp), color = colors.divider)
                        DrawerItems(informationEntries, state.destination) { destination ->
                            journalEntryInDetail = null
                            viewModel.navigate(destination)
                            scope.launch { drawerState.close() }
                        }
                        Spacer(Modifier.weight(1f))
                        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
                            Text("Hope Cards", color = colors.textSecondary, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp)
                            Text(
                                "Version ${BuildConfig.VERSION_NAME.removeSuffix("-debug")}",
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
                                if (state.selectedVerse != null) "Verse" else state.destination.title,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (state.selectedVerse != null) closeVerse()
                                else scope.launch {
                                    drawerLoaded = true
                                    delay(16)
                                    drawerState.open()
                                }
                            }) {
                                AppIcon(
                                    if (state.selectedVerse != null) AppIconGlyph.ArrowBack else AppIconGlyph.Menu,
                                    if (state.selectedVerse != null) "Back" else "Open navigation",
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
                        state.destination in bannerDestinations
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
                                                        it.isBlank() -> "Removed from your journal."
                                                        existing.isBlank() -> "Added to your journal."
                                                        else -> "Journal entry updated."
                                                    },
                                                )
                                            }
                                        },
                                        onChangeTranslation = { translationPickerOpen = true },
                                    )
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
                                            runCatching { viewModel.createBackup() }
                                                .onSuccess { viewModel.showNotice("Your backup is ready and safely stored on this device.") }
                                                .onFailure { viewModel.showNotice(it.message ?: "Backup could not be created.") }
                                        }
                                    },
                                    onExport = {
                                        scope.launch {
                                            runCatching { viewModel.createBackup() }
                                                .onSuccess { (uri, _) -> shareBackup(activity, uri) }
                                                .onFailure { viewModel.showNotice(it.message ?: "Backup could not be exported.") }
                                        }
                                    },
                                    onRestore = { restoreBackup.launch(arrayOf("application/json", "text/plain")) },
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

        state.notice?.let { notice ->
            NoticeDialog(notice) { viewModel.showNotice(null) }
        }
        if (state.destination == Destination.REMOVE_ADS) {
            billing.message?.let { message ->
                NoticeDialog(message, viewModel.billing::clearMessage)
            }
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
                        snackbarHostState.showSnackbar("Journal entry updated.")
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
                        snackbarHostState.showSnackbar("Removed from your journal.")
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
        label = { Text(entry.destination.title, fontFamily = Poppins, fontWeight = FontWeight.SemiBold) },
        selected = selected == entry.destination,
        onClick = { onSelect(entry.destination) },
        icon = {
            AppIcon(
                drawerIcon(entry.destination),
                null,
                if (selected == entry.destination) colors.accent else colors.cardText,
                size = 24.dp,
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.drawerActiveBackground,
            selectedIconColor = colors.accent,
            selectedTextColor = colors.accent,
            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
            unselectedIconColor = colors.cardText,
            unselectedTextColor = colors.cardText,
        ),
    )
}

private fun drawerIcon(destination: Destination) = when (destination) {
    Destination.HOME -> AppIconGlyph.HomeOutline
    Destination.DAILY -> AppIconGlyph.SunnyOutline
    Destination.FAVORITES -> AppIconGlyph.HeartOutline
    Destination.JOURNAL -> AppIconGlyph.JournalOutline
    Destination.REMOVE_ADS -> AppIconGlyph.SparklesOutline
    Destination.SETTINGS -> AppIconGlyph.SettingsOutline
    Destination.PRIVACY -> AppIconGlyph.ShieldCheckmarkOutline
    Destination.ABOUT -> AppIconGlyph.InformationCircleOutline
}

@Composable
private fun NoticeDialog(message: String, onDismiss: () -> Unit) {
    val colors = LocalHopeColors.current
    val success = message.contains("restored", ignoreCase = true) ||
        message.contains("ready", ignoreCase = true) ||
        message.contains("success", ignoreCase = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(shape = CircleShape, color = colors.accentSoft, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        if (success) AppIconGlyph.CheckmarkCircleOutline else AppIconGlyph.InformationCircleOutline,
                        null,
                        colors.accent,
                        size = 22.dp,
                    )
                }
            }
        },
        title = {
            Text(
                if (success) "All set" else "A quick note",
                color = colors.text,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
            )
        },
        text = { Text(message, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surface,
        tonalElevation = 0.dp,
    )
}

@Composable
private fun CalmSnackbar(data: SnackbarData) {
    val colors = LocalHopeColors.current
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.text,
        contentColor = colors.buttonText,
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(AppIconGlyph.CheckmarkCircleOutline, null, colors.accent, size = 22.dp)
            Text(
                data.visuals.message,
                color = colors.buttonText,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(start = 12.dp).weight(1f),
            )
            data.visuals.actionLabel?.let { actionLabel ->
                TextButton(onClick = data::performAction) {
                    Text(
                        actionLabel,
                        color = colors.accent,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun shareVerse(activity: Activity, verse: Verse) {
    val text = "${verse.text}\n\n— ${verse.displayReference} • ${verse.translation}\n\n" +
        "Shared from Hope Cards ❤️\n\nhttps://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"
    activity.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, verse.displayReference)
                putExtra(Intent.EXTRA_TEXT, text)
            },
            "Share Hope Card",
        ),
    )
}

private fun shareBackup(activity: Activity, uri: android.net.Uri) {
    activity.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Save Hope Cards Backup",
        ),
    )
}

private suspend fun shareDailyHopeImage(activity: Activity, verse: Verse) {
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
                    putExtra(Intent.EXTRA_TEXT, "${verse.displayReference} • ${verse.translation}\n\nShared from Hope Cards ❤️")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("Daily Hope", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Share Daily Hope",
            ),
        )
    }
}
