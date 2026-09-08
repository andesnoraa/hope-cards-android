package com.aaronsedna.hopecards.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleReferenceFormatterTest {
    @Test
    fun formatsVerifiedEditionSpecificTitles() {
        val cases = mapOf(
            Translation.LUT1912 to mapOf(
                "Genesis 1:1" to "1. Mose 1:1",
                "1 Chronicles 16:34" to "1. Chronik 16:34",
                "Acts 2:38" to "Apg 2:38",
                "2 Thessalonians 3:16" to "2 Thess 3:16",
            ),
            Translation.LSG1910 to mapOf(
                "Isaiah 41:10" to "Ésaïe 41:10",
                "James 1:5" to "Jacques 1:5",
                "1 Thessalonians 5:16-18" to "1 Th 5:16-18",
            ),
            Translation.RIV1927 to mapOf(
                "Joshua 1:9" to "Giosuè 1:9",
                "1 Corinthians 13:4" to "1 Corinti 13:4",
                "2 Peter 3:9" to "2 Pietro 3:9",
                "1 Thessalonians 5:11" to "1 Tess. 5:11",
            ),
            Translation.RV1909 to mapOf(
                "Matthew 11:28" to "San Mateo 11:28",
                "Luke 1:37" to "San Lucas 1:37",
                "Revelation 21:4" to "Apocalipsis 21:4",
                "2 Thessalonians 3:3" to "2 Tes. 3:3",
            ),
            Translation.ADB1905 to mapOf(
                "Numbers 6:24-26" to "Mga Bilang 6:24-26",
                "Micah 6:8" to "Mikas 6:8",
                "1 Corinthians 13:13" to "1 Cor. 13:13",
                "1 Thessalonians 5:18" to "1 Tes. 5:18",
                "Revelation 21:4" to "Pahayag 21:4",
            ),
            Translation.MAL1910 to mapOf(
                "Genesis 1:1" to "ഉല്പത്തി 1:1",
                "Acts 1:8" to "അപ്പൊ. പ്രവൃത്തികൾ 1:8",
                "1 Thessalonians 5:16-18" to "1. തെസ്സ. 5:16-18",
                "Revelation 21:4" to "വെളിപ്പാടു 21:4",
            ),
        )

        cases.forEach { (translation, references) ->
            references.forEach { (canonical, expected) ->
                assertEquals("Incorrect $translation title for $canonical", expected, BibleReferenceFormatter.format(canonical, translation))
            }
        }
    }

    @Test
    fun normalizesBothPsalmSourceFormsWithoutChangingTheAddress() {
        assertEquals("Psalmen 23:4", BibleReferenceFormatter.format("Psalm 23:4", Translation.LUT1912))
        assertEquals("Psalmen 46:1", BibleReferenceFormatter.format("Psalms 46:1", Translation.LUT1912))
        assertEquals("സങ്കീർത്തനങ്ങൾ 23:4", BibleReferenceFormatter.format("Psalm 23:4", Translation.MAL1910))
    }

    @Test
    fun leavesEnglishAndUnknownReferencesUnchanged() {
        Translation.entries.filter { it.language == "English" }.forEach { translation ->
            assertEquals("Romans 8:38-39", BibleReferenceFormatter.format("Romans 8:38-39", translation))
        }
        assertEquals("Unknown 1:2", BibleReferenceFormatter.format("Unknown 1:2", Translation.LSG1910))
        assertEquals("not a reference", BibleReferenceFormatter.format("not a reference", Translation.RV1909))
    }

    @Test
    fun localizedDisplayDoesNotChangeTheCanonicalStoredReference() {
        val verse = Verse(
            id = "compatibility-test",
            category = "hope",
            text = "Test verse",
            reference = "1 Thessalonians 5:18",
            translation = Translation.LUT1912.attribution,
            tags = emptyList(),
            edition = Translation.LUT1912,
        )

        assertEquals("1 Thessalonians 5:18", verse.reference)
        assertEquals("1 Thess 5:18", verse.displayReference)
    }

    @Test
    fun coversEveryBookCurrentlyBundledInTheApp() {
        val canonicalBooks = listOf(
            "Genesis", "Exodus", "Numbers", "Deuteronomy", "Joshua", "1 Samuel", "2 Samuel",
            "1 Chronicles", "Nehemiah", "Job", "Psalm", "Psalms", "Proverbs", "Isaiah",
            "Jeremiah", "Lamentations", "Daniel", "Micah", "Nahum", "Habakkuk", "Matthew",
            "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians",
            "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
            "2 Thessalonians", "1 Timothy", "2 Timothy", "Philemon", "Hebrews", "James",
            "1 Peter", "2 Peter", "1 John", "2 John", "Jude", "Revelation",
        )
        Translation.entries.forEach { translation ->
            canonicalBooks.forEach { book ->
                assertTrue(
                    "Missing $book for ${translation.id}",
                    BibleReferenceFormatter.hasLocalizedBookTitle("$book 1:1", translation),
                )
            }
        }
    }
}
