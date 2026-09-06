package com.aaronsedna.hopecards.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {
    @Test
    fun fewerThanTenCompletedCardsDoesNotShowAnAd() {
        assertFalse(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS - 1,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS,
            ),
        )
    }

    @Test
    fun tenCardsBeforeMinimumIntervalDoesNotShowAnAd() {
        assertFalse(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS - 1,
            ),
        )
    }

    @Test
    fun tenCardsAfterMinimumIntervalShowsAnAd() {
        assertTrue(
            InterstitialPolicy.shouldShow(
                completedCards = InterstitialPolicy.COMPLETED_CARDS_BETWEEN_ADS,
                elapsedMs = InterstitialPolicy.MIN_INTERVAL_MS,
            ),
        )
    }
}
