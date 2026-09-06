package com.aaronsedna.hopecards.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R

/** The Ionicons family used by the original Hope Cards app. */
private val Ionicons = FontFamily(Font(R.font.ionicons))

enum class AppIconGlyph(val codePoint: Int) {
    Menu(0xF451),
    ArrowBack(0xF127),
    HomeOutline(0xF383),
    SunnyOutline(0xF5AE),
    HeartOutline(0xF377),
    Heart(0xF36A),
    JournalOutline(0xF3A1),
    SparklesOutline(0xF58D),
    SettingsOutline(0xF56C),
    ShieldCheckmarkOutline(0xF579),
    InformationCircleOutline(0xF399),
    ChevronForward(0xF23B),
    ChevronUp(0xF241),
    ChevronDown(0xF232),
    Checkmark(0xF21D),
    CheckmarkCircleOutline(0xF21F),
    CloudUploadOutline(0xF260),
    RefreshOutline(0xF518),
    NotificationsOutline(0xF47F),
    HandLeftOutline(0xF35C),
    EyeOffOutline(0xF2E8),
    CreateOutline(0xF293),
    TrashOutline(0xF5F6),
    Close(0xF24A),
    ShareOutline(0xF572),
}

@Composable
fun AppIcon(
    glyph: AppIconGlyph,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val semantics = if (contentDescription == null) {
        Modifier
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }
    Box(
        modifier = modifier.then(semantics).size(size),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = String(Character.toChars(glyph.codePoint)),
            color = tint,
            fontFamily = Ionicons,
            fontSize = size.value.sp,
            lineHeight = size.value.sp,
            maxLines = 1,
        )
    }
}
