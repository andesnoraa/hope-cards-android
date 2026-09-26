package com.aaronsedna.hopecards.model

import kotlin.random.Random

/** A finite shuffled deck, retaining unseen questions across rounds and content updates. */
internal data class QuizQuestionRotation(
    val remaining: List<String> = emptyList(),
    val known: List<String> = emptyList(),
    val previousRound: List<String> = emptyList(),
) {
    fun draw(availableIds: List<String>, random: Random = Random.Default): Pair<List<String>, QuizQuestionRotation> {
        val available = availableIds.distinct()
        require(available.isNotEmpty())
        val allowed = available.toSet()
        val queue = remaining.filter { it in allowed }.distinct().toMutableList()
        val added = available.filter { it !in known && it !in queue }.shuffled(random)
        queue.addAll(added)
        val round = mutableListOf<String>()
        repeat(minOf(QuizSession.ROUND_SIZE, available.size)) {
            if (queue.isEmpty()) {
                // At a cycle boundary, delay the previous round when other questions remain.
                val refill = available.filterNot { it in round }.shuffled(random)
                queue.addAll(refill.filterNot { it in previousRound })
                queue.addAll(refill.filter { it in previousRound })
            }
            round += queue.removeAt(0)
        }
        return round to QuizQuestionRotation(queue, available, round)
    }
}
