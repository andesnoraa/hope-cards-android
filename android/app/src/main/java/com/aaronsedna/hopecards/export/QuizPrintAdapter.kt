package com.aaronsedna.hopecards.export

import android.os.Bundle
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.os.ParcelFileDescriptor
import android.print.*
import com.aaronsedna.hopecards.R
import kotlinx.coroutines.*

class QuizPrintAdapter(private val report: QuizReport) : PrintDocumentAdapter() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pages: QuizReport.Pages? = null
    override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes, cancellationSignal: CancellationSignal, callback: LayoutResultCallback, extras: Bundle?) {
        scope.launch {
            try {
                pages = null
                val media = requireNotNull(newAttributes.mediaSize)
                val margins = requireNotNull(newAttributes.minMargins)
                fun points(mils: Int) = (mils * 72 / 1000)
                val paper = QuizReport.Paper(points(media.widthMils), points(media.heightMils),
                    maxOf(40, points(margins.leftMils)), maxOf(40, points(margins.topMils)),
                    maxOf(40, points(margins.rightMils)), maxOf(40, points(margins.bottomMils)))
                val result = withContext(Dispatchers.Default) { report.layout(paper, cancellationSignal) }
                cancellationSignal.throwIfCanceled()
                pages = result
                callback.onLayoutFinished(PrintDocumentInfo.Builder("${report.title}.pdf").setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(result.content.size).build(), oldAttributes != newAttributes)
            } catch (_: OperationCanceledException) { callback.onLayoutCancelled() }
            catch (error: CancellationException) { callback.onLayoutCancelled(); throw error }
            catch (_: Exception) { callback.onLayoutFailed(report.resources.getString(R.string.quiz_export_error)) }
        }
    }
    override fun onWrite(ranges: Array<out PageRange>, destination: ParcelFileDescriptor, cancellationSignal: CancellationSignal, callback: WriteResultCallback) {
        val snapshot = pages
        scope.launch {
            try {
                val written = withContext(Dispatchers.IO) {
                    ParcelFileDescriptor.AutoCloseOutputStream(destination).use {
                        report.write(requireNotNull(snapshot), it, ranges, cancellationSignal)
                    }
                }
                cancellationSignal.throwIfCanceled()
                callback.onWriteFinished(written)
            } catch (_: OperationCanceledException) { callback.onWriteCancelled() }
            catch (error: CancellationException) { callback.onWriteCancelled(); throw error }
            catch (_: Exception) { callback.onWriteFailed(report.resources.getString(R.string.quiz_export_error)) }
        }
    }
    override fun onFinish() { scope.cancel() }
}
