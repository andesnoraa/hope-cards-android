package com.aaronsedna.hopecards.billing

import org.junit.Assert.*
import org.junit.Test

class FreshCheckoutTest {
    @Test fun duplicateTapsQueryOnceAndLaterCheckoutFetchesNewOffer() {
        val busy = mutableListOf<Boolean>()
        val callbacks = mutableListOf<(String?) -> Unit>()
        val launched = mutableListOf<String>()
        val checkout = FreshCheckout<String>(busy::add)
        fun tap() = checkout.start({ callbacks.add(it) }, { true }, launched::add)
        tap(); tap()
        assertEquals(1, callbacks.size)
        callbacks[0]("offer-one")
        tap()
        assertEquals(2, callbacks.size)
        callbacks[0]("late-duplicate")
        callbacks[1]("offer-two")
        assertEquals(listOf("offer-one", "offer-two"), launched)
        assertEquals(listOf(true, false, true, false), busy)
    }

    @Test fun leavingScreenOrClosingManagerNeverLaunchesDelayedCheckout() {
        var callback: ((String?) -> Unit)? = null
        var visible = true
        var launches = 0
        val checkout = FreshCheckout<String> { }
        checkout.start({ callback = it }, { visible }, { launches++ })
        visible = false
        callback!!("offer")
        visible = true
        checkout.start({ callback = it }, { visible }, { launches++ })
        checkout.close()
        callback!!("offer")
        assertEquals(0, launches)
    }

    @Test fun unavailableProductEndsLoadingAndAllowsRetry() {
        val busy = mutableListOf<Boolean>()
        val checkout = FreshCheckout<String>(busy::add)
        var launches = 0
        checkout.start({ it(null) }, { true }, { launches++ })
        checkout.start({ it("available") }, { true }, { launches++ })
        assertEquals(1, launches)
        assertEquals(listOf(true, false, true, false), busy)
    }
}
