package com.aaronsedna.hopecards.model

import org.junit.Assert.*
import org.junit.Test

class VerseArtCatalogTest {
    @Test fun sixCategoriesKeepDistinctVersesIncludingTheNewFaithfulArtwork() {
        assertEquals(181, VerseArtCatalog.artworks.size)
        assertEquals(6, VerseArtCatalog.categories.size)
        assertEquals(181, VerseArtCatalog.artworks.map { it.verseId }.toSet().size)
        val categorized = VerseArtCatalog.categories.flatMap { category ->
            val cards = VerseArtCatalog.inCategory(category.id, emptySet())
            assertEquals(if (category.id == "hope") 31 else 30, cards.size)
            assertTrue(cards.all { it.categoryId == category.id })
            category.artworkIds
        }
        assertEquals(181, categorized.size)
        assertEquals(VerseArtCatalog.artworks.map { it.id }.toSet(), categorized.toSet())
    }

    @Test fun savedArtworkUsesExistingVerseFavorites() {
        val selected = listOf(VerseArtCatalog.artworks.first(), VerseArtCatalog.artworks.last())
        assertEquals(selected, VerseArtCatalog.inCategory(VerseArtCatalog.SAVED, selected.map { it.verseId }.toSet()))
        assertTrue(VerseArtCatalog.inCategory(VerseArtCatalog.SAVED, emptySet()).isEmpty())
        assertEquals(181, VerseArtCatalog.inCategory(VerseArtCatalog.ALL, emptySet()).size)
    }
}
