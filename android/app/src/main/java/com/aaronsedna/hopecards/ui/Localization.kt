package com.aaronsedna.hopecards.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.R
import java.util.Locale

val LocalAppTranslation = staticCompositionLocalOf { Translation.BSB }

@Composable
fun ProvideAppTranslation(translation: Translation, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppTranslation provides translation, content = content)
}

val Translation.localeTag: String
    get() = when (this) {
        Translation.MAL1910 -> "ml-IN"
        Translation.RV1909 -> "es-419"
        Translation.LSG1910 -> "fr-FR"
        Translation.LUT1912 -> "de-DE"
        Translation.RIV1927 -> "it-IT"
        Translation.ADB1905 -> "fil-PH"
        else -> "en-US"
    }

fun Context.forTranslation(translation: Translation): Context {
    val configuration = Configuration(resources.configuration)
    // Bible editions can change the verse text and book reference presentation, but the
    // product UI stays in English. Keeping this context explicit also avoids a selected
    // Malayalam/German/etc. Bible from unexpectedly changing navigation and settings labels.
    configuration.setLocale(Locale.US)
    configuration.setLayoutDirection(Locale.US)
    return createConfigurationContext(configuration)
}

@Composable
fun appString(@StringRes id: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val translation = LocalAppTranslation.current
    return context.forTranslation(translation).resources.getString(id, *formatArgs)
}

@Composable
fun appQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val translation = LocalAppTranslation.current
    return context.forTranslation(translation).resources.getQuantityString(id, quantity, *formatArgs)
}

@Composable
fun categoryLabel(category: String): String = appString(
    when (category.lowercase()) {
        "comfort" -> R.string.category_comfort
        "courage" -> R.string.category_courage
        "faith" -> R.string.category_faith
        "freedom" -> R.string.category_freedom
        "grace" -> R.string.category_grace
        "hope" -> R.string.category_hope
        "joy" -> R.string.category_joy
        "life" -> R.string.category_life
        "love" -> R.string.category_love
        "peace" -> R.string.category_peace
        "prayer" -> R.string.category_prayer
        "strength" -> R.string.category_strength
        "trust" -> R.string.category_trust
        "wisdom" -> R.string.category_wisdom
        else -> R.string.category_hope
    },
)

@Composable
fun reflectionPrompt(category: String): String = appString(
    when (category.lowercase()) {
        "comfort" -> R.string.reflection_comfort
        "courage" -> R.string.reflection_courage
        "faith" -> R.string.reflection_faith
        "freedom" -> R.string.reflection_freedom
        "grace" -> R.string.reflection_grace
        "hope" -> R.string.reflection_hope
        "joy" -> R.string.reflection_joy
        "life" -> R.string.reflection_life
        "love" -> R.string.reflection_love
        "peace" -> R.string.reflection_peace
        "prayer" -> R.string.reflection_prayer
        "strength" -> R.string.reflection_strength
        "trust" -> R.string.reflection_trust
        "wisdom" -> R.string.reflection_wisdom
        else -> R.string.reflection_default
    },
)

@Composable
fun themeLabel(theme: ThemeName): String = appString(when (theme) {
    ThemeName.CLASSIC -> R.string.theme_classic
    ThemeName.ROSE_DAWN -> R.string.theme_rose
    ThemeName.OLIVE_GROVE -> R.string.theme_olive
    ThemeName.SERENITY -> R.string.theme_evergreen
    ThemeName.STILL_WATER -> R.string.theme_water
    ThemeName.MIDNIGHT -> R.string.theme_midnight
    ThemeName.COASTAL_LINEN -> R.string.theme_coastal
    ThemeName.QUIET_LAVENDER -> R.string.theme_lavender
    ThemeName.VINTAGE_HERITAGE -> R.string.theme_vintage
})

@Composable
fun themeDescription(theme: ThemeName): String = appString(when (theme) {
    ThemeName.CLASSIC -> R.string.theme_classic_desc
    ThemeName.ROSE_DAWN -> R.string.theme_rose_desc
    ThemeName.OLIVE_GROVE -> R.string.theme_olive_desc
    ThemeName.SERENITY -> R.string.theme_evergreen_desc
    ThemeName.STILL_WATER -> R.string.theme_water_desc
    ThemeName.MIDNIGHT -> R.string.theme_midnight_desc
    ThemeName.COASTAL_LINEN -> R.string.theme_coastal_desc
    ThemeName.QUIET_LAVENDER -> R.string.theme_lavender_desc
    ThemeName.VINTAGE_HERITAGE -> R.string.theme_vintage_desc
})

@Composable
fun languageLabel(language: String): String = appString(when (language) {
    "Malayalam" -> R.string.language_malayalam
    "Spanish" -> R.string.language_spanish
    "French" -> R.string.language_french
    "German" -> R.string.language_german
    "Italian" -> R.string.language_italian
    "Tagalog" -> R.string.language_tagalog
    else -> R.string.language_english
})
