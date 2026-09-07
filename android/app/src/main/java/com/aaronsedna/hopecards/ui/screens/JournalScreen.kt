package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalScreen(
    entries: List<JournalEntry>,
    verseFor: (String) -> Verse?,
    onOpenEntry: (JournalEntry, Verse) -> Unit,
) {
    val colors = LocalHopeColors.current

    if (entries.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(Modifier.size(64.dp), shape = CircleShape, color = colors.accentSoft) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(AppIconGlyph.JournalOutline, null, colors.accent, size = 30.dp)
                }
            }
            Text("A quiet space is ready", color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 35.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 22.dp))
            Text("Open Daily Hope and keep a few words from a quiet reflection. They will appear here.", color = colors.textSecondary, fontFamily = Poppins, fontSize = 16.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp).widthIn(max = 360.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(entries, key = JournalEntry::id) { entry ->
                val verse = verseFor(entry.verseId)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 760.dp)
                        .clickable(enabled = verse != null) { verse?.let { onOpenEntry(entry, it) } }
                        .padding(vertical = 18.dp),
                ) {
                    if (verse != null) {
                        Text(
                            verse.category.uppercase(),
                            color = colors.accent,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 2.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(
                            verse.text,
                            color = colors.cardText,
                            fontFamily = SourceSerif,
                            fontSize = 19.sp,
                            lineHeight = 29.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            verse?.reference ?: entry.reference,
                            color = colors.text,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            formatDate(entry.date),
                            color = colors.textTertiary,
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        AppIcon(AppIconGlyph.ChevronForward, null, colors.textTertiary, size = 18.dp)
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(42.dp)
                                .background(colors.accentLine, RoundedCornerShape(1.dp)),
                        )
                        Text(
                            entry.note,
                            color = colors.textSecondary,
                            fontFamily = SourceSerif,
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp).weight(1f),
                        )
                    }

                    HorizontalDivider(
                        color = colors.divider,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun JournalEditorDialog(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onSave: (JournalEntry) -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val colors = LocalHopeColors.current
    var draft by remember(entry.id, entry.updatedAt) { mutableStateOf(entry.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your Reflection", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(calmReflectionText(entry.prompt), color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 20_000) draft = it },
                    minLines = 6,
                    shape = RoundedCornerShape(18.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SourceSerif, fontSize = 18.sp, color = colors.cardText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.accentLine,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(entry.copy(note = draft)) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Keep Changes", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDeleteRequest) { Text("Delete", color = colors.danger) }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary,
        tonalElevation = 0.dp,
    )
}

@Composable
fun DeleteJournalDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalHopeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Reflection?", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
        text = { Text("This reflection will be removed from your journal.") },
        confirmButton = { TextButton(onClick = onDelete) { Text("Delete", color = colors.danger) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary,
        tonalElevation = 0.dp,
    )
}

private fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
}.getOrDefault(value)

private fun calmReflectionText(value: String): String = legacyReflectionQuestions[value] ?: value

private val legacyReflectionQuestions = mapOf(
    "Where do you need to receive comfort today?" to "Take a moment to rest in these words.",
    "What would one small act of courage look like today?" to "One small step is enough for today.",
    "What are you being invited to trust, even without seeing the whole path?" to "You can move forward without having every answer.",
    "What burden can you begin to release today?" to "Consider what you can gently let go of today.",
    "Where can you receive or extend grace today?" to "Receive grace, and offer it where you can.",
    "What possibility does this verse invite you to hold onto?" to "Keep close the words that give you hope.",
    "What quiet gift can you notice and give thanks for today?" to "Notice one good thing in this day.",
    "What is helping you feel fully alive right now?" to "Notice what helps you feel present and grateful.",
    "Who might need to experience love through you today?" to "Let these words guide how you care for others today.",
    "What can you place in God’s hands to make room for peace?" to "Place what feels heavy in God’s care.",
    "What honest prayer rises from this verse?" to "Let these words become a simple prayer.",
    "Where do you need strength for the next faithful step?" to "Take the next step with the strength you have.",
    "What are you holding tightly that you could entrust to God?" to "Place what you cannot control in God’s hands.",
    "What choice could you approach with greater wisdom today?" to "Pause and choose what is thoughtful and kind.",
    "What is this verse inviting you to notice, trust, or practice today?" to "Keep the words that feel meaningful to you today.",
)
