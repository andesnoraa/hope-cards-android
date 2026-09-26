package com.aaronsedna.hopecards.model

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class QuizQuestionRotationTest {
    private val bank = (1..200).map { "q$it" }

    @Test fun twentyRoundsUseEveryQuestionBeforeRepeating() {
        var deck = QuizQuestionRotation()
        val seen = mutableSetOf<String>()
        var previous = emptyList<String>()
        repeat(20) {
            val (round, next) = deck.draw(bank, Random(it))
            assertEquals(10, round.size)
            assertEquals(10, round.distinct().size)
            assertTrue(round.none { it in seen })
            seen.addAll(round)
            previous = round
            deck = next
        }
        assertEquals(bank.toSet(), seen)
        val (nextRound, _) = deck.draw(bank, Random(21))
        assertTrue(nextRound.none { it in previous })
    }

    @Test fun contentUpdatesRetainUnseenQuestionsAndIncludeAdditions() {
        val (first, deck) = QuizQuestionRotation().draw(bank.take(30), Random(4))
        val (second, next) = deck.draw(bank.drop(1), Random(5))
        assertFalse("q1" in second || "q1" in next.remaining)
        assertTrue(second.none { it in first })
        assertEquals(bank.drop(30).toSet(), next.remaining.filter { it in bank.drop(30) }.toSet())
        assertEquals(bank.drop(1).toSet(), next.known.toSet())
    }

    @Test fun shortDecksAndCycleBoundariesNeverDuplicateWithinARound() {
        for (size in listOf(1, 4, 10, 13, 23)) {
            val ids = bank.take(size)
            var deck = QuizQuestionRotation()
            repeat(20) { seed ->
                val (round, next) = deck.draw(ids, Random(seed))
                assertEquals(minOf(size, 10), round.size)
                assertEquals(round.size, round.distinct().size)
                assertTrue(round.all { it in ids })
                deck = next
            }
        }
    }

    @Test fun staleAndDuplicateSavedIdsAreDiscarded() {
        val deck = QuizQuestionRotation(listOf("gone", "q1", "q1", "q2"), bank, emptyList())
        val (round, _) = deck.draw(bank, Random(1))
        assertEquals(10, round.distinct().size)
        assertFalse("gone" in round)
    }
}
