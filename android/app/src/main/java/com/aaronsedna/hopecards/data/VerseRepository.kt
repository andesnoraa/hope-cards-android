package com.aaronsedna.hopecards.data

import android.content.Context
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import org.json.JSONArray
import kotlin.random.Random

class VerseRepository(private val context: Context) {
    private val cache = object : LinkedHashMap<Translation, List<Verse>>(2, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Translation, List<Verse>>?): Boolean =
            size > 2
    }

    @Synchronized
    fun verses(translation: Translation): List<Verse> =
        cache.getOrPut(translation) { load(translation) }

    fun byId(id: String, translation: Translation): Verse? =
        verses(translation).firstOrNull { it.id == id }

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
                verse.category.lowercase().contains(normalized) ||
                verse.tags.any { it.lowercase().contains(normalized) }
        }
    }

    private fun load(translation: Translation): List<Verse> {
        val json = context.assets.open("verses/${translation.assetName}")
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
                    ),
                )
            }
        }
    }
}
