package com.aaronsedna.hopecards.ads

import com.aaronsedna.hopecards.model.Destination

internal object AdPlacementPolicy {
    private val bannerDestinations = setOf(
        Destination.FAVORITES,
        Destination.JOURNAL,
    )

    fun showsBanner(destination: Destination): Boolean = destination in bannerDestinations

    fun allowsInterstitial(destination: Destination): Boolean = destination != Destination.DAILY

    fun handlesQuizExit(from: Destination, to: Destination): Boolean =
        from == Destination.BIBLE_QUIZ && to != Destination.BIBLE_QUIZ && allowsInterstitial(to)
}
