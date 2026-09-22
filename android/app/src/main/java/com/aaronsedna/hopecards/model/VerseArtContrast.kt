package com.aaronsedna.hopecards.model

import kotlin.math.pow

/** Choose ink against the actual text region; edge protection changes lettering only. */
internal object VerseArtContrast {
    data class Choice(val ink: Int, val edge: Int, val outline: Boolean)

    fun luminance(color: Int): Double {
        fun linear(channel: Int): Double {
            val s = channel / 255.0
            return if (s <= .04045) s / 12.92 else ((s + .055) / 1.055).pow(2.4)
        }
        return .2126 * linear((color shr 16) and 255) + .7152 * linear((color shr 8) and 255) + .0722 * linear(color and 255)
    }

    fun choose(samples: List<Double>, preferredInk: Int): Choice {
        val light = 0xFFFFF8E9.toInt()
        val dark = if (luminance(preferredInk) < .12) preferredInk else 0xFF172D2A.toInt()
        fun score(ink: Int): Double {
            val foreground = luminance(ink)
            val contrast = samples.map { (maxOf(it, foreground) + .05) / (minOf(it, foreground) + .05) }.sorted()
            // Protect the difficult parts of a mixed photograph, not just its average brightness.
            return contrast.getOrElse(contrast.size / 20) { 1.0 }
        }
        val lightScore = score(light)
        val darkScore = score(dark)
        val useLight = lightScore >= darkScore
        return Choice(if (useLight) light else dark, if (useLight) 0xFF101B19.toInt() else 0xFFFFFCF4.toInt(),
            maxOf(lightScore, darkScore) < 4.5)
    }
}
