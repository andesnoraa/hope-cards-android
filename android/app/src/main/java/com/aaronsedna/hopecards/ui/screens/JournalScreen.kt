package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.JournalEntry
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.components.SectionCard
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import com.aaronsedna.hopecards.ui.theme.SourceSerif
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalScreen(entries: List<JournalEntry>, onSave: (JournalEntry) -> Unit) {
    val colors = LocalHopeColors.current
    var editing by remember { mutableStateOf<JournalEntry?>(null) }
    var deleting by remember { mutableStateOf<JournalEntry?>(null) }

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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(entries, key = JournalEntry::id) { entry ->
                SectionCard(Modifier.widthIn(max = 760.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.reference, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        Text(formatDate(entry.date), color = colors.textTertiary, fontFamily = Poppins, fontSize = 12.sp)
                    }
                    Text(calmReflectionText(entry.prompt), color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 21.sp)
                    Text(entry.note, color = colors.cardText, fontFamily = SourceSerif, fontSize = 18.sp, lineHeight = 28.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { editing = entry }) {
                            AppIcon(AppIconGlyph.CreateOutline, null, colors.accent, size = 18.dp)
                            Text("Edit", modifier = Modifier.padding(start = 5.dp))
                        }
                        TextButton(onClick = { deleting = entry }) {
                            AppIcon(AppIconGlyph.TrashOutline, null, colors.textTertiary, size = 18.dp)
                            Text("Delete", color = colors.textTertiary, modifier = Modifier.padding(start = 5.dp))
                        }
                    }
                }
            }
        }
    }

    editing?.let { entry ->
        var draft by remember(entry.id) { mutableStateOf(entry.note) }
        AlertDialog(
            onDismissRequest = { editing = null },
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
                    onClick = { onSave(entry.copy(note = draft)); editing = null },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Keep Changes", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surface,
            titleContentColor = colors.text,
            textContentColor = colors.textSecondary,
            tonalElevation = 0.dp,
        )
    }

    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete Reflection?", fontFamily = Poppins, fontWeight = FontWeight.Bold) },
            text = { Text("This reflection will be removed from this device.") },
            confirmButton = { TextButton(onClick = { onSave(entry.copy(note = "")); deleting = null }) { Text("Delete", color = colors.danger) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.surface,
            titleContentColor = colors.text,
            textContentColor = colors.textSecondary,
            tonalElevation = 0.dp,
        )
    }
}

private fun formatDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
}.getOrDefault(value)

private fun calmReflectionText(value: String): String = legacyReflectionQuestions[value] ?: value

private val legacyReflectionQuestions = mapOf(
    "Where do you need to receive comfort today?" to "You are allowed to rest here and receive comfort, one breath at a time.",
    "What would one small act of courage look like today?" to "Courage can begin with one small, faithful step.",
    "What are you being invited to trust, even without seeing the whole path?" to "You do not need to see the whole path to take the next step in faith.",
    "What burden can you begin to release today?" to "You may gently loosen your hold on what has been weighing you down.",
    "Where can you receive or extend grace today?" to "There is grace for you here, and enough to share with someone else.",
    "What possibility does this verse invite you to hold onto?" to "Hold space for hope, even before the way forward becomes clear.",
    "What quiet gift can you notice and give thanks for today?" to "A quiet gift may be waiting to be noticed today.",
    "What is helping you feel fully alive right now?" to "Notice what brings you back to life, peace, and presence.",
    "Who might need to experience love through you today?" to "Let love move gently through the way you meet someone today.",
    "What can you place in God’s hands to make room for peace?" to "You can place what feels heavy into God’s hands and make room for peace.",
    "What honest prayer rises from this verse?" to "Let a simple, honest prayer rise from this moment.",
    "Where do you need strength for the next faithful step?" to "Strength for the next faithful step is enough for today.",
    "What are you holding tightly that you could entrust to God?" to "You do not have to carry everything alone; this can be entrusted to God.",
    "What choice could you approach with greater wisdom today?" to "Move slowly, listen deeply, and let wisdom shape the next choice.",
    "What is this verse inviting you to notice, trust, or practice today?" to "Stay with the words that bring you peace, hope, or courage today.",
)
