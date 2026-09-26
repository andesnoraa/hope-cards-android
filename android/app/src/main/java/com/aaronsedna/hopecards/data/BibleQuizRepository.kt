package com.aaronsedna.hopecards.data

import android.content.Context
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.QuizQuestion
import com.aaronsedna.hopecards.model.QuizSession
import org.json.JSONObject

/** Bundled original questions: no network, API key, or third-party question service. */
class BibleQuizRepository(context: Context) {
    private val assets = context.applicationContext.assets

    fun load(language: QuizLanguage): List<QuizQuestion> {
        val root = assets.open("quiz/questions.json").bufferedReader().use { JSONObject(it.readText()) }
        require(root.getInt("schemaVersion") == 1)
        val rows = root.getJSONArray("questions")
        val questions = List(rows.length()) { index ->
            val row = rows.getJSONObject(index)
            val text = row.getJSONObject("locales").getJSONObject(language.code)
            val options = text.getJSONArray("options")
            QuizQuestion(
                id = row.getString("id"),
                reference = row.getString("reference"),
                question = text.getString("question"),
                options = List(options.length()) { options.getString(it) },
                correctIndex = row.getInt("correctIndex"),
                explanation = text.getString("explanation"),
            )
        }
        require(questions.size >= QuizSession.ROUND_SIZE)
        require(questions.map { it.id }.distinct().size == questions.size)
        return questions
    }
}
