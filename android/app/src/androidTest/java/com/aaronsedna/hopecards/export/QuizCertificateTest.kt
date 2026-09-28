package com.aaronsedna.hopecards.export

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.forQuizTranslation
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class QuizCertificateTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val directory get() = File(context.cacheDir, "quiz-certificates").apply { mkdirs() }
    private val translations = listOf(Translation.BSB, Translation.MAL1910, Translation.LUT1912,
        Translation.LSG1910, Translation.RIV1927, Translation.RV1909, Translation.ADB1905)
    private val details = CertificateDetails("Bible Quiz Competition", "27 September 2026", "Sunday School")

    private fun assertLayout(page: QuizCertificateRenderer.Page) {
        assertEquals(842, page.width)
        assertEquals(595, page.height)
        page.blocks.forEach { block ->
            assertEquals(block.text, block.layout.text.toString())
            assertTrue(block.layout.lineCount > 0)
            assertEquals("Text was lost", block.text.length, block.layout.getLineEnd(block.layout.lineCount - 1))
            assertTrue(block.x >= 55)
            assertTrue(block.x + block.layout.width <= page.width - 55)
            assertTrue(block.y >= 76)
            assertTrue(block.bottom <= 544)
            repeat(block.layout.lineCount) { line ->
                assertEquals("Text was truncated", 0, block.layout.getEllipsisCount(line))
                assertTrue("Text exceeds its width: ${block.text}", block.layout.getLineWidth(line) <= block.layout.width + 0.5f)
            }
        }
        page.blocks.forEachIndexed { index, block ->
            page.blocks.drop(index + 1).forEach { other ->
                val horizontallyOverlaps = block.x < other.x + other.layout.width && other.x < block.x + block.layout.width
                val verticallyOverlaps = block.y < other.bottom && other.y < block.bottom
                assertFalse("Certificate text blocks overlap", horizontallyOverlaps && verticallyOverlaps)
            }
        }
        val renderedText = page.blocks.joinToString("\n") { it.text }
        assertFalse(renderedText.contains("play.google.com"))
        assertFalse(renderedText.contains("https://"))
        assertFalse(renderedText.contains("Created using Hope Cards"))
    }

    @Test fun everyLocaleAndMaximumLengthInputsFitOneA4LandscapePageWithoutEllipsis() {
        translations.forEach { translation ->
            val renderer = QuizCertificateRenderer(context, translation)
            val localized = context.forQuizTranslation(translation).resources
            val worstCase = CertificateDetails("W".repeat(120), "W".repeat(60), "W".repeat(120))
            listOf(
                details.copy(competitionTitle = localized.getString(R.string.quiz_certificate_competition_default)) to "Elizabeth Grace Mathew",
                worstCase to "W".repeat(100),
                CertificateDetails("ബൈബിൾ ക്വിസ് മത്സരം", "2026 സെപ്റ്റംബർ 27", "സൺഡേ സ്കൂൾ") to "അന്ന മറിയം ജോസഫ്",
                CertificateDetails("മ".repeat(120), "മ".repeat(60), "മ".repeat(120)) to "മ".repeat(100),
            ).forEach { (fields, name) ->
                val page = renderer.layout(fields, CertificateParticipant(name))
                assertLayout(page)
                assertTrue(page.blocks.any { it.text == name })
                assertTrue(page.blocks.any { it.text == localized.getString(R.string.quiz_certificate_pdf_title) })
                assertTrue(page.blocks.any { it.text == fields.competitionTitle })
                assertTrue(page.blocks.any { it.text == fields.organizer })
                val file = File.createTempFile("certificate-layout-", ".pdf", context.cacheDir)
                try {
                    file.outputStream().use { renderer.write(page, it) }
                    PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { pdf ->
                        assertEquals(1, pdf.pageCount)
                        pdf.openPage(0).use { assertEquals(842, it.width); assertEquals(595, it.height) }
                    }
                } finally { file.delete() }
            }
        }
    }

    @Test fun optionalOrganizerIsOmittedAndMalayalamNamesUseMalayalamFontInEnglishCertificate() {
        val page = QuizCertificateRenderer(context, Translation.BSB).layout(details.copy(organizer = ""), CertificateParticipant("അന്ന മറിയം"))
        assertLayout(page)
        assertFalse(page.blocks.any { it.text == "ORGANIZER" })
        val name = page.blocks.single { it.text == "അന്ന മറിയം" }
        val expected = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.noto_sans_malayalam_semibold)
        assertEquals(expected, name.layout.paint.typeface)
    }

    @Test fun certificateUsesIssuerAttestationEventAndBlankSignatureWithNaturalMalayalamOrder() {
        translations.forEach { translation ->
            val resources = context.forQuizTranslation(translation).resources
            val page = QuizCertificateRenderer(context, translation).layout(details, CertificateParticipant("Anna Mary"))
            val blocks = page.blocks
            assertTrue(blocks.indexOfFirst { it.text == details.organizer } < blocks.indexOfFirst { it.text == resources.getString(R.string.quiz_certificate_pdf_title) })
            val nameIndex = blocks.indexOfFirst { it.text == "Anna Mary" }
            val statementIndex = blocks.indexOfFirst { it.text == resources.getString(R.string.quiz_certificate_pdf_participation) }
            val eventIndex = blocks.indexOfFirst { it.text == details.competitionTitle }
            assertTrue(nameIndex < statementIndex && statementIndex < eventIndex)
            assertTrue(blocks.any { it.text == details.date })
            assertTrue(blocks.any { it.text == resources.getString(R.string.quiz_certificate_pdf_signature) && it.y > 515 })
            if (translation == Translation.MAL1910) {
                assertEquals("പങ്കാളിത്ത സാക്ഷ്യപത്രം", resources.getString(R.string.quiz_certificate_pdf_title))
                assertTrue(blocks[statementIndex].text.contains("പങ്കെടുത്തതായി സാക്ഷ്യപ്പെടുത്തുന്നു"))
                assertFalse(blocks.any { it.text == "ഈ സർട്ടിഫിക്കറ്റ് സമ്മാനിക്കുന്നു" || it.text == "പങ്കെടുത്തതിന്" })
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test fun batchHasUniqueFilesAndSingleOrMultipleReadableUrisWithCompanionOnlyStoreLink() = runBlocking {
        val progress = mutableListOf<Pair<Int, Int>>()
        val certificates = QuizCertificates.generate(context, Translation.BSB, details,
            listOf(CertificateParticipant("Alex Joseph"), CertificateParticipant("Alex Joseph"), CertificateParticipant("../../അന്ന"))) { done, total ->
            progress += done to total
        }
        try {
            assertEquals(listOf(0 to 3, 1 to 3, 2 to 3, 3 to 3), progress)
            assertEquals(3, certificates.map { it.file }.toSet().size)
            certificates.forEachIndexed { index, certificate ->
                assertEquals(directory.canonicalFile, certificate.file.canonicalFile.parentFile)
                assertTrue(certificate.file.name.startsWith("%02d-".format(index + 1)))
                assertFalse(certificate.file.name.contains("Alex"))
            }
            listOf(certificates.take(1), certificates).forEach { batch ->
                val intent = QuizCertificates.shareIntent(context, Translation.BSB, batch)
                assertEquals(if (batch.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE, intent.action)
                assertEquals("application/pdf", intent.type)
                assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
                assertEquals(0, intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val texts = if (batch.size == 1) listOf(requireNotNull(intent.getStringExtra(Intent.EXTRA_TEXT)))
                    else requireNotNull(intent.getCharSequenceArrayListExtra(Intent.EXTRA_TEXT)).map { it.toString() }
                assertEquals(batch.size, texts.size)
                val text = texts.first()
                assertTrue(texts.all { it == QuizCertificates.shareMessage(context, Translation.BSB) })
                assertTrue(text.contains("Generated using Hope Cards App"))
                assertTrue(text.contains("Install Hope Cards on Google Play"))
                assertTrue(text.contains("https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"))
                val uris = if (batch.size == 1) listOf(requireNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)))
                    else requireNotNull(intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM))
                assertEquals(batch.size, uris.size)
                assertEquals(batch.size, intent.clipData?.itemCount)
                assertTrue(intent.clipData!!.description.hasMimeType("application/pdf"))
                assertTrue(intent.clipData!!.description.hasMimeType("text/plain"))
                uris.forEachIndexed { index, uri ->
                    assertEquals("content", uri.scheme)
                    assertEquals("${context.packageName}.files", uri.authority)
                    assertEquals(uri, intent.clipData?.getItemAt(index)?.uri)
                    assertEquals(texts[index], intent.clipData?.getItemAt(index)?.text?.toString())
                    assertEquals("application/pdf", context.contentResolver.getType(uri))
                    PdfRenderer(context.contentResolver.openFileDescriptor(uri, "r")!!).use { pdf ->
                        assertEquals(1, pdf.pageCount)
                        pdf.openPage(0).use { assertEquals(842, it.width); assertEquals(595, it.height) }
                    }
                }
            }
            assertTrue(certificates.all { it.file.isFile })
        } finally { certificates.forEach { it.file.delete() } }
    }

    @Test fun cancellationAndFailureDeleteTheWholeIncompleteBatch() = runBlocking {
        val before = directory.listFiles().orEmpty().map { it.name }.toSet()
        val participants = listOf(CertificateParticipant("Anna"), CertificateParticipant("Joseph"))
        val job = launch {
            QuizCertificates.generate(context, Translation.BSB, details, participants) { done, _ ->
                if (done == 1) currentCoroutineContext().cancel(CancellationException("Stop batch"))
            }
            fail("Cancelled batch returned")
        }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(before, directory.listFiles().orEmpty().map { it.name }.toSet())
        try {
            QuizCertificates.generate(context, Translation.BSB, details, participants) { done, _ ->
                if (done == 1) throw IllegalStateException("Test export failure")
            }
            fail("Failed batch returned")
        } catch (error: IllegalStateException) {
            assertEquals("Test export failure", error.message)
        }
        assertEquals(before, directory.listFiles().orEmpty().map { it.name }.toSet())
    }

    @Test fun cleanupKeepsRecentSharedCertificatesAndRemovesFilesOlderThanSevenDays() = runBlocking {
        val old = File.createTempFile("old-certificate-", ".pdf", directory)
        val recent = File.createTempFile("recent-certificate-", ".pdf", directory)
        old.setLastModified(System.currentTimeMillis() - 8 * 24 * 60 * 60 * 1000L)
        val result = QuizCertificates.generate(context, Translation.BSB, details, listOf(CertificateParticipant("Anna")))
        try {
            assertFalse(old.exists())
            assertTrue(recent.exists())
            assertTrue(result.single().file.exists())
        } finally { old.delete(); recent.delete(); result.forEach { it.file.delete() } }
    }

    @Test fun createAllLanguageSamplesForVisualReview() = runBlocking {
        val samples = listOf(
            Triple(Translation.BSB, details, "Elizabeth Grace Mathew"),
            Triple(Translation.MAL1910, CertificateDetails("ബൈബിൾ ക്വിസ് മത്സരം", "2026 സെപ്റ്റംബർ 27", "സൺഡേ സ്കൂൾ"), "അന്ന മറിയം ജോസഫ്"),
            Triple(Translation.LUT1912, CertificateDetails("Bibelquiz-Wettbewerb", "27. September 2026", "Sonntagsschule"), "Anna Maria Müller"),
            Triple(Translation.LSG1910, CertificateDetails("Concours biblique", "27 septembre 2026", "École du dimanche"), "Élise Marie Laurent"),
            Triple(Translation.RIV1927, CertificateDetails("Concorso biblico", "27 settembre 2026", "Scuola domenicale"), "Anna Maria Rossi"),
            Triple(Translation.RV1909, CertificateDetails("Concurso bíblico", "27 de septiembre de 2026", "Escuela dominical"), "María Elena García"),
            Triple(Translation.ADB1905, CertificateDetails("Paligsahan sa Kaalaman sa Bibliya", "Setyembre 27, 2026", "Paaralang Panlinggo"), "Maria Isabel Santos"),
        )
        samples.forEach { (translation, fields, name) ->
            val generated = QuizCertificates.generate(context, translation, fields, listOf(CertificateParticipant(name)))
            val language = when (translation) {
                Translation.BSB -> "english"
                Translation.MAL1910 -> "malayalam"
                else -> com.aaronsedna.hopecards.model.QuizLanguage.forTranslation(translation).code
            }
            generated.single().file.copyTo(File(context.getExternalFilesDir(null), "hope-cards-certificate-$language.pdf"), overwrite = true)
            generated.forEach { it.file.delete() }
        }
    }
}
