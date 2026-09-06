package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif

@Composable
fun FavoritesScreen(verses: List<Verse>, onOpen: (Verse) -> Unit) {
    val colors = LocalHopeColors.current
    if (verses.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 50.dp).padding(bottom = 140.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(Modifier.size(64.dp), shape = CircleShape, color = colors.danger.copy(alpha = .08f)) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(AppIconGlyph.Heart, null, colors.danger, size = 30.dp)
                }
            }
            Text("A place for verses you love", color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 35.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 22.dp, bottom = 12.dp))
            Text("When a verse stays with you, tap Save and find it here anytime.", color = colors.textSecondary, fontFamily = Poppins, fontSize = 16.sp, lineHeight = 26.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 330.dp))
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(verses, key = Verse::id) { verse ->
            Column(
                Modifier.fillMaxWidth().widthIn(max = 760.dp).clickable { onOpen(verse) }.padding(vertical = 22.dp),
            ) {
                Text(verse.category.uppercase(), color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.5.sp, modifier = Modifier.padding(bottom = 10.dp))
                Text(verse.text, color = colors.cardText, fontFamily = SourceSerif, fontSize = 19.sp, lineHeight = 30.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 18.dp))
                Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(verse.reference, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    AppIcon(AppIconGlyph.ChevronForward, null, colors.textTertiary, size = 18.dp)
                }
                HorizontalDivider(color = colors.divider)
            }
        }
    }
}
