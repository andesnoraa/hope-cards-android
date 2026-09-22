package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.Destination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class ReminderTimeInputTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun tappingReplacesNumbersAndInvalidTimesCannotBeSaved(): Unit = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val repository = AppRepository(context).apply { initialize() }
        val original = repository.currentSettings()
        try {
            repository.updateSettings { it.copy(dailyHopeReminderHour = 6, dailyHopeReminderMinute = 0) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                compose.waitUntil(15_000) { runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess }
                scenario.onActivity {
                    val vm = ViewModelProvider(it)[HopeCardsViewModel::class.java]
                    vm.ads.close()
                    vm.navigate(Destination.SETTINGS)
                }
                compose.onNodeWithText("Reminder Time").performScrollTo().performClick()
                val hour = compose.onNodeWithTag("reminder_hour")
                val minute = compose.onNodeWithTag("reminder_minute")
                fun select(field: SemanticsNodeInteraction, text: String) {
                    field.performTouchInput { click() }
                    compose.waitForIdle()
                    field.assertTextEquals(text)
                    assertEquals(TextRange(0, text.length), field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
                }
                select(hour, "06")
                hour.performTextInput("1"); hour.performTextInput("2")
                hour.assertTextEquals("12")
                hour.performImeAction()
                compose.waitForIdle()
                assertEquals(TextRange(0, 2), minute.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
                minute.performTextInput("3"); minute.performTextInput("5")
                minute.assertTextEquals("35")
                select(minute, "35")
                minute.performTextInput("99")
                minute.assertTextEquals("35")
                minute.performTextInput("123")
                minute.assertTextEquals("35")
                minute.performTextInput("ab")
                minute.assertTextEquals("35")
                minute.performTextClearance()
                compose.onNodeWithTag("reminder_time_save").assertIsNotEnabled()
                minute.performTextInput("00")
                select(hour, "12")
                hour.performTextInput("00")
                compose.onNodeWithTag("reminder_time_save").assertIsNotEnabled()
                select(hour, "00")
                hour.performTextInput("09")
                // Reselect an already focused field, then type without clearing it explicitly.
                select(hour, "09")
                hour.performTextInput("12")
                hour.assertTextEquals("12")
                scenario.recreate()
                compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag("reminder_hour").assertTextEquals("12") }.isSuccess }
                compose.onNodeWithTag("reminder_minute").assertTextEquals("00")
                select(compose.onNodeWithTag("reminder_minute"), "00")
                compose.onNodeWithTag("reminder_minute").performTextInput("45")
                compose.onNodeWithTag("reminder_time_save").assertIsDisplayed().assertIsEnabled()
                val out = File(context.getExternalFilesDir(null), "time-input-fix").apply { mkdirs() }
                instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
                    File(out, "after.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                }
                compose.onNodeWithTag("reminder_minute").performImeAction()
                compose.onNodeWithTag("reminder_time_save").performClick()
                compose.waitUntil(10_000) { runBlocking { repository.currentSettings().let { it.dailyHopeReminderHour == 0 && it.dailyHopeReminderMinute == 45 } } }
                compose.onNodeWithText("Reminder Time").performScrollTo().performClick()
                compose.onNodeWithTag("reminder_hour").assertTextEquals("12")
                compose.onNodeWithTag("reminder_minute").assertTextEquals("45")
                compose.onNodeWithContentDescription("Increase reminder hour").performClick()
                compose.onNodeWithTag("reminder_hour").assertTextEquals("01")
                compose.onNodeWithText("Cancel").performClick()
                assertEquals(0, repository.currentSettings().dailyHopeReminderHour)
            }
        } finally { repository.replaceSettings(original) }
    }
}
