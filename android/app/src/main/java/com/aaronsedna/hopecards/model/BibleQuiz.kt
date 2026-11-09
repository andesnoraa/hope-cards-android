package com.aaronsedna.hopecards.model

import kotlin.random.Random

enum class QuizLanguage(val code: String, val nativeName: String) {
    ENGLISH("en", "English"), MALAYALAM("ml", "മലയാളം"), GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"), ITALIAN("it", "Italiano"),
    SPANISH("es", "Español"), TAGALOG("fil", "Filipino");

    companion object {
        fun forTranslation(translation: Translation): QuizLanguage = when (translation) {
            Translation.BSB, Translation.BBE, Translation.KJV, Translation.WEB -> ENGLISH
            Translation.MAL1910 -> MALAYALAM
            Translation.LUT1912 -> GERMAN
            Translation.LSG1910 -> FRENCH
            Translation.RIV1927 -> ITALIAN
            Translation.RV1909 -> SPANISH
            Translation.ADB1905 -> TAGALOG
        }
    }
}

data class QuizQuestion(
    val id: String,
    val reference: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
) {
    init {
        require(id.isNotBlank() && reference.isNotBlank() && question.isNotBlank())
        require(options.size == 4 && options.all { it.isNotBlank() } && options.distinct().size == 4)
        require(correctIndex in options.indices)
    }
}

/** Answers are locked on submission, so repeated taps cannot award extra points. */
data class QuizSession(
    val questionIds: List<String> = emptyList(),
    val answers: List<Int> = emptyList(),
    val position: Int = 0,
    val selectedIndex: Int = -1,
    val finished: Boolean = false,
    val timedOut: Boolean = false,
    /** Submission durations in question order. Null means timing was off or unavailable. */
    val answerTimesMillis: List<Long?> = emptyList(),
) {
    val started get() = questionIds.isNotEmpty()
    val checked get() = answers.size > position

    fun choose(index: Int): QuizSession =
        if (started && !finished && !checked && index in 0..3) copy(selectedIndex = index) else this

    fun submit(timeTakenMillis: Long? = null): QuizSession =
        if (started && !finished && !checked && selectedIndex in 0..3)
            copy(answers = answers + selectedIndex, answerTimesMillis = appendTime(timeTakenMillis)) else this

    private fun appendTime(timeTakenMillis: Long?): List<Long?> =
        answers.indices.map { answerTimesMillis.getOrNull(it) } +
            timeTakenMillis?.coerceIn(0L, QUIZ_COUNTDOWN_DURATION_MILLIS)

    fun advance(): QuizSession = when {
        !checked || finished -> this
        position == questionIds.lastIndex -> copy(finished = true)
        else -> copy(position = position + 1, selectedIndex = -1)
    }

    /** Lock the current choice, or record an unanswered question, and continue the round. */
    fun expireQuestion(timeTakenMillis: Long? = null): QuizSession {
        if (!started || finished || checked) return this
        val answer = selectedIndex.takeIf { it in 0..3 } ?: -1
        val submitted = answers + answer
        return if (position == questionIds.lastIndex) {
            copy(answers = submitted, selectedIndex = answer, finished = true, timedOut = true,
                answerTimesMillis = appendTime(timeTakenMillis))
        } else {
            copy(answers = submitted, position = position + 1, selectedIndex = -1,
                answerTimesMillis = appendTime(timeTakenMillis))
        }
    }

    /** Lock the current choice, then mark every remaining question as unanswered. */
    fun finishOnTimeout(): QuizSession {
        if (!started || finished) return this
        val submitted = if (checked) answers else answers + (selectedIndex.takeIf { it in 0..3 } ?: -1)
        val completed = submitted + List((questionIds.size - submitted.size).coerceAtLeast(0)) { -1 }
        return copy(answers = completed, position = questionIds.lastIndex,
            selectedIndex = completed.last(), finished = true, timedOut = true,
            answerTimesMillis = completed.indices.map { answerTimesMillis.getOrNull(it) })
    }

    fun score(questions: Map<String, QuizQuestion>): Int =
        answers.indices.count { questions[questionIds[it]]?.correctIndex == answers[it] }

    fun isValidFor(questions: List<QuizQuestion>): Boolean {
        if (!started) return this == QuizSession()
        if (answerTimesMillis.isNotEmpty() && answerTimesMillis.size != answers.size ||
            answerTimesMillis.any { it != null && it !in 0L..QUIZ_COUNTDOWN_DURATION_MILLIS }) return false
        if (questionIds.size > ROUND_SIZE || questionIds.distinct().size != questionIds.size ||
            questionIds.any { id -> questions.none { it.id == id } } || position !in questionIds.indices ||
            selectedIndex !in -1..3) return false
        if (answers.size !in position..position + 1 || answers.any { it !in -1..3 } ||
            timedOut && !finished) return false
        // Expired questions advance immediately, so only earlier answers can be unanswered during play.
        if (checked && (selectedIndex != answers[position] || !finished && selectedIndex !in 0..3)) return false
        return !finished || position == questionIds.lastIndex && answers.size == questionIds.size &&
            (timedOut || selectedIndex in 0..3)
    }

    companion object {
        const val ROUND_SIZE = 10
        fun start(questions: List<QuizQuestion>, random: Random = Random.Default): QuizSession =
            QuizSession(questionIds = questions.shuffled(random).take(ROUND_SIZE).map { it.id })
    }
}
