package com.aaronsedna.hopecards.ads

import com.aaronsedna.hopecards.model.Destination

internal object AdPlacementPolicy {
    private val bannerDestinations = setOf(
        Destination.FAVORITES,
        Destination.JOURNAL,
    )

    fun showsBanner(destination: Destination): Boolean = destination in bannerDestinations
}
