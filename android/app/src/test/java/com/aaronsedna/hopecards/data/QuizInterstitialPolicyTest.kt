package com.aaronsedna.hopecards.data

import org.junit.Assert.*
import org.junit.Test

class QuizInterstitialPolicyTest {
    @Test fun aCompletedTenQuestionQuizCanShowAnAd() {
        assertFalse(QuizInterstitialPolicy.shouldShow(0, Long.MAX_VALUE))
        assertTrue(QuizInterstitialPolicy.shouldShow(1, InterstitialPolicy.MIN_INTERVAL_MS))
    }

    @Test fun recentAdsElsewhereInTheAppSuppressTheQuizAd() {
        assertFalse(QuizInterstitialPolicy.shouldShow(1, InterstitialPolicy.MIN_INTERVAL_MS - 1))
        assertFalse(QuizInterstitialPolicy.shouldShow(10, 0))
        assertFalse(QuizInterstitialPolicy.shouldShow(10, -1))
    }
}
