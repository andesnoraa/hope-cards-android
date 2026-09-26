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
        require(id.isNotBlank() && reference.isNotBlank() && question.isNotBlank() && explanation.isNotBlank())
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
) {
    val started get() = questionIds.isNotEmpty()
    val checked get() = answers.size > position

    fun choose(index: Int): QuizSession =
        if (started && !finished && !checked && index in 0..3) copy(selectedIndex = index) else this

    fun submit(): QuizSession =
        if (started && !finished && !checked && selectedIndex in 0..3) copy(answers = answers + selectedIndex) else this

    fun advance(): QuizSession = when {
        !checked || finished -> this
        position == questionIds.lastIndex -> copy(finished = true)
        else -> copy(position = position + 1, selectedIndex = -1)
    }

    fun score(questions: Map<String, QuizQuestion>): Int =
        answers.indices.count { questions[questionIds[it]]?.correctIndex == answers[it] }

    fun isValidFor(questions: List<QuizQuestion>): Boolean {
        if (!started) return this == QuizSession()
        return questionIds.size <= ROUND_SIZE && questionIds.distinct().size == questionIds.size &&
            questionIds.all { id -> questions.any { it.id == id } } && position in questionIds.indices &&
            answers.size in position..position + 1 && answers.all { it in 0..3 } && selectedIndex in -1..3 &&
            (!checked || selectedIndex == answers[position]) &&
            (!finished || position == questionIds.lastIndex && answers.size == questionIds.size)
    }

    companion object {
        const val ROUND_SIZE = 10
        fun start(questions: List<QuizQuestion>, random: Random = Random.Default): QuizSession =
            QuizSession(questionIds = questions.shuffled(random).take(ROUND_SIZE).map { it.id })
    }
}
