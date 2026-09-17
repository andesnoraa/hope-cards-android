package com.aaronsedna.hopecards.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPresentationPolicyTest {
    @Test
    fun presentsOnlyAtAResumedConsentedContentBreak() {
        assertTrue(
            InterstitialPresentationPolicy.canPresent(
                adsEnabled = true,
                foreground = true,
                consentAllowsAds = true,
                activityResumed = true,
                alreadyShowing = false,
            ),
        )
    }

    @Test fun paidUsersNeverSeeInterstitials() = assertBlocked(adsEnabled = false)

    @Test fun backgroundedAppNeverShowsInterstitials() = assertBlocked(foreground = false)

    @Test fun missingConsentPreventsInterstitials() = assertBlocked(consentAllowsAds = false)

    @Test fun pausedActivityPreventsLateInterstitials() = assertBlocked(activityResumed = false)

    @Test fun duplicateInterstitialsCannotStack() = assertBlocked(alreadyShowing = true)

    private fun assertBlocked(
        adsEnabled: Boolean = true,
        foreground: Boolean = true,
        consentAllowsAds: Boolean = true,
        activityResumed: Boolean = true,
        alreadyShowing: Boolean = false,
    ) {
        assertFalse(
            InterstitialPresentationPolicy.canPresent(
                adsEnabled = adsEnabled,
                foreground = foreground,
                consentAllowsAds = consentAllowsAds,
                activityResumed = activityResumed,
                alreadyShowing = alreadyShowing,
            ),
        )
    }
}
