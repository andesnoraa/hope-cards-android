package com.aaronsedna.hopecards.ui

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.screens.BibleQuizScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class QuizLocalizationInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val englishContext get() = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply { setLocale(Locale.US) },
    )

    @Test fun everyBibleEditionLocalizesQuizResourcesWithoutChangingAppLanguage() {
        val english = englishContext
        Translation.entries.forEach { edition ->
            val expected = labelsFor(edition)
            val quiz = english.forQuizTranslation(edition)
            assertEquals("Title for ${edition.id}", expected.title, quiz.getString(R.string.nav_bible_quiz))
            assertEquals("Start for ${edition.id}", expected.start, quiz.getString(R.string.quiz_start))
            assertEquals("Sound for ${edition.id}", expected.sound, quiz.getString(R.string.quiz_enable_sound))
            assertEquals("Action for ${edition.id}", expected.check, quiz.getString(R.string.quiz_check))
            assertEquals("Share for ${edition.id}", expected.sharePdf, quiz.getString(R.string.quiz_share_results))
            if (edition !in englishEditions) {
                listOf(R.string.quiz_intro, R.string.quiz_correct_answer, R.string.quiz_your_answer,
                    R.string.quiz_review, R.string.quiz_save_pdf).forEach { id ->
                    assertNotEquals("Localized resource $id for ${edition.id}", english.getString(id), quiz.getString(id))
                }
            }
            assertEquals("The app language remains English", "Bible Quiz", english.forTranslation(edition).getString(R.string.nav_bible_quiz))
            assertEquals("The source context is unchanged", "Start quiz", english.getString(R.string.quiz_start))
        }

        val malayalamDevice = english.forQuizTranslation(Translation.MAL1910)
        englishEditions.forEach { edition ->
            assertEquals("English edition on a Malayalam device", "Start quiz",
                malayalamDevice.forQuizTranslation(edition).getString(R.string.quiz_start))
        }
    }

    @Test fun startOptionsAndActionFollowEverySelectedEditionOnAnEnglishDevice() {
        val english = englishContext
        val selected = mutableStateOf(Translation.BSB)
        val questions = Translation.entries.associateWith { edition ->
            BibleQuizRepository(english).load(QuizLanguage.forTranslation(edition)).take(1)
        }
        compose.setContent {
            CompositionLocalProvider(
                LocalContext provides english,
                LocalConfiguration provides english.resources.configuration,
            ) {
                HopeCardsTheme(ThemeName.CLASSIC) {
                    key(selected.value) { BibleQuizScreen(questions.getValue(selected.value), selected.value) }
                }
            }
        }

        Translation.entries.forEach { edition ->
            compose.runOnIdle { selected.value = edition }
            val expected = labelsFor(edition)
            compose.onNodeWithText(expected.sound).assertExists()
            compose.onNodeWithTag("quiz_start").assertTextEquals(expected.start).performClick()
            compose.onNodeWithTag("quiz_action").assertTextEquals(expected.check)
            compose.onNodeWithText(questions.getValue(edition).single().options.first()).assertExists()
        }
    }

    private data class Labels(val title: String, val start: String, val sound: String, val check: String, val sharePdf: String)

    private val englishEditions = setOf(Translation.BSB, Translation.BBE, Translation.KJV, Translation.WEB)

    private fun labelsFor(edition: Translation) = when (edition) {
        Translation.BSB, Translation.BBE, Translation.KJV, Translation.WEB ->
            Labels("Bible Quiz", "Start quiz", "Enable sound", "Check answer", "Share results PDF")
        Translation.MAL1910 ->
            Labels("ബൈബിൾ ക്വിസ്", "ആരംഭിക്കാം", "ശബ്ദം പ്രവർത്തനക്ഷമമാക്കുക", "ഉത്തരം പരിശോധിക്കുക", "ഫലങ്ങളുടെ PDF പങ്കിടുക")
        Translation.LUT1912 ->
            Labels("Bibelquiz", "Quiz starten", "Ton aktivieren", "Antwort prüfen", "Ergebnis als PDF teilen")
        Translation.LSG1910 ->
            Labels("Quiz biblique", "Commencer", "Activer le son", "Vérifier la réponse", "Partager les résultats en PDF")
        Translation.RIV1927 ->
            Labels("Quiz biblico", "Inizia il quiz", "Attiva audio", "Verifica la risposta", "Condividi i risultati in PDF")
        Translation.RV1909 ->
            Labels("Quiz bíblico", "Comenzar", "Activar sonido", "Comprobar respuesta", "Compartir resultados en PDF")
        Translation.ADB1905 ->
            Labels("Pagsusulit sa Bibliya", "Magsimula", "Paganahin ang tunog", "Suriin ang sagot", "Ibahagi ang mga resulta bilang PDF")
    }
}
