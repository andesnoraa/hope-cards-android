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

    @Test fun firstAndMiddleQuestionExpiryAdvanceAndAllowNormalCompletionAfterUnansweredQuestions() {
        val questions = bank.take(4)
        val started = QuizSession(questionIds = questions.map { it.id })
        val afterFirst = started.expireQuestion()
        assertEquals(listOf(-1), afterFirst.answers)
        assertEquals(1, afterFirst.position)
        assertEquals(-1, afterFirst.selectedIndex)
        assertFalse(afterFirst.checked)
        assertFalse(afterFirst.finished)
        assertFalse(afterFirst.timedOut)
        assertTrue(afterFirst.isValidFor(questions))

        val afterMiddle = afterFirst.choose(1).submit().advance().expireQuestion()
        assertEquals(listOf(-1, 1, -1), afterMiddle.answers)
        assertEquals(3, afterMiddle.position)
        assertEquals(-1, afterMiddle.selectedIndex)
        assertFalse(afterMiddle.checked)
        assertFalse(afterMiddle.finished)
        assertFalse(afterMiddle.timedOut)
        assertTrue(afterMiddle.isValidFor(questions))

        val checkedLast = afterMiddle.choose(3).submit()
        assertTrue(checkedLast.isValidFor(questions))
        val completed = checkedLast.advance()
        assertEquals(listOf(-1, 1, -1, 3), completed.answers)
        assertEquals(2, completed.score(questions.associateBy { it.id }))
        assertTrue(completed.finished)
        assertFalse(completed.timedOut)
        assertTrue(completed.isValidFor(questions))
    }

    @Test fun questionExpirySubmitsTheCurrentChoiceAndOnlyFinalExpiryMarksTheRoundTimedOut() {
        val questions = bank.take(2)
        val first = QuizSession(questionIds = questions.map { it.id }).choose(0).expireQuestion()
        assertEquals(listOf(0), first.answers)
        assertEquals(1, first.position)
        assertEquals(-1, first.selectedIndex)
        assertFalse(first.timedOut)
        assertTrue(first.isValidFor(questions))

        val completed = first.choose(1).expireQuestion()
        assertEquals(listOf(0, 1), completed.answers)
        assertEquals(1, completed.position)
        assertEquals(1, completed.selectedIndex)
        assertEquals(2, completed.score(questions.associateBy { it.id }))
        assertTrue(completed.finished)
        assertTrue(completed.timedOut)
        assertTrue(completed.isValidFor(questions))
        assertEquals(completed, completed.expireQuestion())
    }

    @Test fun finalQuestionExpiryRecordsUnansweredAndDoesNotRepeatOrChangeCheckedAnswers() {
        val started = QuizSession(questionIds = listOf("q0"))
        val expired = started.expireQuestion()
        assertEquals(listOf(-1), expired.answers)
        assertEquals(-1, expired.selectedIndex)
        assertTrue(expired.finished)
        assertTrue(expired.timedOut)
        assertTrue(expired.isValidFor(bank))
        assertEquals(0, expired.score(bank.associateBy { it.id }))
        assertEquals(expired, expired.expireQuestion().choose(0).submit().advance())
        assertEquals(QuizSession(), QuizSession().expireQuestion())

        val checked = QuizSession(questionIds = listOf("q0", "q1")).choose(0).submit()
        assertEquals(checked, checked.expireQuestion())
        val checkedLast = started.choose(0).submit()
        assertEquals(checkedLast, checkedLast.expireQuestion())
        val completed = checkedLast.advance()
        assertEquals(completed, completed.expireQuestion())
        assertFalse(completed.timedOut)
    }

    @Test fun restoredAnswerGapsAreValidButInvalidAnswerBoundsAndSessionShapesAreRejected() {
        val active = QuizSession(questionIds = listOf("q0", "q1", "q2", "q3"),
            answers = listOf(-1, 1, -1), position = 3)
        assertTrue(active.isValidFor(bank))
        assertTrue(active.choose(3).submit().isValidFor(bank))
        assertTrue(active.choose(3).submit().advance().isValidFor(bank))
        assertTrue(active.choose(3).expireQuestion().isValidFor(bank))
        assertTrue(active.expireQuestion().isValidFor(bank))
        assertFalse(active.copy(timedOut = true).isValidFor(bank))
        assertFalse(active.copy(finished = true).isValidFor(bank))
        assertFalse(active.copy(position = 2).isValidFor(bank))
        assertFalse(active.copy(answers = listOf(-1, 1)).isValidFor(bank))
        assertFalse(active.copy(answers = listOf(-1, 1, -1, 3, 0)).isValidFor(bank))
        assertFalse(active.copy(answers = listOf(-2, 1, -1)).isValidFor(bank))
        assertFalse(active.copy(answers = listOf(-1, 4, -1)).isValidFor(bank))
        assertFalse(active.copy(selectedIndex = 4).isValidFor(bank))
        assertFalse(active.expireQuestion().copy(timedOut = false).isValidFor(bank))
        assertFalse(active.choose(3).submit().copy(selectedIndex = 2).isValidFor(bank))
    }

    @Test fun expiryOfFreshRoundMarksEveryQuestionUnansweredAndScoresZero() {
        val expired = QuizSession.start(bank, Random(5)).finishOnTimeout()
        assertEquals(List(10) { -1 }, expired.answers)
        assertEquals(9, expired.position)
        assertEquals(-1, expired.selectedIndex)
        assertTrue(expired.finished)
        assertTrue(expired.timedOut)
        assertTrue(expired.isValidFor(bank))
        assertEquals(0, expired.score(bank.associateBy { it.id }))
    }

    @Test fun expiryPreservesSubmittedAnswersAndKeepsRemainingQuestionsUnanswered() {
        val questions = bank.take(4)
        val started = QuizSession(questionIds = questions.map { it.id })
        val partial = started.choose(0).submit().advance().choose(1).submit().advance()
        val expired = partial.finishOnTimeout()
        assertEquals(listOf(0, 1, -1, -1), expired.answers)
        assertEquals(2, expired.score(questions.associateBy { it.id }))
        assertTrue(expired.isValidFor(questions))
    }

    @Test fun expiryLocksCurrentSelectedChoiceEvenBeforeCheckAnswer() {
        val questions = bank.take(3)
        val partial = QuizSession(questionIds = questions.map { it.id }).choose(0).submit().advance().choose(1)
        val expired = partial.finishOnTimeout()
        assertEquals(listOf(0, 1, -1), expired.answers)
        assertEquals(2, expired.score(questions.associateBy { it.id }))
        assertTrue(expired.isValidFor(questions))
        val selectedLast = partial.submit().advance().choose(2).finishOnTimeout()
        assertEquals(listOf(0, 1, 2), selectedLast.answers)
        assertEquals(2, selectedLast.selectedIndex)
        assertTrue(selectedLast.timedOut)
        assertTrue(selectedLast.isValidFor(questions))
    }

    @Test fun expiryDuringCheckedFeedbackDoesNotDuplicateTheAnswer() {
        val questions = bank.take(3)
        val checked = QuizSession(questionIds = questions.map { it.id }).choose(0).submit()
        val expired = checked.finishOnTimeout()
        assertEquals(listOf(0, -1, -1), expired.answers)
        assertTrue(expired.isValidFor(questions))
        val checkedLast = checked.advance().choose(1).submit().advance().choose(2).submit()
        val finalExpired = checkedLast.finishOnTimeout()
        assertEquals(listOf(0, 1, 2), finalExpired.answers)
        assertEquals(2, finalExpired.selectedIndex)
        assertTrue(finalExpired.isValidFor(questions))
    }

    @Test fun timeoutIsIdempotentAndCannotChangeAnAlreadyCompletedRound() {
        assertEquals(QuizSession(), QuizSession().finishOnTimeout())
        val expired = QuizSession.start(bank, Random(6)).choose(2).finishOnTimeout()
        assertEquals(expired, expired.finishOnTimeout())
        assertEquals(expired, expired.choose(0).submit().advance())
        val completed = QuizSession(questionIds = listOf("q0")).choose(0).submit().advance()
        assertFalse(completed.timedOut)
        assertEquals(completed, completed.finishOnTimeout())
        assertTrue(completed.isValidFor(bank))
    }

    @Test fun corruptExpiredResultsAndCheckedUnansweredQuestionsAreRejected() {
        val active = QuizSession(questionIds = listOf("q0", "q1", "q2"))
        val expired = active.choose(0).finishOnTimeout()
        assertTrue(expired.isValidFor(bank))
        assertFalse(active.copy(timedOut = true).isValidFor(bank))
        assertFalse(active.copy(answers = listOf(-1)).isValidFor(bank))
        assertFalse(expired.copy(finished = false).isValidFor(bank))
        assertFalse(expired.copy(timedOut = false).isValidFor(bank))
        assertFalse(expired.copy(position = 0).isValidFor(bank))
        assertFalse(expired.copy(answers = listOf(0, -1)).isValidFor(bank))
        assertFalse(expired.copy(answers = listOf(0, -1, -1, -1)).isValidFor(bank))
        assertTrue(expired.copy(answers = listOf(0, -1, 2), selectedIndex = 2).isValidFor(bank))
        assertFalse(expired.copy(answers = listOf(0, 4, -1)).isValidFor(bank))
        assertFalse(expired.copy(answers = listOf(0, -2, -1)).isValidFor(bank))
        assertFalse(expired.copy(selectedIndex = 0).isValidFor(bank))
        assertFalse(QuizSession(timedOut = true).isValidFor(bank))
    }
}
