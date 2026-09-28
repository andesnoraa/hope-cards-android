package com.aaronsedna.hopecards.ui

import android.graphics.Bitmap
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.screens.QuizCertificateScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.io.File
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test

class QuizCertificateScreenTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private var previousEnglish: Boolean? = null
    private var previousLanguage: String? = null
    @Before fun resetLanguagePreference() {
        val preferences = context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE)
        previousEnglish = if (preferences.contains("certificate_english")) preferences.getBoolean("certificate_english", false) else null
        previousLanguage = preferences.getString("certificate_language", null)
        preferences.edit().remove("certificate_english").remove("certificate_language").commit()
    }
    @After fun restoreLanguagePreference() {
        val editor = context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE).edit()
        previousEnglish?.let { editor.putBoolean("certificate_english", it) } ?: editor.remove("certificate_english")
        previousLanguage?.let { editor.putString("certificate_language", it) } ?: editor.remove("certificate_language")
        editor.commit()
    }
    private fun node(tag: String) = compose.onNodeWithTag(tag)
    private fun field(tag: String) = node(tag).performScrollTo()
    private fun input(tag: String, value: String) = field(tag).performTextReplacement(value)
    private fun assertInput(tag: String, value: String) {
        assertEquals(value, field(tag).fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
    }
    private fun assertHint(tag: String, id: Int) = compose.onNodeWithTag("${tag}_hint", useUnmergedTree = true).assertTextEquals(context.getString(id))

    @Test fun validatesRequiredFieldsAndLimitsWithoutTruncatingInput() {
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.BSB) {} }
        }
        assertLanguage(QuizLanguage.ENGLISH)
        field("certificate_organizer").assertTextContains(context.getString(R.string.quiz_certificate_organizer_label))
        node("certificate_generate").assertIsDisplayed().performClick()
        field("certificate_participants")
        assertHint("certificate_participants", R.string.quiz_certificate_error_required)
        input("certificate_participants", "A".repeat(101))
        assertHint("certificate_participants", R.string.quiz_certificate_error_name_length)
        assertInput("certificate_participants", "A".repeat(101))
        val manyNames = (1..51).joinToString("\n") { "Participant $it" }
        input("certificate_participants", manyNames)
        assertHint("certificate_participants", R.string.quiz_certificate_error_count)
        assertInput("certificate_participants", manyNames)
        input("certificate_participants", "Aaron\nSarah")
        input("certificate_competition", "")
        assertHint("certificate_competition", R.string.quiz_certificate_error_required)
        input("certificate_competition", "C".repeat(121))
        assertHint("certificate_competition", R.string.quiz_certificate_error_title_length)
        input("certificate_competition", "Sunday School Bible Quiz")
        input("certificate_organizer", "O".repeat(121))
        assertHint("certificate_organizer", R.string.quiz_certificate_error_organizer_length)
        node("certificate_generate").assertIsDisplayed().performClick()
        node("certificate_ready").assertDoesNotExist()
    }

    @Test fun draftAndSeparatePdfsForRepeatedParticipantNamesSurviveRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.BSB) {} }
        }
        input("certificate_competition", "Sunday School Bible Quiz")
        val selectedDate = selectDate(15)
        input("certificate_organizer", "St. Thomas Sunday School")
        // Spreadsheet tabs become a single space for export, but the original draft survives.
        // Repeated names may be different people; blank separator lines are ignored.
        val people = "Aaron\t\tJoseph\n\nAaron Joseph"
        input("certificate_participants", people)
        restoration.emulateSavedInstanceStateRestore()
        assertInput("certificate_competition", "Sunday School Bible Quiz")
        assertDate(selectedDate, QuizLanguage.ENGLISH)
        assertInput("certificate_organizer", "St. Thomas Sunday School")
        assertInput("certificate_participants", people)
        node("certificate_generate").assertIsDisplayed().performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("certificate_ready").fetchSemanticsNodes().isNotEmpty() }
        node("certificate_person_0").assertExists()
        node("certificate_person_1").assertExists()
        node("certificate_person_2").assertDoesNotExist()
        compose.onAllNodesWithText("Aaron Joseph").assertCountEquals(2)
        node("certificate_share_0").assertExists().assertHasClickAction()
        node("certificate_share_1").assertExists().assertHasClickAction()
        node("certificate_share_all").assertIsDisplayed().assertIsEnabled()
        val expectedMessage = "Generated using Hope Cards App\n\nInstall Hope Cards on Google Play\n" +
            "https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"
        field("certificate_sharing_message").assertIsDisplayed().assertTextEquals(expectedMessage)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val previousClip = compose.runOnIdle { clipboard.primaryClip }
        try {
            field("certificate_copy_message").assertTextEquals(context.getString(R.string.quiz_certificate_share_copy))
                .assertIsDisplayed().performClick()
            node("certificate_copy_message").assertTextEquals(context.getString(R.string.quiz_certificate_share_copied))
            compose.runOnIdle {
                val copied = requireNotNull(clipboard.primaryClip)
                assertEquals(1, copied.itemCount)
                assertEquals(expectedMessage, copied.getItemAt(0).text.toString())
            }
        } finally {
            compose.runOnIdle {
                when {
                    previousClip != null -> clipboard.setPrimaryClip(previousClip)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> clipboard.clearPrimaryClip()
                    else -> clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            }
        }
        restoration.emulateSavedInstanceStateRestore()
        node("certificate_person_0").assertExists()
        node("certificate_person_1").assertExists()
        node("certificate_share_all").assertIsDisplayed().assertIsEnabled()
        field("certificate_sharing_message").assertIsDisplayed().assertTextEquals(expectedMessage)
        capture("certificate-ready.png")
        node("certificate_edit").performClick()
        assertInput("certificate_competition", "Sunday School Bible Quiz")
        assertInput("certificate_participants", people)
        node("certificate_generate").assertIsDisplayed()
        capture("certificate-form.png")
    }

    @Test fun malayalamFormUsesSelectedBibleLanguageAndKeepsNamesAfterRestoration() {
        val restoration = StateRestorationTester(compose)
        val translated = context.forQuizTranslation(Translation.MAL1910).resources
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.MAL1910) {} }
        }
        node("certificate_generate").assertTextEquals(translated.getString(R.string.quiz_certificate_generate))
        assertLanguage(QuizLanguage.MALAYALAM)
        assertInput("certificate_competition", translated.getString(R.string.quiz_certificate_competition_default))
        input("certificate_participants", "ആരോൺ ജോസഫ്\nസാറ മേരി")
        restoration.emulateSavedInstanceStateRestore()
        assertInput("certificate_participants", "ആരോൺ ജോസഫ്\nസാറ മേരി")
        node("certificate_generate").assertIsDisplayed()
        capture("certificate-form-malayalam.png")
    }

    @Test fun allSevenCertificateLanguagesAreAvailableEvenWithAnEnglishBibleAndPersistOnReopen() {
        val visible = mutableStateOf(true)
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                if (visible.value) QuizCertificateScreen(Translation.BSB) { visible.value = false }
            }
        }
        openLanguagePicker()
        QuizLanguage.entries.forEach { node("certificate_language_${it.code}").assertExists() }
        node("certificate_language_en").assertIsSelected()
        capture("certificate-language-picker-english.png")
        node("certificate_language_cancel").performClick()
        assertLanguage(QuizLanguage.ENGLISH)
        QuizLanguage.entries.forEach { language ->
            selectLanguage(language)
            assertLanguage(language)
            val translation = translationFor(language)
            assertInput("certificate_competition", context.forQuizTranslation(translation)
                .getString(R.string.quiz_certificate_competition_default))
            assertEquals(language.code, context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE)
                .getString("certificate_language", null))
        }
        node("certificate_close").performClick()
        compose.runOnIdle { visible.value = true }
        assertLanguage(QuizLanguage.TAGALOG)
        node("certificate_generate").assertTextEquals(context.getString(R.string.quiz_certificate_generate))
    }

    @Test fun certificateLanguageRelocalizesDefaultsAndPreservesCustomDetailsAcrossRestorationAndReopen() {
        val translated = context.forQuizTranslation(Translation.MAL1910).resources
        val english = context.forQuizTranslation(Translation.BSB).resources
        val visible = mutableStateOf(true)
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                if (visible.value) QuizCertificateScreen(Translation.MAL1910) { visible.value = false }
            }
        }
        assertLanguage(QuizLanguage.MALAYALAM)
        selectLanguage(QuizLanguage.ENGLISH)
        assertInput("certificate_competition", english.getString(R.string.quiz_certificate_competition_default))
        val selectedDate = selectDate(15, QuizLanguage.MALAYALAM)
        assertDate(selectedDate, QuizLanguage.ENGLISH)
        selectLanguage(QuizLanguage.MALAYALAM)
        assertInput("certificate_competition", translated.getString(R.string.quiz_certificate_competition_default))
        assertDate(selectedDate, QuizLanguage.MALAYALAM)
        input("certificate_competition", "സൺഡേ സ്കൂൾ ബൈബിൾ ക്വിസ്")
        input("certificate_organizer", "St. Thomas Sunday School")
        input("certificate_participants", "ആരോൺ ജോസഫ്\nSarah Mary")
        selectLanguage(QuizLanguage.ENGLISH)
        assertInput("certificate_competition", "സൺഡേ സ്കൂൾ ബൈബിൾ ക്വിസ്")
        assertDate(selectedDate, QuizLanguage.ENGLISH)
        assertInput("certificate_organizer", "St. Thomas Sunday School")
        assertInput("certificate_participants", "ആരോൺ ജോസഫ്\nSarah Mary")
        restoration.emulateSavedInstanceStateRestore()
        assertLanguage(QuizLanguage.ENGLISH)
        assertInput("certificate_competition", "സൺഡേ സ്കൂൾ ബൈബിൾ ക്വിസ്")
        assertDate(selectedDate, QuizLanguage.ENGLISH)
        assertInput("certificate_organizer", "St. Thomas Sunday School")
        assertInput("certificate_participants", "ആരോൺ ജോസഫ്\nSarah Mary")
        node("certificate_close").performClick()
        compose.runOnIdle { visible.value = true }
        assertLanguage(QuizLanguage.ENGLISH)
        assertInput("certificate_competition", english.getString(R.string.quiz_certificate_competition_default))
        // The UI continues to follow the Bible language, even while the PDF language is English.
        node("certificate_generate").assertTextEquals(translated.getString(R.string.quiz_certificate_generate))
    }

    @Test fun calendarSelectionCommitsOnlyOnConfirmAndConfirmedDateSurvivesRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.MAL1910) {} }
        }
        val confirmedDate = selectDate(15, QuizLanguage.MALAYALAM)
        assertDate(confirmedDate, QuizLanguage.MALAYALAM)
        openDatePicker()
        selectCalendarDay(16, QuizLanguage.MALAYALAM)
        capture("certificate-date-picker-malayalam.png")
        node("certificate_date_cancel").performClick()
        node("certificate_date_picker").assertDoesNotExist()
        assertDate(confirmedDate, QuizLanguage.MALAYALAM)
        restoration.emulateSavedInstanceStateRestore()
        assertDate(confirmedDate, QuizLanguage.MALAYALAM)
        openDatePicker()
        selectCalendarDay(16, QuizLanguage.MALAYALAM)
        node("certificate_date_confirm").performClick()
        assertDate(currentMonthDate(16), QuizLanguage.MALAYALAM)
        selectLanguage(QuizLanguage.FRENCH)
        assertDate(currentMonthDate(16), QuizLanguage.FRENCH)
    }

    @Test fun previousEnglishCertificatePreferenceIsRespectedUntilANewLanguageIsChosen() {
        context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE).edit()
            .putBoolean("certificate_english", true).commit()
        compose.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.MAL1910) {} }
        }
        assertLanguage(QuizLanguage.ENGLISH)
        selectLanguage(QuizLanguage.GERMAN)
        assertLanguage(QuizLanguage.GERMAN)
        assertEquals("de", context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE)
            .getString("certificate_language", null))
    }

    @Test fun malayalamBibleGeneratesEnglishAndMalayalamCertificatesAndRestoresBatchLanguage() {
        val translated = context.forQuizTranslation(Translation.MAL1910).resources
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) { QuizCertificateScreen(Translation.MAL1910) {} }
        }
        selectLanguage(QuizLanguage.ENGLISH)
        input("certificate_participants", "ആരോൺ ജോസഫ്")
        input("certificate_organizer", "St. Thomas Sunday School")
        val firstFiles = certificateFiles().map { it.name }.toSet()
        node("certificate_generate").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("certificate_ready").fetchSemanticsNodes().isNotEmpty() }
        node("certificate_output_language").assertTextEquals(translated.getString(R.string.quiz_certificate_language_ready, "English"))
        copyGeneratedCertificate(firstFiles, "certificate-english-from-malayalam.pdf")
        restoration.emulateSavedInstanceStateRestore()
        node("certificate_output_language").assertTextEquals(translated.getString(R.string.quiz_certificate_language_ready, "English"))
        node("certificate_share_all").assertIsEnabled()
        capture("certificate-english-ready-malayalam-ui.png")
        node("certificate_edit").performClick()
        assertLanguage(QuizLanguage.ENGLISH)
        selectLanguage(QuizLanguage.MALAYALAM)
        input("certificate_organizer", "സെന്റ് തോമസ് സൺഡേ സ്കൂൾ")
        assertInput("certificate_competition", translated.getString(R.string.quiz_certificate_competition_default))
        assertInput("certificate_participants", "ആരോൺ ജോസഫ്")
        val secondFiles = certificateFiles().map { it.name }.toSet()
        node("certificate_generate").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithTag("certificate_ready").fetchSemanticsNodes().isNotEmpty() }
        node("certificate_output_language").assertTextEquals(translated.getString(R.string.quiz_certificate_language_ready, "മലയാളം"))
        copyGeneratedCertificate(secondFiles, "certificate-malayalam-from-malayalam.pdf")
        restoration.emulateSavedInstanceStateRestore()
        node("certificate_output_language").assertTextEquals(translated.getString(R.string.quiz_certificate_language_ready, "മലയാളം"))
    }

    private fun assertLanguage(language: QuizLanguage) {
        field("certificate_language")
        compose.onNodeWithTag("certificate_language_value", useUnmergedTree = true)
            .assertTextEquals(language.nativeName)
    }

    private fun openLanguagePicker() {
        field("certificate_language").performClick()
        node("certificate_language_picker").assertExists()
    }

    private fun selectLanguage(language: QuizLanguage) {
        openLanguagePicker()
        node("certificate_language_${language.code}").performScrollTo().performClick()
        node("certificate_language_picker").assertDoesNotExist()
    }

    private fun openDatePicker() {
        field("certificate_date").performClick()
        node("certificate_date_picker").assertExists()
    }

    private fun selectDate(day: Int, calendarLanguage: QuizLanguage = QuizLanguage.ENGLISH): Long {
        openDatePicker()
        selectCalendarDay(day, calendarLanguage)
        node("certificate_date_confirm").performClick()
        node("certificate_date_picker").assertDoesNotExist()
        return currentMonthDate(day)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun selectCalendarDay(day: Int, calendarLanguage: QuizLanguage) {
        // Material3 clears numeral Text semantics and gives the clickable day a verbose date.
        // Its public formatter keeps the exact locale-dependent ordering/punctuation in sync.
        val description = requireNotNull(DatePickerDefaults.dateFormatter().formatDate(
            currentMonthDate(day), Locale.forLanguageTag(calendarLanguage.code), forContentDescription = true))
        val matcher = (hasText(description, substring = true) or hasContentDescription(description, substring = true)) and
            hasClickAction() and hasAnyAncestor(hasTestTag("certificate_date_picker"))
        try {
            compose.onNode(matcher).performClick()
        } catch (failure: AssertionError) {
            throw AssertionError("Calendar day not found: $description\n" + node("certificate_date_picker").printToString(), failure)
        }
    }

    private fun currentMonthDate(day: Int): Long {
        val today = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), day)
        }.timeInMillis
    }

    private fun assertDate(utcMillis: Long, language: QuizLanguage) {
        val expected = DateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag(language.code)).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(utcMillis))
        field("certificate_date")
        compose.onNodeWithTag("certificate_date_value", useUnmergedTree = true).assertTextEquals(expected)
    }

    private fun translationFor(language: QuizLanguage) = when (language) {
        QuizLanguage.ENGLISH -> Translation.BSB
        QuizLanguage.MALAYALAM -> Translation.MAL1910
        QuizLanguage.GERMAN -> Translation.LUT1912
        QuizLanguage.FRENCH -> Translation.LSG1910
        QuizLanguage.ITALIAN -> Translation.RIV1927
        QuizLanguage.SPANISH -> Translation.RV1909
        QuizLanguage.TAGALOG -> Translation.ADB1905
    }

    private fun certificateFiles() = File(context.cacheDir, "quiz-certificates").listFiles().orEmpty().filter { it.extension == "pdf" }

    private fun copyGeneratedCertificate(previousFiles: Set<String>, name: String) {
        val file = certificateFiles().single { it.name !in previousFiles }
        val directory = File(context.getExternalFilesDir(null), "certificate-ui").apply { mkdirs() }
        file.copyTo(File(directory, name), overwrite = true)
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(context.getExternalFilesDir(null), "certificate-ui").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
