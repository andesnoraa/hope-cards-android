package com.aaronsedna.hopecards.data

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.QuizLanguage
import org.junit.Assert.*
import org.junit.Test

class QuizRoundHistoryInstrumentedTest {
    @Test fun historySurvivesRepositoryRecreationAndIsIndependentPerLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("quiz-history-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        try {
            val bank = BibleQuizRepository(context).load(QuizLanguage.ENGLISH)
            val seen = mutableSetOf<String>()
            repeat(20) {
                val round = QuizRoundHistory(prefs).nextRound(QuizLanguage.ENGLISH, bank)
                assertTrue(round.questionIds.none { it in seen })
                seen.addAll(round.questionIds)
            }
            assertEquals(200, seen.size)
            val english = prefs.getString("deck-en", null)
            val malayalam = BibleQuizRepository(context).load(QuizLanguage.MALAYALAM)
            assertEquals(10, QuizRoundHistory(prefs).nextRound(QuizLanguage.MALAYALAM, malayalam).questionIds.size)
            assertEquals(english, prefs.getString("deck-en", null))
        } finally { prefs.edit().clear().commit() }
    }
}
