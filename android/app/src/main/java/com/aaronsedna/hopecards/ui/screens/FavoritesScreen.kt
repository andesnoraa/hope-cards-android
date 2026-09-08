package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import com.aaronsedna.hopecards.ui.theme.scriptureFontFor

@Composable
fun FavoritesScreen(
    verses: List<Verse>,
    onOpen: (Verse) -> Unit,
    onRemove: (Verse) -> Unit,
    onRemoveSelected: (Set<String>) -> Unit,
) {
    val colors = LocalHopeColors.current
    var pendingRemoval by remember { mutableStateOf<Verse?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var confirmSelectedRemoval by remember { mutableStateOf(false) }
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
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().widthIn(max = 760.dp).align(Alignment.CenterHorizontally)
                .padding(start = 20.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (selectionMode) "${selectedIds.size} selected"
                else "${verses.size} saved ${if (verses.size == 1) "verse" else "verses"}",
                color = colors.textTertiary,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            if (selectionMode) {
                TextButton(onClick = {
                    selectedIds = if (selectedIds.size == verses.size) emptySet() else verses.map(Verse::id).toSet()
                }) {
                    Text(
                        if (selectedIds.size == verses.size) "Clear" else "Select all",
                        color = colors.accent,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                TextButton(onClick = {
                    selectionMode = false
                    selectedIds = emptySet()
                }) {
                    Text("Cancel", color = colors.textSecondary, fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            } else {
                TextButton(onClick = { selectionMode = true }) {
                    Text("Select", color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(verses, key = Verse::id) { verse ->
                Column(
                    Modifier.fillMaxWidth().widthIn(max = 760.dp).combinedClickable(
                        onClick = {
                            if (selectionMode) {
                                selectedIds = if (verse.id in selectedIds) selectedIds - verse.id else selectedIds + verse.id
                            } else {
                                onOpen(verse)
                            }
                        },
                        onLongClickLabel = "Select ${verse.displayReference}",
                        onLongClick = {
                            selectionMode = true
                            selectedIds = selectedIds + verse.id
                        },
                    ).padding(top = 18.dp),
                ) {
                    Text(verse.category.uppercase(), color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.5.sp, modifier = Modifier.padding(bottom = 10.dp))
                    Text(verse.text, color = colors.cardText, fontFamily = scriptureFontFor(verse.edition), fontSize = 19.sp, lineHeight = 30.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 10.dp))
                    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(verse.displayReference, color = colors.text, fontFamily = interfaceFontFor(verse.edition), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        if (selectionMode) {
                            Checkbox(
                                checked = verse.id in selectedIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + verse.id else selectedIds - verse.id
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.accent,
                                    uncheckedColor = colors.textTertiary,
                                    checkmarkColor = colors.buttonText,
                                ),
                            )
                        } else {
                            AppIcon(AppIconGlyph.ChevronForward, null, colors.textTertiary, size = 18.dp)
                            Spacer(Modifier.width(14.dp))
                            IconButton(onClick = { pendingRemoval = verse }) {
                                AppIcon(AppIconGlyph.TrashOutline, "Remove ${verse.displayReference} from favorites", colors.textTertiary, size = 19.dp)
                            }
                        }
                    }
                    HorizontalDivider(color = colors.divider)
                }
            }
        }
        if (selectionMode && selectedIds.isNotEmpty()) {
            Button(
                onClick = { confirmSelectedRemoval = true },
                modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp).align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
            ) {
                AppIcon(AppIconGlyph.TrashOutline, null, Color.White, size = 19.dp)
                Text(
                    "Remove selected (${selectedIds.size})",
                    color = Color.White,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }

    pendingRemoval?.let { verse ->
        FavoriteRemovalDialog(
            title = "Remove saved verse?",
            message = "${verse.displayReference} will be removed from Favorites.",
            confirmLabel = "Remove",
            onDismiss = { pendingRemoval = null },
            onConfirm = {
                pendingRemoval = null
                onRemove(verse)
            },
        )
    }
    if (confirmSelectedRemoval) {
        FavoriteRemovalDialog(
            title = "Remove selected verses?",
            message = "${selectedIds.size} saved ${if (selectedIds.size == 1) "verse" else "verses"} will be removed from Favorites.",
            confirmLabel = "Remove",
            onDismiss = { confirmSelectedRemoval = false },
            onConfirm = {
                val ids = selectedIds
                confirmSelectedRemoval = false
                selectionMode = false
                selectedIds = emptySet()
                onRemoveSelected(ids)
            },
        )
    }
}

@Composable
private fun FavoriteRemovalDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = LocalHopeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontFamily = Poppins, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontFamily = Poppins) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = colors.danger, fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = Poppins) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary,
        tonalElevation = 0.dp,
    )
}
