package com.aaronsedna.hopecards.model

import org.junit.Assert.*
import org.junit.Test

class VerseArtOrderTest {
    @Test fun stableShuffleSeparatesRelatedScenesWithoutLosingCards() {
        val cards = VerseArtCatalog.artworks.take(30)
        val scenes = cards.mapIndexed { index, art ->
            art.id to VerseArtOrder.Scene("photo-${index % 15}", "family-${index % 5}")
        }.toMap()
        val ordered = VerseArtOrder.arrange(cards, scenes)
        assertEquals(cards.toSet(), ordered.toSet())
        assertEquals(cards.size, ordered.size)
        assertEquals(ordered, VerseArtOrder.arrange(cards.reversed(), scenes))
        ordered.windowed(3).forEach { group -> assertEquals(3, group.map { scenes[it.id]!!.family }.toSet().size) }
        ordered.windowed(5).forEach { group -> assertEquals(5, group.map { scenes[it.id]!!.background }.toSet().size) }
    }

    @Test fun unevenFamiliesDoNotAccumulateAtTheEnd() {
        val counts = listOf(33, 25, 24, 19, 16, 13, 7)
        val cards = VerseArtCatalog.artworks.take(counts.sum())
        val families = counts.flatMapIndexed { family, count -> List(count) { "family-$family" } }
        val scenes = cards.mapIndexed { i, art -> art.id to VerseArtOrder.Scene("photo-${families[i]}", families[i]) }.toMap()
        val ordered = VerseArtOrder.arrange(cards, scenes)
        assertEquals(cards.toSet(), ordered.toSet())
        ordered.zipWithNext().forEach { (a, b) -> assertNotEquals(scenes[a.id]!!.family, scenes[b.id]!!.family) }
    }

    @Test fun sparseSavedSelectionAndSingleFamilyStillKeepEveryCard() {
        val cards = VerseArtCatalog.artworks.take(7)
        val scenes = cards.associate { it.id to VerseArtOrder.Scene("same-photo", "lake") }
        assertEquals(cards.toSet(), VerseArtOrder.arrange(cards, scenes).toSet())
        assertEquals(emptyList<VerseArtwork>(), VerseArtOrder.arrange(emptyList(), scenes))
        assertEquals(cards.take(1), VerseArtOrder.arrange(cards.take(1), scenes))
    }
}
