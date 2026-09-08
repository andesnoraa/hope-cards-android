package com.aaronsedna.hopecards.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.model.BibleReferenceFormatter
import com.aaronsedna.hopecards.model.Translation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerseRepositoryInstrumentedTest {
    @Test
    fun everyTranslationContainsTheCompleteUsableVerseSet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = VerseRepository(context)
        val expectedIds = repository.verses(Translation.BSB).map { it.id }.toSet()

        assertTrue(expectedIds.isNotEmpty())
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
}
