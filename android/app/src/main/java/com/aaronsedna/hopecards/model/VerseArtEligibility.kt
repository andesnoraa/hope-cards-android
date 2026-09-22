package com.aaronsedna.hopecards.model

/** Keep complete Bible verses readable on a shared square card. Never truncate Scripture. */
object VerseArtEligibility {
    fun includes(verse: Verse): Boolean {
        val malayalam = verse.edition == Translation.MAL1910
        val words = verse.text.trim().split(Regex("\\s+")).size
        return verse.text.length <= (if (malayalam) 140 else 130) && words <= (if (malayalam) 16 else 22)
    }
}
