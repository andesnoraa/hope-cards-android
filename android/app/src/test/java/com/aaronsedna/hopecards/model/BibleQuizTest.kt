package com.aaronsedna.hopecards.model

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class BibleQuizTest {
    private val bank = (0 until 30).map {
        QuizQuestion("q$it", "John 1:1", "Question $it", listOf("A", "B", "C", "D"), it % 4, "Explanation")
    }

    @Test fun everyBibleEditionHasAQuizInItsOwnLanguage() {
        val expected = mapOf(
            Translation.BSB to QuizLanguage.ENGLISH, Translation.BBE to QuizLanguage.ENGLISH,
            Translation.KJV to QuizLanguage.ENGLISH, Translation.WEB to QuizLanguage.ENGLISH,
            Translation.MAL1910 to QuizLanguage.MALAYALAM, Translation.LUT1912 to QuizLanguage.GERMAN,
            Translation.LSG1910 to QuizLanguage.FRENCH, Translation.RIV1927 to QuizLanguage.ITALIAN,
            Translation.RV1909 to QuizLanguage.SPANISH, Translation.ADB1905 to QuizLanguage.TAGALOG,
        )
        assertEquals(Translation.entries.toSet(), expected.keys)
        expected.forEach { (edition, language) -> assertEquals(language, QuizLanguage.forTranslation(edition)) }
    }

    @Test fun roundsContainTenDifferentQuestionsAndDifferentSeedsGiveDifferentRounds() {
        val first = QuizSession.start(bank, Random(1))
        assertEquals(10, first.questionIds.size)
        assertEquals(10, first.questionIds.distinct().size)
        assertTrue(first.isValidFor(bank))
        assertNotEquals(first.questionIds, QuizSession.start(bank, Random(2)).questionIds)
    }

    @Test fun cannotSkipOrSubmitWithoutAnAnswerAndCannotChangeASubmittedAnswer() {
        val started = QuizSession.start(bank, Random(1))
        assertEquals(started, started.submit())
        assertEquals(started, started.advance())
        assertEquals(started, started.choose(-1))
        assertEquals(started, started.choose(4))
        val checked = started.choose(2).submit()
        assertEquals(listOf(2), checked.answers)
        assertEquals(checked, checked.submit())
        assertEquals(checked, checked.choose(1))
        val next = checked.advance()
        assertEquals(1, next.position)
        assertEquals(-1, next.selectedIndex)
        assertFalse(next.checked)
        assertTrue(next.isValidFor(bank))
    }

    @Test fun scoreCountsCorrectAnswersExactlyOnceAndResultsRequireTheLastAnswer() {
        var session = QuizSession.start(bank, Random(3))
        val byId = bank.associateBy { it.id }
        repeat(10) { index ->
            val correct = byId.getValue(session.questionIds[index]).correctIndex
            session = session.choose(if (index % 2 == 0) correct else (correct + 1) % 4).submit()
            assertFalse(session.finished)
            assertTrue(session.isValidFor(bank))
            session = session.advance()
        }
        assertTrue(session.finished)
        assertEquals(5, session.score(byId))
        assertTrue(session.isValidFor(bank))
        assertEquals(session, session.advance())
        assertEquals(session, session.choose(0).submit())
    }

    @Test fun retiredQuestionsAndCorruptSavedRoundsAreRejected() {
        val valid = QuizSession.start(bank, Random(4)).choose(1).submit()
        assertFalse(valid.isValidFor(bank.filterNot { it.id == valid.questionIds[0] }))
        assertFalse(valid.copy(position = 12).isValidFor(bank))
        assertFalse(valid.copy(answers = listOf(8)).isValidFor(bank))
        assertFalse(valid.copy(finished = true).isValidFor(bank))
        assertFalse(valid.copy(selectedIndex = 3).isValidFor(bank))
        assertFalse(valid.copy(questionIds = List(10) { "q1" }).isValidFor(bank))
        assertTrue(QuizSession().isValidFor(bank))
    }
}
