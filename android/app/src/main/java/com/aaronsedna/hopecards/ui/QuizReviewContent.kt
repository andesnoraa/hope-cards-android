package com.aaronsedna.hopecards.ui

import android.content.Context
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.*

/** One completed-round snapshot supplies both the on-screen review and its shared PDF. */
class QuizReviewContent(context: Context, translation: Translation, session: QuizSession, questions: List<QuizQuestion>) {
    enum class Outcome { CORRECT, INCORRECT, UNANSWERED }

    data class Answer(
        val id: String,
        val prompt: String,
        val passage: String?,
        val outcome: Outcome,
        val status: String,
        val selectedAnswer: String,
        val correctAnswer: String,
        val explanation: String,
        val reference: String,
    )

    val resources = context.forQuizTranslation(translation).resources
    val title = resources.getString(R.string.quiz_report_title)
    val reviewTitle = resources.getString(R.string.quiz_review)
    val explanationLabel = resources.getString(R.string.quiz_review_explanation)
    val edition = resources.getString(R.string.quiz_selected_edition,
        QuizLanguage.forTranslation(translation).nativeName, translation.label)
    private val byId = questions.associateBy { it.id }
    val score = resources.getString(R.string.quiz_score, session.score(byId), session.questionIds.size)
    val answers: List<Answer>

    init {
        require(session.finished && session.questionIds.isNotEmpty() && session.answers.size == session.questionIds.size)
        answers = session.questionIds.mapIndexed { index, id ->
            val question = byId.getValue(id)
            val selection = session.answers[index]
            require(selection == -1 || selection in question.options.indices)
            val outcome = when (selection) {
                -1 -> Outcome.UNANSWERED
                question.correctIndex -> Outcome.CORRECT
                else -> Outcome.INCORRECT
            }
            val status = resources.getString(when (outcome) {
                Outcome.CORRECT -> R.string.quiz_correct
                Outcome.INCORRECT -> R.string.quiz_incorrect
                Outcome.UNANSWERED -> R.string.quiz_unanswered
            })
            Answer(
                id = id,
                prompt = "${index + 1}. ${question.question.substringBefore("\n\n")}",
                passage = question.question.substringAfter("\n\n", "").takeIf { it.isNotBlank() },
                outcome = outcome,
                status = status,
                selectedAnswer = resources.getString(R.string.quiz_your_answer,
                    question.options.getOrNull(selection) ?: resources.getString(R.string.quiz_unanswered)),
                correctAnswer = resources.getString(R.string.quiz_correct_answer, question.options[question.correctIndex]),
                explanation = question.explanation,
                reference = "${resources.getString(R.string.quiz_reference)}: ${BibleReferenceFormatter.formatFull(question.reference, translation)}",
            )
        }
    }
}
