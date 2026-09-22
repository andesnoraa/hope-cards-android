package com.aaronsedna.hopecards.ads

/** Never hold navigation for an ad load, or show a newly loaded ad after navigation. */
internal suspend fun completeContentBreak(
    adReadyAtStart: Boolean,
    recordCompletion: suspend () -> Boolean,
    canStillPresent: () -> Boolean,
    showAd: () -> Unit,
    continueNavigation: () -> Unit,
) {
    if (!adReadyAtStart) continueNavigation()
    try {
        val eligible = recordCompletion()
        if (adReadyAtStart && eligible && canStillPresent()) showAd()
    } finally {
        // An already loaded ad is presented before the gallery transition, never afterwards.
        if (adReadyAtStart) continueNavigation()
    }
}
