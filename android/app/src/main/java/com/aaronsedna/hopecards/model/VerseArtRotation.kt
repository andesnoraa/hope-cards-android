package com.aaronsedna.hopecards.model

import kotlin.random.Random

/** A shuffled cycle of compatible photos. A visit never changes while scrolling or exporting. */
object VerseArtRotation {
    fun background(candidates: List<String>, verseId: String, visit: Long): String {
        require(candidates.isNotEmpty())
        val shuffled = candidates.distinct().sorted().shuffled(Random(verseId.hashCode()))
        val index = Math.floorMod(visit, shuffled.size.toLong()).toInt()
        return shuffled[index]
    }
}
