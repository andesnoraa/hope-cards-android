package com.aaronsedna.hopecards.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.LocalHopeThemeName
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import com.aaronsedna.hopecards.ui.theme.scriptureFontFor

@Composable
fun ActionPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favorite: Boolean? = null,
    accentColor: Color? = null,
    savedColor: Color? = null,
) {
    val colors = LocalHopeColors.current
    val tint = if (favorite == true) savedColor ?: colors.danger else accentColor ?: colors.accent
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val actionAlpha by animateFloatAsState(
        targetValue = if (pressed) .58f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "action press",
    )
    Box(
        modifier = modifier
            .size(width = 120.dp, height = 48.dp)
            .alpha(actionAlpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                glyph = when (favorite) {
                    true -> AppIconGlyph.Heart
                    false -> AppIconGlyph.HeartOutline
                    null -> AppIconGlyph.ShareOutline
                },
                contentDescription = null,
                tint = tint,
                size = 22.dp,
            )
            Text(label, color = tint, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun VerseCardFace(
    verse: Verse,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    hapticsEnabled: Boolean,
    onChangeTranslation: () -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = true,
) {
    when (LocalHopeThemeName.current) {
        ThemeName.VINTAGE_HERITAGE -> {
            VintageVerseCardFace(verse, favorite, onFavorite, onShare, hapticsEnabled, onChangeTranslation, modifier, showActions)
            return
        }
        ThemeName.SERENITY -> {
            EvergreenVerseCardFace(verse, favorite, onFavorite, onShare, hapticsEnabled, onChangeTranslation, modifier, showActions)
            return
        }
        else -> Unit
    }
    val colors = LocalHopeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(34.dp),
        color = colors.cardFront,
        border = BorderStroke(1.5.dp, colors.accent),
        shadowElevation = 7.dp,
    ) {
        Box(Modifier.padding(12.dp).border(1.dp, colors.accentLine, RoundedCornerShape(26.dp))) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    verse.category.uppercase(),
                    color = colors.accent,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(9.dp))
                Box(Modifier.size(width = 70.dp, height = 3.dp).background(colors.accent, CircleShape))
                Box(modifier = Modifier.weight(1f).padding(vertical = 18.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        verse.text,
                        color = colors.cardText,
                        fontFamily = scriptureFontFor(verse.edition),
                        fontSize = verseFontSize(verse.text.length),
                        lineHeight = verseLineHeight(verse.text.length),
                        textAlign = TextAlign.Center,
                    )
                }
                if (showActions) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionPill(if (favorite) "Saved" else "Save", onFavorite, favorite = favorite)
                        ActionPill("Share", onShare)
                    }
                    Spacer(Modifier.height(18.dp))
                }
                Text(
                    verse.displayReference,
                    color = colors.cardText,
                    fontFamily = interfaceFontFor(verse.edition),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    verse.translation,
                    color = colors.cardMuted,
                    fontFamily = Poppins,
                    fontSize = translationFontSize(verse.translation.length),
                    letterSpacing = translationLetterSpacing(verse.translation.length),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.changeTranslationOnLongPress(hapticsEnabled, onChangeTranslation)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    when (LocalHopeThemeName.current) {
        ThemeName.VINTAGE_HERITAGE -> {
            VintageCardBack(modifier)
            return
        }
        ThemeName.SERENITY -> {
            EvergreenCardBack(modifier)
            return
        }
        else -> Unit
    }
    val colors = LocalHopeColors.current
    Surface(
        modifier = modifier,
        color = colors.cardBack,
        shape = RoundedCornerShape(34.dp),
        border = BorderStroke(1.5.dp, colors.cardBackAccent),
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.padding(12.dp).border(1.dp, colors.cardBackAccent.copy(alpha = .55f), RoundedCornerShape(26.dp))) {
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center).offset(y = (-18).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("✝", color = colors.cardBackAccent, fontFamily = SourceSerif, fontWeight = FontWeight.SemiBold, fontSize = 52.sp)
                Spacer(Modifier.height(12.dp))
                Text("HOPE", color = colors.cardBackText, fontFamily = SourceSerif, fontWeight = FontWeight.SemiBold, fontSize = 68.sp, letterSpacing = 2.sp)
                Text("CARDS", color = colors.cardBackAccent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 14.sp)
            }
        }
    }
}

private val vintageBurgundy = Color(0xFF6B3026)
private val vintageBrass = Color(0xFFB58A4A)
private val evergreenGreen = Color(0xFF183B32)
private val evergreenGold = Color(0xFFC5A35A)

@Composable
private fun VintageVerseCardFace(
    verse: Verse,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    hapticsEnabled: Boolean,
    onChangeTranslation: () -> Unit,
    modifier: Modifier,
    showActions: Boolean,
) {
    val colors = LocalHopeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.cardFront,
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.vintage_card_front),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 34.dp, end = 34.dp, top = 62.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = verse.category.uppercase(),
                    color = vintageBurgundy,
                    fontFamily = SourceSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 2.5.sp,
                )
                Spacer(Modifier.height(12.dp))
                VintageDivider()
                Box(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = verse.text,
                        color = colors.cardText,
                        fontFamily = scriptureFontFor(verse.edition),
                        fontSize = vintageVerseFontSize(verse.text.length),
                        lineHeight = vintageVerseLineHeight(verse.text.length),
                        textAlign = TextAlign.Center,
                    )
                }
                if (showActions) {
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        VintageAction(
                            label = if (favorite) "Saved" else "Save",
                            icon = if (favorite) AppIconGlyph.Heart else AppIconGlyph.HeartOutline,
                            onClick = onFavorite,
                        )
                        Box(Modifier.width(1.dp).height(44.dp).background(vintageBrass.copy(alpha = .4f)))
                        VintageAction("Share", AppIconGlyph.ShareOutline, onShare)
                    }
                }
                VintageDivider()
                Text(
                    text = verse.displayReference,
                    color = vintageBurgundy,
                    fontFamily = interfaceFontFor(verse.edition),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = verse.translation,
                    color = colors.cardMuted,
                    fontFamily = Poppins,
                    fontSize = 10.5.sp,
                    letterSpacing = .15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                        .changeTranslationOnLongPress(hapticsEnabled, onChangeTranslation)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun VintageCardBack(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF0B0B0A),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Image(
            painter = painterResource(R.drawable.vintage_card_back),
            contentDescription = "Hope Cards vintage leather card back",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun EvergreenVerseCardFace(
    verse: Verse,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    hapticsEnabled: Boolean,
    onChangeTranslation: () -> Unit,
    modifier: Modifier,
    showActions: Boolean,
) {
    val colors = LocalHopeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.cardFront,
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.evergreen_card_front),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 34.dp, end = 34.dp, top = 62.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = verse.category.uppercase(),
                    color = evergreenGreen,
                    fontFamily = SourceSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 2.5.sp,
                )
                Spacer(Modifier.height(12.dp))
                EvergreenDivider()
                Box(
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = verse.text,
                        color = evergreenGreen,
                        fontFamily = scriptureFontFor(verse.edition),
                        fontSize = vintageVerseFontSize(verse.text.length),
                        lineHeight = vintageVerseLineHeight(verse.text.length),
                        textAlign = TextAlign.Center,
                    )
                }
                if (showActions) {
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        EvergreenAction(
                            label = if (favorite) "Saved" else "Save",
                            icon = if (favorite) AppIconGlyph.Heart else AppIconGlyph.HeartOutline,
                            onClick = onFavorite,
                        )
                        Box(Modifier.width(1.dp).height(44.dp).background(evergreenGold.copy(alpha = .7f)))
                        EvergreenAction("Share", AppIconGlyph.ShareOutline, onShare)
                    }
                }
                EvergreenDivider()
                Text(
                    text = verse.displayReference,
                    color = evergreenGreen,
                    fontFamily = interfaceFontFor(verse.edition),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = verse.translation,
                    color = colors.cardMuted,
                    fontFamily = Poppins,
                    fontSize = 10.5.sp,
                    letterSpacing = .15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                        .changeTranslationOnLongPress(hapticsEnabled, onChangeTranslation)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun EvergreenCardBack(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = evergreenGreen,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Image(
            painter = painterResource(R.drawable.evergreen_card_back),
            contentDescription = "Hope Cards Evergreen Edition leather card back",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun EvergreenAction(
    label: String,
    icon: AppIconGlyph,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 84.dp, height = 44.dp),
        color = Color.Transparent,
        contentColor = evergreenGreen,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AppIcon(icon, contentDescription = null, tint = evergreenGreen, size = 20.dp)
            Text(label, fontFamily = Poppins, fontSize = 10.5.sp, modifier = Modifier.padding(top = 1.dp))
        }
    }
}

@Composable
private fun EvergreenDivider(width: Dp = 128.dp) {
    Row(
        modifier = Modifier.width(width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(evergreenGold))
        Text("◆", color = evergreenGold, fontSize = 8.sp)
        Box(Modifier.weight(1f).height(1.dp).background(evergreenGold))
    }
}

@Composable
private fun VintageAction(
    label: String,
    icon: AppIconGlyph,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 84.dp, height = 44.dp),
        color = Color.Transparent,
        contentColor = vintageBurgundy,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AppIcon(icon, contentDescription = null, tint = vintageBurgundy, size = 20.dp)
            Text(label, fontFamily = Poppins, fontSize = 10.5.sp, modifier = Modifier.padding(top = 1.dp))
        }
    }
}

@Composable
private fun VintageDivider(width: Dp = 128.dp) {
    Row(
        modifier = Modifier.width(width),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(vintageBrass.copy(alpha = .78f)))
        Text("◆", color = vintageBrass, fontSize = 8.sp)
        Box(Modifier.weight(1f).height(1.dp).background(vintageBrass.copy(alpha = .78f)))
    }
}

private fun vintageVerseFontSize(length: Int) = when {
    length <= 70 -> 27.sp
    length <= 120 -> 23.sp
    length <= 160 -> 20.sp
    length <= 220 -> 18.sp
    else -> 15.sp
}

private fun vintageVerseLineHeight(length: Int) = when {
    length <= 70 -> 37.sp
    length <= 120 -> 32.sp
    length <= 160 -> 29.sp
    length <= 220 -> 26.sp
    else -> 22.sp
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalHopeColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.accentLine),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
fun ResponsiveScrollColumn(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 760.dp,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.fillMaxHeight().widthIn(max = maxContentWidth).fillMaxWidth()
                .verticalScroll(rememberScrollState()).then(modifier),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

private fun verseFontSize(length: Int) = when {
    length <= 70 -> 24.sp
    length <= 120 -> 22.sp
    length <= 160 -> 20.sp
    length <= 220 -> 18.sp
    else -> 16.sp
}

private fun verseLineHeight(length: Int) = when {
    length <= 70 -> 34.sp
    length <= 120 -> 32.sp
    length <= 160 -> 29.sp
    length <= 220 -> 26.sp
    else -> 23.sp
}

private fun translationFontSize(length: Int) = when {
    length <= 22 -> 16.sp
    length <= 30 -> 15.sp
    else -> 14.sp
}

private fun translationLetterSpacing(length: Int) = when {
    length <= 22 -> 1.5.sp
    length <= 30 -> .8.sp
    else -> .3.sp
}
