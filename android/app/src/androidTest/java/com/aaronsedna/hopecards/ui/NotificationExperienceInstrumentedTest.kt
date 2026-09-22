package com.aaronsedna.hopecards.ui

import android.app.Notification
import android.app.NotificationManager
import android.graphics.Bitmap
import android.media.AudioManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.Stage
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseArtImages
import com.aaronsedna.hopecards.model.VerseArtCatalog
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.DailyHopeRecord
import java.time.LocalDate
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.notifications.ReminderScheduler
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NotificationExperienceInstrumentedTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test fun notificationColdLaunchRoutesBeforeFirstFrame(): Unit = runBlocking {
        val repository = AppRepository(context)
        repository.initialize()
        val settings = repository.currentSettings()
        val daily = repository.dailyHopeVerse(VerseRepository(context), settings.preferredTranslation)
        val destinationsAtCreation = mutableListOf<Destination>()
        val observer = ActivityLifecycleCallback { activity, stage ->
            if (activity is MainActivity && stage == Stage.CREATED) {
                destinationsAtCreation += ViewModelProvider(activity)[HopeCardsViewModel::class.java].uiState.value.destination
            }
        }
        instrumentation.runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(observer)
        }
        try {
            val intent = android.content.Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_DAILY_HOPE, true)
                .putExtra(MainActivity.EXTRA_DAILY_VERSE_ID, daily.id)
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                assertEquals(listOf(Destination.DAILY), destinationsAtCreation)
                compose.waitUntil(15_000) {
                    var ready = false
                    scenario.onActivity {
                        val state = ViewModelProvider(it)[HopeCardsViewModel::class.java].uiState.value
                        ready = state.dailyVerse?.id == daily.id && state.destination == Destination.DAILY
                    }
                    ready
                }
                compose.mainClock.advanceTimeBy(4_000)
                compose.waitForIdle()
                compose.onNodeWithText(daily.text).assertIsDisplayed()
                compose.onNodeWithText("Draw a Card").assertDoesNotExist()
                capture("daily-cold-launch")
            }
        } finally {
            instrumentation.runOnMainSync {
                ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(observer)
            }
        }
    }

    @Test fun savedImageShowsCompactMessageWithoutModal() = runBlocking {
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        try {
            repository.updateSettings { it.copy(preferredTranslation = Translation.BSB) }
            val gallery = VerseArtImages.gallery(context, Translation.BSB)
            val artwork = gallery.ordered(VerseArtCatalog.inCategory("hope", emptySet())).first()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                compose.waitUntil(15_000) {
                    runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
                }
                scenario.onActivity { ViewModelProvider(it)[HopeCardsViewModel::class.java].navigate(Destination.VERSE_ART) }
                compose.waitUntil(15_000) { runCatching { compose.onNodeWithTag("art-category-hope").assertIsDisplayed() }.isSuccess }
                compose.onNodeWithTag("art-category-hope").performClick()
                compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-${artwork.id}"))
                compose.onNodeWithTag("art-open-${artwork.id}").performClick()
                compose.onNodeWithTag("art-save-image").performScrollTo().performClick()
                compose.waitUntil(10_000) {
                    runCatching { compose.onNodeWithText("Image saved.").assertIsDisplayed() }.isSuccess
                }
                compose.onNodeWithText("A quick note").assertDoesNotExist()
                compose.onNodeWithText("Done").assertDoesNotExist()
                compose.onNodeWithTag("art-share").assertIsDisplayed()
                capture("image-saved-snackbar")
            }
        } finally { repository.replaceSettings(original) }
    }

    @Test fun notificationUsesSelectedVerseAndTapOpensDailyWithMusic(): Unit = runBlocking {
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        try {
            repository.updateSettings { it.copy(preferredTranslation = Translation.BSB, dailyHopeMusicEnabled = true) }
            val verses = VerseRepository(context)
            val daily = repository.dailyHopeVerse(verses, Translation.BSB)
            val scheduler = ReminderScheduler(context)
            scheduler.ensureChannel(Translation.BSB)
            for (edition in Translation.entries) {
                val verse = repository.dailyHopeVerse(verses, edition)
                assertEquals(daily.id, verse.id)
                val notification = scheduler.buildNotification(verse)
                assertEquals(verse.displayReference, notification.extras.getString(Notification.EXTRA_TITLE))
                assertEquals(verse.text, notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
                assertEquals(Notification.VISIBILITY_PUBLIC, notification.visibility)
                assertFalse(notification.extras.getBoolean(Notification.EXTRA_SHOW_WHEN))
                assertNotNull(notification.smallIcon)
                assertNull(notification.getLargeIcon())
            }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                compose.waitUntil(15_000) {
                    runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
                }
                lateinit var launchIntent: android.content.Intent
                scenario.onActivity { launchIntent = android.content.Intent(it.intent) }
                scheduler.showNotification(daily)
                assertTrue(context.getSystemService(NotificationManager::class.java).activeNotifications.isNotEmpty())
                // Even if the saved daily record changes before a tap, open the notified verse.
                val alternate = verses.random(Translation.BSB, daily.id)
                repository.setDailyHopeRecord(DailyHopeRecord(LocalDate.now().toString(), alternate.id, "bsb"))
                scheduler.buildNotification(daily).contentIntent.send()
                // Sending the PendingIntent directly does not perform SystemUI's auto-cancel.
                context.getSystemService(NotificationManager::class.java).cancel(2025)
                compose.waitUntil(15_000) {
                    var opened = false
                    scenario.onActivity {
                        val state = ViewModelProvider(it)[HopeCardsViewModel::class.java].uiState.value
                        opened = state.destination == Destination.DAILY && state.dailyVerse?.id == daily.id
                    }
                    opened
                }
                compose.waitUntil(10_000) { context.getSystemService(AudioManager::class.java).isMusicActive }
                compose.mainClock.advanceTimeBy(4_000)
                compose.waitForIdle()
                compose.onNodeWithText(daily.text).assertIsDisplayed()
                capture("daily-from-notification")
                repository.setDailyHopeRecord(DailyHopeRecord(LocalDate.now().toString(), daily.id, "bsb"))
                repository.updateSettings { it.copy(dailyHopeMusicEnabled = false) }
                compose.waitUntil(10_000) { !context.getSystemService(AudioManager::class.java).isMusicActive }
                scenario.onActivity { ViewModelProvider(it)[HopeCardsViewModel::class.java].navigate(Destination.HOME) }
                compose.waitUntil(10_000) { !context.getSystemService(AudioManager::class.java).isMusicActive }
                // ActivityScenario matches lifecycle events using its original launch intent;
                // MainActivity correctly replaces that intent in onNewIntent during the tap.
                scenario.onActivity { it.intent = launchIntent }
            }
        } finally { repository.replaceSettings(original) }
    }

    /** Opt-in screenshot capture on a connected test device; never runs as part of the suite. */
    @Test fun captureSystemNotificationSurfaces() = runBlocking {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("captureNotifications") == "true")
        val repository = AppRepository(context)
        repository.initialize()
        val settings = repository.currentSettings()
        val verse = repository.dailyHopeVerse(VerseRepository(context), settings.preferredTranslation)
        shell("input keyevent KEYCODE_HOME")
        android.os.SystemClock.sleep(700)
        ReminderScheduler(context).showNotification(verse)
        android.os.SystemClock.sleep(1_000)
        capture("unlocked-banner")
        shell("input keyevent KEYCODE_SLEEP")
        android.os.SystemClock.sleep(700)
        shell("input keyevent KEYCODE_WAKEUP")
        android.os.SystemClock.sleep(1_000)
        capture("lock-screen")
    }

    private fun shell(command: String) {
        instrumentation.uiAutomation.executeShellCommand(command).use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
        }
    }

    private fun capture(name: String) {
        val file = File(context.getExternalFilesDir(null), "notification-review/$name.png")
        file.parentFile!!.mkdirs()
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
