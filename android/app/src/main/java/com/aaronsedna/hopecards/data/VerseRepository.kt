package com.aaronsedna.hopecards.data

import android.content.Context
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import org.json.JSONArray
import kotlin.random.Random

class VerseRepository(private val context: Context) {
    private class Catalog(val active: List<Verse>, var archive: List<Verse>? = null)

    // Active and legacy verses share one bounded cache. The archive is loaded
    // only when resolving an old favorite, journal entry, artwork or notification.
    private val cache = object : LinkedHashMap<Translation, Catalog>(2, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Translation, Catalog>?): Boolean =
            size > 2
    }

    @Synchronized
    fun verses(translation: Translation): List<Verse> =
        catalog(translation).active

    @Synchronized
    fun byId(id: String, translation: Translation): Verse? {
        val catalog = catalog(translation)
        catalog.active.firstOrNull { it.id == id }?.let { return it }
        val archive = catalog.archive ?: load(translation, "verses/archive").also { catalog.archive = it }
        return archive.firstOrNull { it.id == id }
    }

    private fun catalog(translation: Translation): Catalog =
        cache.getOrPut(translation) { Catalog(load(translation)) }

    fun random(translation: Translation, excludingId: String? = null): Verse {
        val available = verses(translation)
        require(available.isNotEmpty()) { "No verses are available." }
        if (available.size == 1) return available.first()

        var verse: Verse
        do {
            verse = available[Random.nextInt(available.size)]
        } while (verse.id == excludingId)
        return verse
    }

    fun search(query: String, translation: Translation): List<Verse> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return verses(translation)
        return verses(translation).filter { verse ->
            verse.text.lowercase().contains(normalized) ||
                verse.reference.lowercase().contains(normalized) ||
                verse.displayReference.lowercase().contains(normalized) ||
                verse.category.lowercase().contains(normalized) ||
                verse.tags.any { it.lowercase().contains(normalized) }
        }
    }

    private fun load(translation: Translation, directory: String = "verses"): List<Verse> {
        val json = context.assets.open("$directory/${translation.assetName}")
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(json)
        return buildList(array.length()) {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                val tagsJson = item.optJSONArray("tags") ?: JSONArray()
                add(
                    Verse(
                        id = item.getString("id"),
                        category = item.getString("category"),
                        text = item.getString("verse"),
                        reference = item.getString("reference"),
                        translation = item.getString("translation"),
                        tags = buildList(tagsJson.length()) {
                            repeat(tagsJson.length()) { add(tagsJson.getString(it)) }
                        },
                        edition = translation,
                    ),
                )
            }
        }
    }
}
