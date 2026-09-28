package com.aaronsedna.hopecards.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/** Shared PDFs remain readable while another app imports them, even after the chooser closes. */
object QuizReportExport {
    suspend fun createCachedPdf(context: Context, report: QuizReport): File {
        var createdFile: File? = null
        try {
            return withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "quiz-reports")
                check(directory.isDirectory || directory.mkdirs())
                val oldest = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                directory.listFiles()?.filter { it.extension == "pdf" && it.lastModified() < oldest }
                    ?.forEach { it.delete() }
                ensureActive()
                val file = File.createTempFile("Hope-Cards-Quiz-", ".pdf", directory)
                createdFile = file
                val pages = report.layout()
                ensureActive()
                file.outputStream().use { report.write(pages, it) }
                ensureActive()
                file
            }
        } catch (error: Exception) {
            // Also handles cancellation at the dispatcher handoff, before the caller gets the file.
            createdFile?.delete()
            throw error
        }
    }

    fun shareIntent(context: Context, report: QuizReport, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, report.title)
            putExtra(Intent.EXTRA_TEXT, report.shareText)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, report.title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
