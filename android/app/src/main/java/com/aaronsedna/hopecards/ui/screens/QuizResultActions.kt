package com.aaronsedna.hopecards.ui.screens

import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.export.QuizPrintAdapter
import com.aaronsedna.hopecards.export.QuizReport
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.components.*
import com.aaronsedna.hopecards.ui.theme.*
import kotlinx.coroutines.*
import java.io.File

/** Sharing/exporting retains the score screen and intentionally bypasses the completed-round ad. */
@Composable
internal fun QuizResultActions(translation: Translation, session: QuizSession, questions: List<QuizQuestion>) {
    val context = LocalContext.current
    val colors = LocalHopeColors.current
    val scope = rememberCoroutineScope()
    val report = remember(translation, session, questions) { QuizReport(context, translation, session, questions) }
    val resources = report.resources
    var menu by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    // Persist the completed file, so rotation/process recreation during the document picker is safe.
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val file = pendingPath?.let(::File)
        pendingPath = null
        if (uri == null) file?.delete()
        else scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) {
                    requireNotNull(file).inputStream().use { input ->
                        requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { input.copyTo(it) }
                    }
                }
                Toast.makeText(context, resources.getString(R.string.quiz_pdf_saved), Toast.LENGTH_SHORT).show()
            } catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) { error = true }
            finally { file?.delete(); busy = false }
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val menuWidth = minOf(maxWidth, 280.dp)
        val menuOffset = DpOffset((maxWidth - menuWidth) / 2, 4.dp)
        OutlinedButton(onClick = { menu = true }, enabled = !busy && pendingPath == null,
            shape = RoundedCornerShape(28.dp), border = BorderStroke(1.dp, colors.divider),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("quiz_export")) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.text)
                else AppIcon(AppIconGlyph.ShareOutline, null, colors.text, size = 20.dp)
                Text(resources.getString(if (busy) R.string.quiz_preparing_pdf else R.string.quiz_share_export),
                    fontFamily = interfaceFontFor(translation), fontSize = 16.sp)
            }
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false },
            modifier = Modifier.width(menuWidth), offset = menuOffset,
            shape = RoundedCornerShape(16.dp), containerColor = colors.surface) {
            @Composable fun Action(id: Int, tag: String, action: () -> Unit) {
                DropdownMenuItem(text = { Text(resources.getString(id), fontFamily = interfaceFontFor(translation), color = colors.text) },
                    modifier = Modifier.testTag(tag), onClick = { menu = false; action() })
            }
            Action(R.string.quiz_share_results, "quiz_share") {
                try {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, report.title)
                        putExtra(Intent.EXTRA_TEXT, report.shareText)
                    }, resources.getString(R.string.quiz_share_results)))
                } catch (_: Exception) { error = true }
            }
            Action(R.string.quiz_save_pdf, "quiz_save_pdf") {
                scope.launch {
                    busy = true
                    var file: File? = null
                    try {
                        file = withContext(Dispatchers.IO) {
                            val directory = File(context.cacheDir, "quiz-reports").apply { mkdirs() }
                            // Only remove old abandoned picker files, never another current export.
                            directory.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 7 * 24 * 60 * 60 * 1000L }?.forEach { it.delete() }
                            File.createTempFile("quiz-", ".pdf", directory).also { target ->
                                try { target.outputStream().use { report.write(report.layout(), it) } }
                                catch (error: Exception) { target.delete(); throw error }
                            }
                        }
                        pendingPath = file.absolutePath
                        save.launch("Hope-Cards-Quiz-${QuizLanguage.forTranslation(translation).code}.pdf")
                    } catch (cancel: CancellationException) { file?.delete(); throw cancel }
                    catch (_: Exception) { pendingPath = null; file?.delete(); error = true }
                    finally { busy = false }
                }
            }
            Action(R.string.quiz_print, "quiz_print") {
                try {
                    val manager = context.getSystemService(PrintManager::class.java)
                    requireNotNull(manager).print(report.title, QuizPrintAdapter(report), PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4).setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME).build())
                } catch (_: Exception) { error = true }
            }
        }
    }
    if (error) AlertDialog(onDismissRequest = { error = false },
        text = { Text(resources.getString(R.string.quiz_export_error), fontFamily = interfaceFontFor(translation)) },
        confirmButton = { TextButton(onClick = { error = false }) {
            Text(resources.getString(R.string.quiz_export_close), fontFamily = interfaceFontFor(translation))
        } }, containerColor = colors.surface)
}
