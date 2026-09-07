package com.aaronsedna.hopecards.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BillingEntitlementPolicyTest {
    @Test
    fun lifetimePurchaseGrantsAccessWhenLegacySubscriptionsAreUnsupported() {
        assertTrue(
            BillingEntitlementPolicy.resolve(
                currentAdFree = false,
                ownsLifetimePurchase = true,
                hasLegacySubscription = false,
                inAppVerified = true,
                subscriptionsVerified = false,
            ),
        )
    }

    @Test
    fun failedQueryDoesNotRevokeCachedAccess() {
        assertTrue(
            BillingEntitlementPolicy.resolve(
                currentAdFree = true,
                ownsLifetimePurchase = false,
                hasLegacySubscription = false,
                inAppVerified = true,
                subscriptionsVerified = false,
            ),
        )
    }

    @Test
    fun successfulQueriesRevokeExpiredOrCanceledAccess() {
        assertFalse(
            BillingEntitlementPolicy.resolve(
                currentAdFree = true,
                ownsLifetimePurchase = false,
                hasLegacySubscription = false,
                inAppVerified = true,
                subscriptionsVerified = true,
            ),
        )
    }
}
