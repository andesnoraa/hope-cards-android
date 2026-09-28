package com.aaronsedna.hopecards.ads

import com.aaronsedna.hopecards.model.Destination
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
                destination = Destination.HOME,
            ),
        )
    }

    @Test fun paidUsersNeverSeeInterstitials() = assertBlocked(adsEnabled = false)

    @Test fun backgroundedAppNeverShowsInterstitials() = assertBlocked(foreground = false)

    @Test fun missingConsentPreventsInterstitials() = assertBlocked(consentAllowsAds = false)

    @Test fun pausedActivityPreventsLateInterstitials() = assertBlocked(activityResumed = false)

    @Test fun duplicateInterstitialsCannotStack() = assertBlocked(alreadyShowing = true)

    @Test fun dailyHopeBlocksOtherwiseEligibleInterstitials() = assertBlocked(destination = Destination.DAILY)

    private fun assertBlocked(
        adsEnabled: Boolean = true,
        foreground: Boolean = true,
        consentAllowsAds: Boolean = true,
        activityResumed: Boolean = true,
        alreadyShowing: Boolean = false,
        destination: Destination = Destination.HOME,
    ) {
        assertFalse(
            InterstitialPresentationPolicy.canPresent(
                adsEnabled = adsEnabled,
                foreground = foreground,
                consentAllowsAds = consentAllowsAds,
                activityResumed = activityResumed,
                alreadyShowing = alreadyShowing,
                destination = destination,
            ),
        )
    }
}
