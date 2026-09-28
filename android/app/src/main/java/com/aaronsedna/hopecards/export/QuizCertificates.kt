package com.aaronsedna.hopecards.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.text.LineBreaker
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.forQuizTranslation
import java.io.File
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

data class CertificateDetails(val competitionTitle: String, val date: String, val organizer: String = "")
data class CertificateParticipant(val name: String)
data class GeneratedCertificate(val participantName: String, val file: File)

enum class CertificateValidationError {
    REQUIRED, NAME_TOO_LONG, TOO_MANY_PARTICIPANTS, TITLE_TOO_LONG, DATE_TOO_LONG,
    ORGANIZER_TOO_LONG, INVALID_NAME,
}

/** A batch is atomic: an interrupted export leaves no partial certificates behind. */
object QuizCertificates {
    const val MAX_PARTICIPANTS = 50
    const val MAX_NAME_LENGTH = 100
    const val MAX_TITLE_LENGTH = 120
    const val MAX_DATE_LENGTH = 60
    const val MAX_ORGANIZER_LENGTH = 120
    private const val DIRECTORY = "quiz-certificates"
    private const val RETENTION_MILLIS = 7 * 24 * 60 * 60 * 1000L
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"

    fun validate(details: CertificateDetails, participants: List<CertificateParticipant>): CertificateValidationError? = when {
        details.competitionTitle.isBlank() || details.date.isBlank() || participants.isEmpty() ||
            participants.any { it.name.isBlank() } -> CertificateValidationError.REQUIRED
        participants.size > MAX_PARTICIPANTS -> CertificateValidationError.TOO_MANY_PARTICIPANTS
        participants.any { it.name.trim().length > MAX_NAME_LENGTH } -> CertificateValidationError.NAME_TOO_LONG
        participants.any { it.name.trim().any { char -> char == '\n' || char == '\r' || char.isISOControl() } } -> CertificateValidationError.INVALID_NAME
        details.competitionTitle.trim().length > MAX_TITLE_LENGTH -> CertificateValidationError.TITLE_TOO_LONG
        details.date.trim().length > MAX_DATE_LENGTH -> CertificateValidationError.DATE_TOO_LONG
        details.organizer.trim().length > MAX_ORGANIZER_LENGTH -> CertificateValidationError.ORGANIZER_TOO_LONG
        else -> null
    }

    suspend fun generate(
        context: Context,
        translation: Translation,
        details: CertificateDetails,
        participants: List<CertificateParticipant>,
        onProgress: suspend (Int, Int) -> Unit = { _, _ -> },
    ): List<GeneratedCertificate> {
        require(validate(details, participants) == null) { "Invalid certificate details: ${validate(details, participants)}" }
        val completed = mutableListOf<GeneratedCertificate>()
        var currentFile: File? = null
        try {
            return withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, DIRECTORY)
                check(directory.isDirectory || directory.mkdirs()) { "Could not create the certificate folder." }
                val oldest = System.currentTimeMillis() - RETENTION_MILLIS
                directory.listFiles()?.filter { it.extension == "pdf" && it.lastModified() < oldest }?.forEach { it.delete() }
                ensureActive()
                val renderer = QuizCertificateRenderer(context, translation)
                val batchId = UUID.randomUUID().toString()
                val cleanDetails = details.copy(competitionTitle = cleanText(details.competitionTitle),
                    date = cleanText(details.date), organizer = cleanText(details.organizer))
                onProgress(0, participants.size)
                participants.forEachIndexed { index, participant ->
                    ensureActive()
                    val name = participant.name.trim()
                    // User-entered names never become paths. Index and batch ID also preserve duplicate names.
                    val file = File(directory, "%02d-Hope-Cards-Certificate-%s.pdf".format(index + 1, batchId))
                    currentFile = file
                    val page = renderer.layout(cleanDetails, CertificateParticipant(name))
                    ensureActive()
                    file.outputStream().use { renderer.write(page, it) }
                    ensureActive()
                    completed += GeneratedCertificate(name, file)
                    currentFile = null
                    onProgress(index + 1, participants.size)
                }
                ensureActive()
                completed.toList()
            }
        } catch (error: Throwable) {
            currentFile?.delete()
            completed.forEach { it.file.delete() }
            throw error
        }
    }

    fun shareMessage(context: Context, translation: Translation): String {
        val resources = context.forQuizTranslation(translation).resources
        return "${resources.getString(R.string.quiz_certificate_share_attribution)}\n\n" +
            "${resources.getString(R.string.quiz_certificate_share_install)}\n$PLAY_STORE_URL"
    }

    fun shareIntent(context: Context, translation: Translation, certificates: List<GeneratedCertificate>): Intent {
        require(certificates.isNotEmpty() && certificates.size <= MAX_PARTICIPANTS)
        val directory = File(context.cacheDir, DIRECTORY).canonicalFile
        require(certificates.all { it.file.canonicalFile.parentFile == directory && it.file.isFile && it.file.extension == "pdf" })
        val resources = context.forQuizTranslation(translation).resources
        val title = resources.getString(R.string.quiz_certificate_title)
        val message = shareMessage(context, translation)
        val uris = certificates.map { FileProvider.getUriForFile(context, "${context.packageName}.files", it.file) }
        return Intent(if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, title)
            if (uris.size == 1) {
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_STREAM, uris.single())
            } else {
                putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, uris.mapTo(ArrayList<CharSequence>()) { message })
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
            // Keep each caption with its file for receivers that consume ClipData instead of extras.
            clipData = ClipData(title, arrayOf("application/pdf", "text/plain"), ClipData.Item(message, null, uris.first())).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(message, null, it)) }
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun cleanText(value: String) = value.trim().replace(Regex("\\s+"), " ")
}

/** Vector artwork and embedded app fonts keep each A4 certificate sharp when printed or enlarged. */
class QuizCertificateRenderer(context: Context, translation: Translation) {
    private val resources = context.forQuizTranslation(translation).resources
    private val sans = ResourcesCompat.getFont(context, R.font.poppins_regular)!!
    private val sansBold = ResourcesCompat.getFont(context, R.font.poppins_semibold)!!
    private val serif = ResourcesCompat.getFont(context, R.font.source_serif_semibold)!!
    private val malayalam = ResourcesCompat.getFont(context, R.font.noto_sans_malayalam_regular)!!
    private val malayalamBold = ResourcesCompat.getFont(context, R.font.noto_sans_malayalam_semibold)!!

    data class TextBlock(val text: String, val layout: StaticLayout, val x: Float, val y: Float) {
        val bottom: Float get() = y + layout.height
    }
    data class Page(val blocks: List<TextBlock>, val width: Int = WIDTH, val height: Int = HEIGHT)

    fun layout(details: CertificateDetails, participant: CertificateParticipant): Page {
        require(QuizCertificates.validate(details, listOf(participant)) == null)
        val blocks = mutableListOf<TextBlock>()
        fun place(text: String, y: Int, height: Int, size: Float, minSize: Float, width: Int = 674,
                  x: Int = (WIDTH - width) / 2, bold: Boolean = false, elegant: Boolean = false,
                  color: Int = INK) {
            val face = when {
                text.any { it in '\u0D00'..'\u0D7F' } -> if (bold || elegant) malayalamBold else malayalam
                elegant -> serif
                bold -> sansBold
                else -> sans
            }
            val textLayout = fit(text, width, height, size, minSize, face, color)
            blocks += TextBlock(text, textLayout, x.toFloat(), y + (height - textLayout.height) / 2f)
        }
        // Conventional participation-certificate hierarchy: issuer, title, recipient,
        // attestation, event, date and space for the organizer to sign.
        if (details.organizer.isNotBlank()) place(details.organizer, 76, 32, 14f, 8.5f, bold = true)
        place(resources.getString(R.string.quiz_certificate_pdf_title), 114, 56, 32f, 20f, elegant = true)
        val introduction = resources.getString(R.string.quiz_certificate_pdf_presented_to)
        if (introduction.isNotBlank()) place(introduction, 177, 25, 11.5f, 9f, color = MUTED)
        place(participant.name.trim(), 209, 73, 35f, 14f, elegant = true)
        // Malayalam uses a complete attestation after the name rather than English word order.
        place(resources.getString(R.string.quiz_certificate_pdf_participation), 307, 38, 11.5f, 9f, color = MUTED)
        place(details.competitionTitle, 350, 68, 22f, 11f, bold = true)
        place(details.date, 471, 48, 12f, 8.5f, width = 294, x = 70)
        place(resources.getString(R.string.quiz_certificate_date_label), 523, 21, 9.5f, 8f,
            width = 294, x = 70, bold = true, color = MUTED)
        place(resources.getString(R.string.quiz_certificate_pdf_signature), 523, 21, 9.5f, 8f,
            width = 294, x = WIDTH - 70 - 294, bold = true, color = MUTED)
        return Page(blocks)
    }

    private fun fit(text: String, width: Int, height: Int, maximum: Float, minimum: Float, face: Typeface, color: Int): StaticLayout {
        var size = maximum
        while (true) {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = size; this.color = color }
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(true)
                .setLineSpacing(2f, 1f).setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED)
                .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE).build()
            val fitsWidth = (0 until layout.lineCount).all { layout.getLineWidth(it) <= width + 0.5f }
            if (layout.height <= height && fitsWidth) return layout
            check(size > minimum) { "Certificate text does not fit the page." }
            size = maxOf(minimum, size - 0.5f)
        }
    }

    fun write(page: Page, output: OutputStream) {
        val document = PdfDocument()
        try {
            val pdfPage = document.startPage(PdfDocument.PageInfo.Builder(WIDTH, HEIGHT, 1).create())
            drawArtwork(pdfPage.canvas)
            page.blocks.forEach { block ->
                pdfPage.canvas.save()
                pdfPage.canvas.translate(block.x, block.y)
                block.layout.draw(pdfPage.canvas)
                pdfPage.canvas.restore()
            }
            document.finishPage(pdfPage)
            document.writeTo(output)
        } finally {
            document.close()
        }
    }

    private fun drawArtwork(canvas: Canvas) {
        canvas.drawColor(IVORY)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = INK; style = Paint.Style.STROKE; strokeWidth = 1.6f }
        canvas.drawRect(26f, 26f, WIDTH - 26f, HEIGHT - 26f, paint)
        paint.color = GOLD
        paint.strokeWidth = 0.65f
        canvas.drawRect(33f, 33f, WIDTH - 33f, HEIGHT - 33f, paint)
        // Mirrored botanical corner sprays are vector shapes, leaving generous clear margins.
        for ((x, y, angle) in listOf(Triple(46f, 46f, 0f), Triple(WIDTH - 46f, 46f, 90f),
            Triple(WIDTH - 46f, HEIGHT - 46f, 180f), Triple(46f, HEIGHT - 46f, 270f))) {
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angle)
            drawCorner(canvas)
            canvas.restore()
        }
        val cx = WIDTH / 2f
        paint.color = GOLD
        paint.strokeWidth = 1.3f
        canvas.drawLine(cx - 94, 59f, cx - 30, 59f, paint)
        canvas.drawLine(cx + 30, 59f, cx + 94, 59f, paint)
        // An open book, not a seal or signature: the certificate makes no accreditation claim.
        paint.color = INK
        paint.strokeWidth = 1.5f
        val book = Path().apply {
            moveTo(cx, 51f); quadTo(cx - 10, 47f, cx - 18, 49f)
            lineTo(cx - 18, 65f); quadTo(cx - 9, 63f, cx, 68f)
            quadTo(cx + 9, 63f, cx + 18, 65f); lineTo(cx + 18, 49f)
            quadTo(cx + 10, 47f, cx, 51f); lineTo(cx, 68f)
        }
        canvas.drawPath(book, paint)
        paint.color = GOLD
        paint.strokeWidth = 0.8f
        canvas.drawLine(cx - 170, 294f, cx + 170, 294f, paint)
        paint.style = Paint.Style.FILL
        canvas.drawPath(Path().apply { moveTo(cx, 289f); lineTo(cx + 5, 294f); lineTo(cx, 299f); lineTo(cx - 5, 294f); close() }, paint)
        paint.style = Paint.Style.STROKE
        paint.color = PALE_GOLD
        canvas.drawLine(92f, 439f, WIDTH - 92f, 439f, paint)
        // Leave this line blank for a real signature. Never synthesize a signature or seal.
        paint.color = MUTED
        paint.strokeWidth = 0.6f
        canvas.drawLine(WIDTH - 340f, 515f, WIDTH - 92f, 515f, paint)
    }

    private fun drawCorner(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GOLD; strokeWidth = 0.8f; style = Paint.Style.STROKE }
        canvas.drawPath(Path().apply { moveTo(0f, 60f); quadTo(0f, 0f, 60f, 0f) }, paint)
        paint.style = Paint.Style.FILL
        listOf(Triple(3f, 37f, -15f), Triple(11f, 22f, 15f), Triple(23f, 11f, 45f), Triple(38f, 3f, 70f)).forEach { (x, y, rotation) ->
            canvas.save(); canvas.translate(x, y); canvas.rotate(rotation)
            canvas.drawPath(Path().apply { moveTo(0f, 0f); quadTo(-8f, -9f, 0f, -16f); quadTo(7f, -8f, 0f, 0f) }, paint)
            canvas.restore()
        }
    }

    companion object {
        const val WIDTH = 842
        const val HEIGHT = 595
        private val INK = Color.rgb(26, 39, 71)
        private val IVORY = Color.rgb(252, 250, 245)
        private val GOLD = Color.rgb(178, 142, 71)
        private val PALE_GOLD = Color.rgb(225, 214, 186)
        private val MUTED = Color.rgb(101, 105, 115)
    }
}
