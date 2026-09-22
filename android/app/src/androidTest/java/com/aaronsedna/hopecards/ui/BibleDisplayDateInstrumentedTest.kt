package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.screens.DailyHopeScreen
import com.aaronsedna.hopecards.ui.screens.DailySharePresentation
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BibleDisplayDateInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun datesFollowBibleLanguageEvenWhenDeviceIsEnglish() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val expected = mapOf(
                Translation.BSB to "Tuesday • 8 September",
                Translation.BBE to "Tuesday • 8 September",
                Translation.KJV to "Tuesday • 8 September",
                Translation.WEB to "Tuesday • 8 September",
                Translation.LUT1912 to "Dienstag • 8. September",
                Translation.LSG1910 to "mardi • 8 septembre",
                Translation.RIV1927 to "martedì • 8 settembre",
                Translation.RV1909 to "martes • 8 de septiembre",
                Translation.ADB1905 to "Martes • Setyembre 8",
                Translation.MAL1910 to "സെപ്റ്റംബർ 8 • ചൊവ്വാഴ്ച",
            )
            expected.forEach { (translation, text) ->
                // ICU versions vary in capitalization and Malayalam shaping controls.
                // Check the localized words, date, punctuation and order across devices.
                val actual = BibleDisplayDateFormatter.format(LocalDate.of(2026, 9, 8), translation)
                assertEquals(translation.id, text.lowercase(Locale.ROOT), actual.replace("\u200c", "").lowercase(Locale.ROOT))
            }
        } finally { Locale.setDefault(original) }
    }

    @Test fun dailyScreenAndSharedCardRefreshDateWhenBibleEditionChanges() {
        val edition = mutableStateOf(Translation.MAL1910)
        val share = mutableStateOf(false)
        compose.setContent {
            val verse = Verse("test", "hope", "ദൈവം സ്നേഹം ആകുന്നു.", "1 John 4:8", edition.value.label, emptyList(), edition.value)
            HopeCardsTheme(ThemeName.CLASSIC) {
                ProvideAppTranslation(edition.value) {
                    if (share.value) DailySharePresentation(verse)
                    else DailyHopeScreen(verse, AppSettings(dailyHopeMusicEnabled = false), false, "", {}, {}, {}, {})
                }
            }
        }
        compose.mainClock.advanceTimeBy(5_000)
        compose.onNodeWithText("ദൈവവചനം").assertIsDisplayed()
        compose.onNodeWithText(BibleDisplayDateFormatter.format(LocalDate.now(), Translation.MAL1910)).assertIsDisplayed()
        capture("malayalam-daily.png")
        compose.runOnIdle { share.value = true }
        compose.onNodeWithText("ദൈവവചനം").assertIsDisplayed()
        compose.onNodeWithText(BibleDisplayDateFormatter.format(LocalDate.now(), Translation.MAL1910)).assertIsDisplayed()
        capture("malayalam-share.png")
        compose.runOnIdle { edition.value = Translation.LSG1910 }
        compose.onNodeWithText(BibleDisplayDateFormatter.format(LocalDate.now(), Translation.LSG1910)).assertIsDisplayed()
        compose.runOnIdle { share.value = false; edition.value = Translation.BSB }
        compose.mainClock.advanceTimeBy(5_000)
        compose.onNodeWithText("Today’s Hope").assertIsDisplayed()
        compose.onNodeWithText(BibleDisplayDateFormatter.format(LocalDate.now(), Translation.BSB)).assertIsDisplayed()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "date-regression/$name")
        file.parentFile!!.mkdirs()
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
