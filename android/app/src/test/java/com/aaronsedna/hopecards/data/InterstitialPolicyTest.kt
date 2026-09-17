package com.aaronsedna.hopecards.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {
    @Test
    fun interstitialIsEligibleEveryTenCompletedCards() {
        assertEquals(10, InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS)
    }

    @Test
    fun fewerThanRequiredCompletedCardsDoesNotShowAnAd() {
        assertFalse(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS - 1,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS,
            ),
        )
    }

    @Test
    fun enoughCardsBeforeMinimumIntervalDoesNotShowAnAd() {
        assertFalse(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS - 1,
            ),
        )
    }

    @Test
    fun enoughCardsAfterMinimumIntervalShowsAnAd() {
        assertTrue(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS,
            ),
        )
    }
}
