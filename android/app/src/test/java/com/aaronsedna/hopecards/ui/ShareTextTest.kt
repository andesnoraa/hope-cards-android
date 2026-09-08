package com.aaronsedna.hopecards.ui

import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareTextTest {
    private val verse = Verse(
        id = "john-3-16",
        category = "Love",
        text = "For God so loved the world.",
        reference = "John 3:16",
        translation = "Berean Standard Bible (BSB)",
        tags = emptyList(),
        edition = Translation.BSB,
    )

    @Test
    fun `text share includes verse details attribution and install link`() {
        val text = verseShareText(verse)

        assertTrue(text.contains(verse.text))
        assertTrue(text.contains(verse.displayReference))
        assertTrue(text.contains(verse.translation))
        assertTrue(text.contains("Shared from Hope Cards ❤️"))
        assertTrue(text.contains("https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards"))
    }

    @Test
    fun `daily hope image share caption contains only attribution and install link`() {
        val text = dailyHopeImageShareText()

        assertEquals(
            "Shared from Hope Cards ❤️\n" +
                "https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards",
            text,
        )
        assertFalse(text.contains(verse.text))
        assertFalse(text.contains(verse.displayReference))
        assertFalse(text.contains(verse.translation))
    }
}
