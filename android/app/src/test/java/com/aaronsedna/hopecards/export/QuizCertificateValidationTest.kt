package com.aaronsedna.hopecards.export

import org.junit.Assert.*
import org.junit.Test

class QuizCertificateValidationTest {
    private val details = CertificateDetails("Bible Quiz Competition", "27 September 2026")
    private val people = listOf(CertificateParticipant("Anna"))

    @Test fun requiredFieldsAndBatchLimitsAreEnforced() {
        assertEquals(CertificateValidationError.REQUIRED, QuizCertificates.validate(details.copy(competitionTitle = " "), people))
        assertEquals(CertificateValidationError.REQUIRED, QuizCertificates.validate(details.copy(date = " "), people))
        assertEquals(CertificateValidationError.REQUIRED, QuizCertificates.validate(details, emptyList()))
        assertEquals(CertificateValidationError.REQUIRED, QuizCertificates.validate(details, listOf(CertificateParticipant(" "))))
        assertNull(QuizCertificates.validate(details, List(50) { CertificateParticipant("Same Name") }))
        assertEquals(CertificateValidationError.TOO_MANY_PARTICIPANTS, QuizCertificates.validate(details, List(51) { CertificateParticipant("Anna") }))
    }

    @Test fun validLimitsAndMalformedValuesAreHandledWithoutTruncatingNames() {
        assertNull(QuizCertificates.validate(CertificateDetails("W".repeat(120), "W".repeat(60), "W".repeat(120)), listOf(CertificateParticipant("W".repeat(100)))))
        assertEquals(CertificateValidationError.TITLE_TOO_LONG, QuizCertificates.validate(details.copy(competitionTitle = "W".repeat(121)), people))
        assertEquals(CertificateValidationError.DATE_TOO_LONG, QuizCertificates.validate(details.copy(date = "W".repeat(61)), people))
        assertEquals(CertificateValidationError.ORGANIZER_TOO_LONG, QuizCertificates.validate(details.copy(organizer = "W".repeat(121)), people))
        assertEquals(CertificateValidationError.NAME_TOO_LONG, QuizCertificates.validate(details, listOf(CertificateParticipant("W".repeat(101)))))
        assertEquals(CertificateValidationError.INVALID_NAME, QuizCertificates.validate(details, listOf(CertificateParticipant("Anna\nJoseph"))))
        assertNull(QuizCertificates.validate(details, listOf(CertificateParticipant("അന്ന മറിയം"))))
        assertNull(QuizCertificates.validate(details, listOf(CertificateParticipant("Anna"), CertificateParticipant("Anna"))))
    }
}
