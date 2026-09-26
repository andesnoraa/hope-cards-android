package com.aaronsedna.hopecards.data

import android.content.Context
import android.content.SharedPreferences
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.QuizQuestion
import com.aaronsedna.hopecards.model.QuizQuestionRotation
import com.aaronsedna.hopecards.model.QuizSession
import org.json.JSONArray
import org.json.JSONObject

/** Reserve each new round once. Recomposition, score review and restoration do not draw again. */
internal class QuizRoundHistory(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.applicationContext.getSharedPreferences("bible-quiz-history", Context.MODE_PRIVATE))

    @Synchronized
    fun nextRound(language: QuizLanguage, questions: List<QuizQuestion>): QuizSession {
        val key = "deck-${language.code}"
        val history = runCatching {
            val json = JSONObject(preferences.getString(key, null) ?: "{}")
            fun ids(name: String): List<String> {
                val values = json.optJSONArray(name) ?: return emptyList()
                return List(values.length()) { values.getString(it) }
            }
            QuizQuestionRotation(ids("remaining"), ids("known"), ids("previous"))
        }.getOrDefault(QuizQuestionRotation())
        val (ids, next) = history.draw(questions.map { it.id })
        val json = JSONObject().put("remaining", JSONArray(next.remaining))
            .put("known", JSONArray(next.known)).put("previous", JSONArray(next.previousRound))
        preferences.edit().putString(key, json.toString()).apply()
        return QuizSession(questionIds = ids)
    }
}
