package com.aaronsedna.hopecards.model

/** Stable visual shuffle: separate scene families and repeated photos, including in Saved. */
object VerseArtOrder {
    data class Scene(val background: String, val family: String)

    fun arrange(artworks: List<VerseArtwork>, scenes: Map<String, Scene>): List<VerseArtwork> {
        val remaining = artworks.sortedBy { it.id.hashCode().and(Int.MAX_VALUE) }.toMutableList()
        val result = mutableListOf<VerseArtwork>()
        val recent = mutableListOf<Scene>()
        while (remaining.isNotEmpty()) {
            val counts = remaining.groupingBy { scenes[it.id]?.family }.eachCount()
            val next = remaining.minWith(compareBy<VerseArtwork> { art ->
                // Do not strand a large family at the end of an otherwise varied gallery.
                val nextFamily = scenes[art.id]?.family
                val slots = remaining.size - 1
                counts.maxOf { (family, count) ->
                    val left = count - if (family == nextFamily) 1 else 0
                    val capacity = if (family == nextFamily) slots / 2 else (slots + 1) / 2
                    (left - capacity).coerceAtLeast(0)
                }
            }.thenBy { art ->
                val scene = scenes[art.id]
                if (scene == null) 0 else recent.takeLast(4).reversed().withIndex().sumOf { (distance, prior) ->
                    (if (scene.family == prior.family) when (distance) { 0 -> 1000; 1 -> 350; else -> 0 } else 0) +
                        (if (scene.background == prior.background) 200 else 0)
                }
            }.thenByDescending { counts[scenes[it.id]?.family] ?: 0 })
            remaining.remove(next)
            result.add(next)
            scenes[next.id]?.let(recent::add)
        }
        return result
    }
}
