package com.aaronsedna.hopecards.model

import org.junit.Assert.*
import org.junit.Test

class VerseArtRotationTest {
    @Test fun visitsExhaustEachCompatibleBackgroundBeforeRepeating() {
        val backgrounds = (1..39).map { "photo-$it" }
        val cycle = (0L..38L).map { VerseArtRotation.background(backgrounds, "psalm-56-3", it) }
        assertEquals(backgrounds.toSet(), cycle.toSet())
        assertEquals(cycle.first(), VerseArtRotation.background(backgrounds, "psalm-56-3", 39))
        assertNotEquals(cycle, (0L..38L).map { VerseArtRotation.background(backgrounds, "john-15-9", it) })
    }

    @Test fun restoredVisitAndCandidateOrderKeepTheSameImage() {
        val backgrounds = listOf("lake", "forest", "flowers", "desert")
        for (visit in listOf(0L, 1L, Long.MAX_VALUE, Long.MIN_VALUE)) {
            assertEquals(VerseArtRotation.background(backgrounds, "verse", visit),
                VerseArtRotation.background(backgrounds.reversed() + "lake", "verse", visit))
        }
        assertEquals("lake", VerseArtRotation.background(listOf("lake"), "verse", 999))
    }
}
