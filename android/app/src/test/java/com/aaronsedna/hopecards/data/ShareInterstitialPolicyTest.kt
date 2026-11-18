package com.aaronsedna.hopecards.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareInterstitialPolicyTest {
    @Test fun shareInterstitialWaitsForTheGlobalCooldown() {
        assertFalse(ShareInterstitialPolicy.shouldShow(InterstitialPolicy.MIN_INTERVAL_MS - 1))
        assertTrue(ShareInterstitialPolicy.shouldShow(InterstitialPolicy.MIN_INTERVAL_MS))
    }
}
