package com.aaronsedna.hopecards.ui

import android.content.res.Configuration
import androidx.core.os.ConfigurationCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import com.aaronsedna.hopecards.ui.theme.NotoSansMalayalam
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class DeviceLanguageInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun device(tag: String) = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) },
    )

    @Test fun bibleChoiceDoesNotOverrideDeviceInterfaceLanguage() {
        listOf("de-DE", "fr-FR", "it-IT", "ml-IN", "es-419", "fil-PH").forEach { tag ->
            val device = device(tag)
            Translation.entries.forEach { edition ->
                val ui = device.forTranslation(edition)
                assertEquals(tag, Locale.forLanguageTag(tag).language, ConfigurationCompat.getLocales(ui.resources.configuration)[0]?.language)
                assertEquals("Navigation remains in device language ($tag, ${edition.id})",
                    device.getString(R.string.nav_settings), ui.getString(R.string.nav_settings))
                assertNotEquals("Translated navigation available ($tag)", "Settings", ui.getString(R.string.nav_settings))
                assertEquals("Hope Cards", ui.getString(R.string.app_name))
                assertEquals(ui.getString(R.string.draw_card_accessibility),
                    ui.resources.getStringArray(R.array.about_feature_list).first())
            }
        }
    }

    @Test fun portugueseGoogleExportPreservesLocaleAndReviewedControls() {
        val ui = device("pt-BR").forTranslation(Translation.MAL1910)
        assertEquals("pt", ConfigurationCompat.getLocales(ui.resources.configuration)[0]?.language)
        assertEquals("BR", ConfigurationCompat.getLocales(ui.resources.configuration)[0]?.country)
        assertEquals("Hope Cards", ui.getString(R.string.app_name))
        // Google-generated resources are retained locally for the same offline behavior.
        assertEquals("Configurações", ui.getString(R.string.nav_settings))
        assertEquals("Tirar uma carta", ui.getString(R.string.draw_a_card))
        assertEquals(ui.getString(R.string.draw_card_accessibility),
            ui.resources.getStringArray(R.array.about_feature_list).first())
    }

    @Test fun frenchInterfaceAndGermanQuizRenderTogether() {
        val french = device("fr-FR")
        compose.setContent {
            CompositionLocalProvider(LocalContext provides french,
                LocalConfiguration provides french.resources.configuration) {
                ProvideAppTranslation(Translation.LUT1912) {
                    HopeCardsTheme(ThemeName.CLASSIC) {
                        Text(appString(R.string.nav_settings))
                        Text(quizString(Translation.LUT1912, R.string.quiz_start))
                    }
                }
            }
        }
        compose.onNodeWithText("Réglages").assertExists()
        compose.onNodeWithText("Quiz starten").assertExists()
        assertEquals("Start quiz", french.forQuizTranslation(Translation.BSB).getString(R.string.quiz_start))
    }

    @Test fun malayalamInterfaceUsesItsBundledFontWithEnglishBible() {
        val malayalam = device("ml-IN")
        compose.setContent {
            CompositionLocalProvider(LocalContext provides malayalam,
                LocalConfiguration provides malayalam.resources.configuration) {
                ProvideAppTranslation(Translation.BSB) {
                    HopeCardsTheme(ThemeName.CLASSIC) {
                        assertEquals(NotoSansMalayalam, MaterialTheme.typography.bodyLarge.fontFamily)
                        Text(appString(R.string.nav_settings))
                    }
                }
            }
        }
        compose.onNodeWithText(malayalam.getString(R.string.nav_settings)).assertExists()
        assertEquals("Start quiz", malayalam.forQuizTranslation(Translation.BSB).getString(R.string.quiz_start))
    }
}
