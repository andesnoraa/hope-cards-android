package com.aaronsedna.hopecards.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.BibleReferenceFormatter
import com.aaronsedna.hopecards.model.Translation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.json.JSONArray
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerseRepositoryInstrumentedTest {
    @Test
    fun everyTranslationContainsTheCompleteUsableVerseSet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = VerseRepository(context)
        val expectedIds = repository.verses(Translation.BSB).map { it.id }.toSet()

        assertEquals(365, expectedIds.size)
        Translation.entries.forEach { translation ->
            val verses = repository.verses(translation)
            assertEquals("Verse count differs for ${translation.id}", expectedIds.size, verses.size)
            assertEquals("Verse IDs differ for ${translation.id}", expectedIds, verses.map { it.id }.toSet())
            assertTrue("Wrong edition metadata in ${translation.id}", verses.all { it.edition == translation })
            assertTrue("Missing localized book title in ${translation.id}", verses.all {
                BibleReferenceFormatter.hasLocalizedBookTitle(it.reference, translation)
            })
            assertTrue("Blank verse content in ${translation.id}", verses.all {
                it.id.isNotBlank() &&
                    it.category.isNotBlank() &&
                    it.text.isNotBlank() &&
                    it.reference.isNotBlank() &&
                    it.translation.isNotBlank()
            })
        }
    }

    @Test
    fun retiredPassagesRemainResolvableButAreExcludedFromDailySelection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = VerseRepository(context)
        Translation.entries.forEach { translation ->
            val activeIds = repository.verses(translation).map { it.id }.toSet()
            val archive = JSONArray(context.assets.open("verses/archive/${translation.assetName}")
                .bufferedReader().use { it.readText() })
            assertEquals(83, archive.length())
            repeat(archive.length()) { index ->
                val saved = archive.getJSONObject(index)
                val id = saved.getString("id")
                assertTrue("Retired verse still rotates: $id", id !in activeIds)
                val verse = repository.byId(id, translation)
                assertNotNull("Old saved ID no longer resolves: $id", verse)
                assertEquals(saved.getString("verse"), verse!!.text)
            }
            assertEquals(365, repository.search("", translation).size)
            repeat(20) { assertTrue(repository.random(translation).id in activeIds) }
            assertNull(repository.byId("nonexistent-verse", translation))
        }
        // Reload after the two-edition cache has evicted earlier catalogs.
        assertNotNull(repository.byId("joshua-24-15", Translation.BSB))
    }

    @Test
    fun referencesFollowTheEditionRatherThanAssumingEnglishNumbering() {
        val repository = VerseRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        val french = checkNotNull(repository.byId("psalm-46-1", Translation.LSG1910))
        assertEquals("Psalm 46:2", french.reference)
        assertTrue(french.text.startsWith("Dieu est pour nous un refuge"))
        assertEquals("Psalm 34:19", repository.byId("psalm-34-18", Translation.LSG1910)!!.reference)
        assertEquals("Psalm 34:19", repository.byId("psalm-34-18", Translation.LUT1912)!!.reference)
        assertEquals("Psalm 46:2", repository.byId("psalm-46-1", Translation.LUT1912)!!.reference)
        assertEquals("Philippians 1:4", repository.byId("philippians-1-6", Translation.MAL1910)!!.reference)
        assertEquals("2 Corinthians 13:13", repository.byId("2-corinthians-13-14", Translation.RV1909)!!.reference)
        assertEquals("2 Corinthians 13:13", repository.byId("2-corinthians-13-14", Translation.RIV1927)!!.reference)
        assertEquals("2 Corinthians 13:14", repository.byId("2-corinthians-13-14", Translation.BSB)!!.reference)
    }
}
