package com.aaronsedna.hopecards.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.data.VerseRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class CuratedDailyVersesInstrumentedTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun longestCardInEveryEditionDisplaysThroughTheDailyNotificationRoute(): Unit = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context)
        repository.initialize()
        val original = repository.currentSettings()
        val verses = VerseRepository(context)
        try {
            Translation.entries.forEach { edition ->
                repository.updateSettings { it.copy(preferredTranslation = edition, dailyHopeMusicEnabled = false) }
                val verse = verses.verses(edition).maxBy { it.text.length }
                val intent = Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_OPEN_DAILY_HOPE, true)
                    .putExtra(MainActivity.EXTRA_DAILY_VERSE_ID, verse.id)
                ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                    compose.waitUntil(15_000) {
                        var ready = false
                        scenario.onActivity {
                            val state = ViewModelProvider(it)[HopeCardsViewModel::class.java].uiState.value
                            ready = state.destination == Destination.DAILY && state.dailyVerse?.id == verse.id
                        }
                        ready
                    }
                    compose.mainClock.advanceTimeBy(4_000)
                    compose.waitForIdle()
                    compose.onNodeWithText(verse.text).assertIsDisplayed()
                    val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
                    val directory = File(context.getExternalFilesDir(null), "daily-verse-review").apply { mkdirs() }
                    File(directory, "${edition.id}.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
            }
        } finally {
            repository.replaceSettings(original)
        }
    }
}
