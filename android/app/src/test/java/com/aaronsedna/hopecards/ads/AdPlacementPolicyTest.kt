package com.aaronsedna.hopecards.ads

import com.aaronsedna.hopecards.model.Destination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPlacementPolicyTest {
    @Test
    fun bannersOnlyAppearOnFavoritesAndJournal() {
        Destination.entries.forEach { destination ->
            val expected = destination == Destination.FAVORITES || destination == Destination.JOURNAL
            if (expected) {
                assertTrue(destination.name, AdPlacementPolicy.showsBanner(destination))
            } else {
                assertFalse(destination.name, AdPlacementPolicy.showsBanner(destination))
            }
        }
    }
}
