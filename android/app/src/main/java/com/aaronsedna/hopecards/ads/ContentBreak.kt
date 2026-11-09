package com.aaronsedna.hopecards.ads

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/** Wait for a presented ad's dismissal/failure, never for an ad load. */
internal suspend fun awaitFullScreenContent(show: (onFinished: () -> Unit) -> Unit) {
    suspendCancellableCoroutine<Unit> { continuation ->
        val finished = AtomicBoolean(false)
        show {
            if (finished.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(Unit)
            }
        }
    }
}

/** No late ads after navigation. A presented ad must finish before starting new content. */
internal suspend fun completeContentBreak(
    adReadyAtStart: Boolean,
    recordCompletion: suspend () -> Boolean,
    canStillPresent: () -> Boolean,
    showAd: suspend () -> Unit,
    continueNavigation: () -> Unit,
) {
    if (!adReadyAtStart) continueNavigation()
    try {
        val eligible = recordCompletion()
        if (adReadyAtStart && eligible && canStillPresent()) showAd()
    } finally {
        // Do not restart a quiz (and its timer) behind the ad or after activity destruction.
        if (adReadyAtStart && currentCoroutineContext().isActive) continueNavigation()
    }
}
