package com.aaronsedna.hopecards.ads

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentBreakTest {
    @Test fun unavailableAdContinuesImmediatelyAndCannotAppearLater(): Unit = runBlocking {
        val eligibility = CompletableDeferred<Boolean>()
        val events = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            completeContentBreak(false, { eligibility.await() }, { true },
                { events += "ad" }, { events += "gallery" })
        }
        assertEquals(listOf("gallery"), events)
        // Even if bookkeeping and an ad load finish later, the opportunity has passed.
        eligibility.complete(true)
        job.join()
        assertEquals(listOf("gallery"), events)
    }

    @Test fun eligiblePreloadedAdAppearsBeforeGallery(): Unit = runBlocking {
        val events = mutableListOf<String>()
        completeContentBreak(true, { events += "count"; true }, { true },
            { events += "ad" }, { events += "gallery" })
        assertEquals(listOf("count", "ad", "gallery"), events)
    }

    @Test fun navigatingAwayDuringBookkeepingDiscardsAd(): Unit = runBlocking {
        val eligibility = CompletableDeferred<Boolean>()
        var current = true
        val events = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            completeContentBreak(true, { eligibility.await() }, { current },
                { events += "ad" }, { events += "continue" })
        }
        current = false
        eligibility.complete(true)
        job.join()
        assertEquals(listOf("continue"), events)
    }

    @Test fun frequencyLimitSkipsAd(): Unit = runBlocking {
        val events = mutableListOf<String>()
        completeContentBreak(true, { false }, { true },
            { events += "ad" }, { events += "gallery" })
        assertEquals(listOf("gallery"), events)
    }

    @Test fun bookkeepingFailureCannotTrapUserInArtwork(): Unit = runBlocking {
        val events = mutableListOf<String>()
        runCatching {
            completeContentBreak(true, { error("Storage unavailable") }, { true },
                { events += "ad" }, { events += "gallery" })
        }
        assertEquals(listOf("gallery"), events)
    }

    @Test fun failedPresentationStillContinuesExactlyOnce(): Unit = runBlocking {
        var transitions = 0
        runCatching {
            completeContentBreak(true, { true }, { true }, { error("Activity paused") }, { transitions++ })
        }
        assertEquals(1, transitions)
    }

    @Test fun destroyingActivityCancelsPendingWorkWithoutShowingAd(): Unit = runBlocking {
        val pendingStorage = CompletableDeferred<Boolean>()
        val events = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            completeContentBreak(true, { pendingStorage.await() }, { true },
                { events += "ad" }, { events += "continue" })
        }
        job.cancelAndJoin()
        pendingStorage.complete(true)
        assertEquals(listOf("continue"), events)
    }
}
