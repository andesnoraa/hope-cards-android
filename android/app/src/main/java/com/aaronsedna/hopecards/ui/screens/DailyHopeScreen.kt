package com.aaronsedna.hopecards.ui.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.BibleDisplayDateFormatter
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.HopeCardsViewModel
import com.aaronsedna.hopecards.ui.components.ActionPill
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.components.changeTranslationOnLongPress
import com.aaronsedna.hopecards.ui.theme.ClassicHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import com.aaronsedna.hopecards.ui.theme.scriptureFontFor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHopeScreen(
    verse: Verse,
    settings: AppSettings,
    favorite: Boolean,
    existingNote: String,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onSaveReflection: (String) -> Unit,
    onChangeTranslation: () -> Unit,
) {
    val colors = ClassicHopeColors
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var note by remember(verse.id, existingNote) { mutableStateOf(existingNote) }
    var reflectionOpen by remember(verse.id) { mutableStateOf(false) }
    val reflectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var player by remember(verse.category) { mutableStateOf<MediaPlayer?>(null) }
    val headerAlpha = remember(verse.id) { Animatable(0f) }
    val verseAlpha = remember(verse.id) { Animatable(0f) }
    val verseOffset = remember(verse.id) { Animatable(18f) }
    val actionsAlpha = remember(verse.id) { Animatable(0f) }
    val translationAlpha = remember(verse.id) { Animatable(0f) }

    fun releasePlayer() {
        player?.runCatching { stop() }
        player?.runCatching { release() }
        player = null
    }

    fun startPlayer() {
        if (!settings.dailyHopeMusicEnabled || player != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        player = MediaPlayer.create(context, musicFor(verse.category), attributes, 0)?.apply {
            isLooping = true
            setVolume(.45f, .45f)
            start()
        }
    }

    LaunchedEffect(verse.id) {
        headerAlpha.animateTo(1f, tween(600))
    }
    LaunchedEffect(verse.id) {
        kotlinx.coroutines.delay(900)
        verseAlpha.animateTo(1f, tween(1000))
    }
    LaunchedEffect(verse.id) {
        kotlinx.coroutines.delay(900)
        verseOffset.animateTo(0f, tween(1000))
    }
    LaunchedEffect(verse.id) {
        kotlinx.coroutines.delay(1900)
        actionsAlpha.animateTo(1f, tween(600))
    }
    LaunchedEffect(verse.id) {
        kotlinx.coroutines.delay(2100)
        translationAlpha.animateTo(1f, tween(600))
    }
    DisposableEffect(lifecycle, verse.category, settings.dailyHopeMusicEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) releasePlayer()
            if (event == Lifecycle.Event.ON_RESUME) startPlayer()
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) startPlayer()
        onDispose {
            lifecycle.removeObserver(observer)
            releasePlayer()
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp).padding(top = 32.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.alpha(headerAlpha.value).padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AdaptiveDailyHopeTitle(verse)
            Text(
                BibleDisplayDateFormatter.format(LocalDate.now(), verse.edition),
                color = colors.textSecondary,
                fontFamily = interfaceFontFor(verse.edition),
                fontSize = 16.sp,
                letterSpacing = .4.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Column(
            Modifier.widthIn(max = 620.dp).fillMaxWidth(.82f).padding(top = 12.dp)
                .alpha(verseAlpha.value).offset { IntOffset(0, verseOffset.value.dp.roundToPx()) },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(verse.displayReference, color = colors.text, fontFamily = interfaceFontFor(verse.edition), fontWeight = FontWeight.Bold, fontSize = 28.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 14.dp))
            Box(Modifier.size(56.dp, 2.dp).background(colors.accent.copy(alpha = .75f), CircleShape))
            Text(
                verse.text,
                color = colors.cardText,
                fontFamily = scriptureFontFor(verse.edition),
                fontSize = dailyFontSize(verse.text.length),
                lineHeight = dailyLineHeight(verse.text.length),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 36.dp),
            )
        }
        Text(
            verse.translation,
            color = colors.textTertiary,
            fontFamily = interfaceFontFor(verse.edition),
            fontSize = 14.sp,
            letterSpacing = .5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 48.dp).alpha(translationAlpha.value)
                .changeTranslationOnLongPress(settings.enableHaptics, onChangeTranslation)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 60.dp).alpha(actionsAlpha.value),
        ) {
            ActionPill(
                if (favorite) "Saved" else "Save",
                onFavorite,
                favorite = favorite,
                accentColor = colors.accent,
                savedColor = colors.danger,
            )
            ActionPill("Share", onShare, accentColor = colors.accent)
        }
        TextButton(
            onClick = { reflectionOpen = true },
            modifier = Modifier.padding(top = 18.dp),
        ) {
            Text(
                if (existingNote.isBlank()) "Add to journal" else "Edit journal entry",
                color = colors.accent,
                fontFamily = interfaceFontFor(verse.edition),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }

    if (reflectionOpen) {
        val trimmedNote = note.trim()
        val noteChanged = trimmedNote != existingNote.trim()
        ModalBottomSheet(
            onDismissRequest = { reflectionOpen = false },
            sheetState = reflectionSheetState,
            containerColor = colors.surface,
            contentColor = colors.text,
            scrimColor = colors.text.copy(alpha = .32f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().widthIn(max = 620.dp).align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState()).imePadding()
                    .padding(horizontal = 24.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(38.dp), shape = CircleShape, color = colors.accentSoft) {
                        Box(contentAlignment = Alignment.Center) {
                            AppIcon(AppIconGlyph.CreateOutline, null, colors.accent, size = 20.dp)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            if (existingNote.isBlank()) "Add to journal" else "Edit journal entry",
                            color = colors.text,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                        Text("For ${verse.displayReference}", color = colors.textTertiary, fontFamily = Poppins, fontSize = 12.sp)
                    }
                    IconButton(onClick = { reflectionOpen = false }) {
                        AppIcon(AppIconGlyph.Close, "Close journal entry", colors.textSecondary, size = 22.dp)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "A SIMPLE THOUGHT",
                        color = colors.accent,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                    )
                    Text(
                        HopeCardsViewModel.prompts[verse.category.lowercase()]
                            ?: "Keep the words that feel meaningful to you today.",
                        color = colors.text,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 1_000) note = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Add a few words…", color = colors.textTertiary) },
                    shape = RoundedCornerShape(18.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SourceSerif, fontSize = 18.sp, lineHeight = 27.sp, color = colors.cardText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.accentLine,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                    ),
                )
                Row(Modifier.fillMaxWidth()) {
                    Text("Journal entry", color = colors.textTertiary, fontFamily = Poppins, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("${note.length} / 1,000", color = colors.textTertiary, fontFamily = Poppins, fontSize = 12.sp)
                }
                if (noteChanged) {
                    Button(
                        onClick = {
                            onSaveReflection(trimmedNote)
                            reflectionOpen = false
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.buttonBackground,
                            contentColor = colors.buttonText,
                        ),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, colors.buttonBorder),
                    ) {
                        Text(
                            when {
                                trimmedNote.isBlank() && existingNote.isNotBlank() -> "Remove from journal"
                                existingNote.isBlank() -> "Save to journal"
                                else -> "Save changes"
                            },
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                } else {
                    Text(
                        if (existingNote.isBlank()) "Add a few words whenever you feel ready."
                        else "Added to your journal.",
                        color = colors.textTertiary,
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DailySharePresentation(verse: Verse) {
    val colors = ClassicHopeColors
    Column(
        Modifier.fillMaxSize().background(colors.background)
            .padding(horizontal = 28.dp, vertical = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AdaptiveDailyHopeTitle(verse)
            Text(
                BibleDisplayDateFormatter.format(LocalDate.now(), verse.edition),
                color = colors.textSecondary,
                fontFamily = interfaceFontFor(verse.edition),
                fontSize = 16.sp,
                lineHeight = 23.sp,
                letterSpacing = .3.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
            Column(
                Modifier.fillMaxWidth(.82f).padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    verse.displayReference,
                    color = colors.text,
                    fontFamily = interfaceFontFor(verse.edition),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 36.sp,
                    textAlign = TextAlign.Center,
                )
                Box(
                    Modifier.padding(top = 20.dp, bottom = 38.dp)
                        .size(56.dp, 2.dp).background(colors.accent.copy(alpha = .75f), CircleShape),
                )
                Text(
                    verse.text,
                    color = colors.cardText,
                    fontFamily = scriptureFontFor(verse.edition),
                    fontSize = dailyFontSize(verse.text.length),
                    lineHeight = dailyLineHeight(verse.text.length),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            verse.translation,
            color = colors.textTertiary,
            fontFamily = interfaceFontFor(verse.edition),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = .4.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            "HOPE CARDS",
            color = colors.accent,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 22.dp),
        )
    }
}

private fun dailyFontSize(length: Int) = when {
    length <= 90 -> 24.sp
    length <= 150 -> 22.sp
    length <= 220 -> 20.sp
    else -> 18.sp
}

@Composable
private fun AdaptiveDailyHopeTitle(verse: Verse) {
    val colors = ClassicHopeColors
    val title = BibleDisplayDateFormatter.dailyHopeTitle(verse.edition)
    val fontFamily = interfaceFontFor(verse.edition)
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val availableWidthPx = with(density) { maxWidth.roundToPx() }
        val fittedSize = remember(title, fontFamily, availableWidthPx) {
            (36 downTo 18).firstOrNull { candidate ->
                textMeasurer.measure(
                    text = title,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = candidate.sp,
                    ),
                    maxLines = 1,
                    softWrap = false,
                ).size.width <= availableWidthPx
            }?.sp ?: 18.sp
        }

        Text(
            text = title,
            color = colors.text,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fittedSize,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun dailyLineHeight(length: Int) = when {
    length <= 90 -> 42.sp
    length <= 150 -> 38.sp
    length <= 220 -> 34.sp
    else -> 31.sp
}

private fun musicFor(category: String): Int = when (category.lowercase()) {
    "comfort" -> R.raw.comfort
    "courage" -> R.raw.courage
    "faith" -> R.raw.faith
    "freedom" -> R.raw.freedom
    "grace" -> R.raw.grace
    "joy" -> R.raw.joy
    "life" -> R.raw.life
    "love" -> R.raw.love
    "peace" -> R.raw.peace
    "prayer" -> R.raw.prayer
    "strength" -> R.raw.strength
    "trust" -> R.raw.trust
    "wisdom" -> R.raw.wisdom
    else -> R.raw.hope
}
