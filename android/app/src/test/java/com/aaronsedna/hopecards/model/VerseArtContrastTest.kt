package com.aaronsedna.hopecards.model

import org.junit.Assert.*
import org.junit.Test

class VerseArtContrastTest {
    @Test fun letteringChangesPolarityForDarkAndBrightPhotos() {
        val darkPhoto = VerseArtContrast.choose(List(100) { .01 }, 0xFF253830.toInt())
        val brightPhoto = VerseArtContrast.choose(List(100) { .85 }, 0xFFFFFFFF.toInt())
        assertTrue(VerseArtContrast.luminance(darkPhoto.ink) > .8)
        assertTrue(VerseArtContrast.luminance(brightPhoto.ink) < .1)
        assertFalse(darkPhoto.outline)
        assertFalse(brightPhoto.outline)
    }

    @Test fun mixedLightAndDarkDetailGetsContrastingLetterEdges() {
        val mixed = VerseArtContrast.choose(List(50) { .01 } + List(50) { .95 }, 0xFF253830.toInt())
        assertTrue(mixed.outline)
        assertNotEquals(VerseArtContrast.luminance(mixed.ink) > .5, VerseArtContrast.luminance(mixed.edge) > .5)
    }
}
