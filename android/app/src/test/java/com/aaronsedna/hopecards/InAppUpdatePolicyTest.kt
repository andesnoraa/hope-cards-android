package com.aaronsedna.hopecards

import com.google.android.play.core.install.model.UpdateAvailability
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InAppUpdatePolicyTest {
    @Test
    fun startsWhenFlexibleUpdateIsAvailable() {
        assertTrue(
            shouldStartFlexibleUpdate(
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                flexibleUpdateAllowed = true,
                promptAttemptedThisSession = false,
                updateFlowStarted = false,
            ),
        )
    }

    @Test
    fun doesNotStartWhenUpdateIsUnavailableOrDisallowed() {
        assertFalse(
            shouldStartFlexibleUpdate(
                updateAvailability = UpdateAvailability.UPDATE_NOT_AVAILABLE,
                flexibleUpdateAllowed = true,
                promptAttemptedThisSession = false,
                updateFlowStarted = false,
            ),
        )
        assertFalse(
            shouldStartFlexibleUpdate(
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                flexibleUpdateAllowed = false,
                promptAttemptedThisSession = false,
                updateFlowStarted = false,
            ),
        )
    }

    @Test
    fun promptsAtMostOncePerSessionAndNeverStartsTwice() {
        assertFalse(
            shouldStartFlexibleUpdate(
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                flexibleUpdateAllowed = true,
                promptAttemptedThisSession = true,
                updateFlowStarted = false,
            ),
        )
        assertFalse(
            shouldStartFlexibleUpdate(
                updateAvailability = UpdateAvailability.UPDATE_AVAILABLE,
                flexibleUpdateAllowed = true,
                promptAttemptedThisSession = false,
                updateFlowStarted = true,
            ),
        )
    }
}
