package com.aaronsedna.hopecards.ui.screens

import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.export.QuizReport
import com.aaronsedna.hopecards.export.QuizReportExport
import com.aaronsedna.hopecards.model.*
import com.aaronsedna.hopecards.ui.components.*
import com.aaronsedna.hopecards.ui.theme.*
import kotlinx.coroutines.*
import java.io.File

/** Sharing retains the score screen and bypasses the completed-round ad. */
@Composable
internal fun QuizResultActions(translation: Translation, session: QuizSession, questions: List<QuizQuestion>, primary: Boolean = false) {
    val context = LocalContext.current
    val colors = LocalHopeColors.current
    val scope = rememberCoroutineScope()
    val report = remember(translation, session, questions) { QuizReport(context, translation, session, questions) }
    val resources = report.resources
    var busy by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    val foreground = if (primary) colors.buttonText else colors.text
    Button(
        onClick = {
            if (!busy) {
                busy = true
                scope.launch {
                    var file: File? = null
                    var shared = false
                    try {
                        file = QuizReportExport.createCachedPdf(context, report)
                        context.startActivity(Intent.createChooser(
                            QuizReportExport.shareIntent(context, report, file),
                            resources.getString(R.string.quiz_share_results)))
                        shared = true
                    } catch (cancel: CancellationException) { throw cancel }
                    catch (_: Exception) { error = true }
                    finally {
                        // Recipients may still be reading a successful share after returning to the app.
                        if (!shared) file?.delete()
                        busy = false
                    }
                }
            }
        },
        enabled = !busy,
        shape = RoundedCornerShape(12.dp), border = if (primary) null else BorderStroke(1.dp, colors.text),
        colors = ButtonDefaults.buttonColors(containerColor = if (primary) colors.buttonBackground else androidx.compose.ui.graphics.Color.Transparent,
            contentColor = foreground),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("quiz_export"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = foreground)
            else AppIcon(AppIconGlyph.ShareOutline, null, foreground, size = 20.dp)
            Text(resources.getString(if (busy) R.string.quiz_preparing_pdf else R.string.quiz_share_export),
                fontFamily = interfaceFontFor(translation), fontSize = 16.sp)
        }
    }
    if (error) AlertDialog(onDismissRequest = { error = false },
        text = { Text(resources.getString(R.string.quiz_export_error), fontFamily = interfaceFontFor(translation)) },
        confirmButton = { TextButton(onClick = { error = false }) {
            Text(resources.getString(R.string.quiz_export_close), fontFamily = interfaceFontFor(translation))
        } }, containerColor = colors.surface)
}
