package com.aaronsedna.hopecards.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.ThemeName

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)
val SourceSerif = FontFamily(
    Font(R.font.source_serif_regular, FontWeight.Normal),
    Font(R.font.source_serif_semibold, FontWeight.SemiBold),
)

@Immutable
data class HopeColors(
    val background: Color,
    val homeBackground: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentLine: Color,
    val divider: Color,
    val cardFront: Color,
    val cardBack: Color,
    val cardBackText: Color,
    val cardBackAccent: Color,
    val cardText: Color,
    val cardMuted: Color,
    val buttonBackground: Color,
    val buttonBorder: Color,
    val buttonText: Color,
    val danger: Color,
    val switchOff: Color,
    val drawerActiveBackground: Color,
)

private fun color(hex: Int) = Color(0xFF000000.toInt() or hex)

val ClassicHopeColors = HopeColors(color(0xF8F6F2), color(0xF7F5F1), Color.White, color(0x1A2747), color(0x777777), color(0x8C93A3), color(0xC89B3C), color(0xF5EAC8), color(0xE7D7B2), color(0xE7E2D8), color(0xF8F6F2), color(0x1A2747), color(0xF8F6F2), color(0xC89B3C), color(0x273043), color(0x7A8292), color(0x1A2747), color(0xC89B3C), Color.White, color(0xC0392B), color(0xD9D9D9), color(0xF5EAC8))

private val themes = mapOf(
    ThemeName.CLASSIC to ClassicHopeColors,
    ThemeName.ROSE_DAWN to HopeColors(color(0xFAF3F1), color(0xF8EFEC), Color.White, color(0x35213A), color(0x7F6D75), color(0x9B8B92), color(0xB96F73), color(0xF3DCDD), color(0xE5BFC0), color(0xE9DAD7), color(0xFFF9F6), color(0x6E2E42), color(0xFFF8F3), color(0xE2A67F), color(0x34293A), color(0x82717B), color(0x6E2E42), color(0xE2A67F), Color.White, color(0xB84A4A), color(0xDDD0CE), color(0xF3DCDD)),
    ThemeName.OLIVE_GROVE to HopeColors(color(0xF5F4EA), color(0xF1F1E4), Color.White, color(0x263126), color(0x6F7464), color(0x8C917F), color(0x8C7A33), color(0xE8E2C3), color(0xCDC28C), color(0xDFDDCD), color(0xFCFAF0), color(0x35482F), color(0xFFFDF0), color(0xC4A64B), color(0x283024), color(0x6F7464), color(0x35482F), color(0xC4A64B), Color.White, color(0xB84A4A), color(0xD8D8CB), color(0xE8E2C3)),
    ThemeName.SERENITY to HopeColors(color(0xFCFBF7), color(0xFAF9F5), Color.White, color(0x183B32), color(0x64736C), color(0x89948F), color(0x1B5142), color(0xEEF3EC), color(0xD4DED8), color(0xE0E5E1), color(0xFCFBF7), color(0x183B32), color(0xDCC580), color(0xC5A35A), color(0x183B32), color(0x717C77), color(0x183B32), color(0xC5A35A), color(0xE8D28F), color(0xA9433A), color(0xD5DBD7), color(0xE8F0EB)),
    ThemeName.STILL_WATER to HopeColors(color(0xF4F8FA), color(0xF0F6F9), Color.White, color(0x1F2D3A), color(0x6B7A86), color(0x8A98A3), color(0x5F9EC1), color(0xDDEEF6), color(0xBBD8E7), color(0xD6E0E5), color(0xFAFCFD), color(0x234D63), color(0xF8FCFD), color(0x83C3E2), color(0x1F2D3A), color(0x6B7A86), color(0x234D63), color(0x83C3E2), Color.White, color(0xB84A4A), color(0xD2DADE), color(0xD8E8EF)),
    ThemeName.MIDNIGHT to HopeColors(color(0xF7F4EE), color(0xF3EFE7), Color.White, color(0x1B1A18), color(0x726B61), color(0x91887B), color(0xB98F3B), color(0xEFE2C5), color(0xD9C28D), color(0xE5DED2), color(0xFBF8F1), color(0x121212), color(0xF7F1E5), color(0xC9A24F), color(0x25221D), color(0x7A7165), color(0x121212), color(0xC9A24F), Color.White, color(0xB84A4A), color(0xD7D1C7), color(0xEFE2C5)),
    ThemeName.COASTAL_LINEN to HopeColors(color(0xF4F7F5), color(0xEDF4F2), color(0xFDFEFD), color(0x173B3B), color(0x657977), color(0x849391), color(0xC07D52), color(0xF4E4D8), color(0xDFC4B2), color(0xDCE5E2), color(0xFAF7EF), color(0x164E52), color(0xF7F0DF), color(0xD6A477), color(0x234443), color(0x70817C), color(0x164E52), color(0xD6A477), Color.White, color(0xB84A4A), color(0xD4DEDB), color(0xE8F0ED)),
    ThemeName.QUIET_LAVENDER to HopeColors(color(0xF8F5FA), color(0xF3EEF6), color(0xFFFDFE), color(0x392B45), color(0x776B7E), color(0x968B9C), color(0x8D6A9F), color(0xECE1F1), color(0xD6C1DF), color(0xE5DDE9), color(0xFCF8FF), color(0x4C365A), color(0xFBF1FF), color(0xC5A4D0), color(0x3D3146), color(0x7C6F83), color(0x4C365A), color(0xC5A4D0), Color.White, color(0xB84A4A), color(0xDBD3DF), color(0xEDE4F1)),
    ThemeName.VINTAGE_HERITAGE to HopeColors(color(0xF8F1E3), color(0xF4E9D7), color(0xFFF9EC), color(0x3A2921), color(0x756356), color(0x958377), color(0xB58A4A), color(0xEFE0BE), color(0xD6BC88), color(0xE1D1B6), color(0xF7EBD2), color(0x0B0B0A), color(0xD5B06A), color(0xB58A4A), color(0x442F24), color(0x806B5B), color(0x0B0B0A), color(0xB58A4A), color(0xE7C978), color(0xA64136), color(0xDACDBA), color(0xEEE0C5)),
)

val LocalHopeColors = staticCompositionLocalOf { themes.getValue(ThemeName.CLASSIC) }
val LocalHopeThemeName = staticCompositionLocalOf { ThemeName.CLASSIC }

@Composable
fun HopeCardsTheme(themeName: ThemeName, content: @Composable () -> Unit) {
    val colors = themes.getValue(themeName)
    val materialColors = lightColorScheme(
        primary = colors.accent,
        onPrimary = colors.buttonText,
        background = colors.background,
        onBackground = colors.text,
        surface = colors.surface,
        onSurface = colors.text,
        error = colors.danger,
        outline = colors.accentLine,
    )
    val typography = androidx.compose.material3.Typography(
        displayLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 56.sp, lineHeight = 62.sp),
        headlineLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
        headlineMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
        titleLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
        titleMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
        bodyLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
        bodyMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodySmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        labelMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelSmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    )
    androidx.compose.runtime.CompositionLocalProvider(
        LocalHopeColors provides colors,
        LocalHopeThemeName provides themeName,
    ) {
        MaterialTheme(colorScheme = materialColors, typography = typography, content = content)
    }
}
