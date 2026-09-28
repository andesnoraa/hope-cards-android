package com.aaronsedna.hopecards.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.Translation
import java.util.Locale

/** The quiz follows the selected Bible language without changing the rest of the app. */
fun Context.forQuizTranslation(translation: Translation): Context =
    createConfigurationContext(quizConfiguration(resources.configuration, translation))

@Composable
fun quizString(translation: Translation, @StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val resources = remember(context, configuration, translation) {
        context.createConfigurationContext(quizConfiguration(configuration, translation)).resources
    }
    return resources.getString(id, *args)
}

private fun quizConfiguration(configuration: Configuration, translation: Translation): Configuration =
    Configuration(configuration).apply {
        val locale = Locale.forLanguageTag(QuizLanguage.forTranslation(translation).code)
        setLocale(locale)
        setLayoutDirection(locale)
    }
