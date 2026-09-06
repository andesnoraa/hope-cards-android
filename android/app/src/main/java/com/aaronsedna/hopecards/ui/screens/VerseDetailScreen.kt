package com.aaronsedna.hopecards.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.components.ActionPill
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif

@Composable
fun VerseDetailScreen(verse: Verse, favorite: Boolean, onFavorite: () -> Unit, onShare: () -> Unit) {
    val colors = LocalHopeColors.current
    val alpha = androidx.compose.runtime.remember(verse.id) { Animatable(0f) }
    LaunchedEffect(verse.id) { alpha.animateTo(1f, tween(300)) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp).padding(top = 32.dp, bottom = 220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(Modifier.fillMaxWidth().widthIn(max = 720.dp).alpha(alpha.value), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(verse.category.uppercase(), color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 3.sp, modifier = Modifier.padding(bottom = 10.dp))
            Box(Modifier.size(80.dp, 3.dp).background(colors.accent, CircleShape))
            Text(verse.reference, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 26.dp))
            Text(
                verse.text,
                color = colors.cardText,
                fontFamily = SourceSerif,
                fontSize = detailFontSize(verse.text.length),
                lineHeight = detailLineHeight(verse.text.length),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 34.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 28.dp, bottom = 18.dp)) {
                ActionPill(if (favorite) "Saved" else "Save", onFavorite, favorite = favorite)
                ActionPill("Share", onShare)
            }
            Column(Modifier.padding(top = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TRANSLATION", color = colors.textTertiary, fontFamily = Poppins, fontSize = 13.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(verse.translation, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun detailFontSize(length: Int) = when {
    length <= 90 -> 24.sp
    length <= 150 -> 22.sp
    length <= 220 -> 20.sp
    else -> 18.sp
}

private fun detailLineHeight(length: Int) = when {
    length <= 90 -> 40.sp
    length <= 150 -> 37.sp
    length <= 220 -> 34.sp
    else -> 31.sp
}
