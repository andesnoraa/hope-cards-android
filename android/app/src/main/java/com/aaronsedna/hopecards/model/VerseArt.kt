package com.aaronsedna.hopecards.model

data class VerseArtwork(
    val id: String,
    val verseId: String,
    val title: String,
    val reference: String,
    val text: String,
    val categoryId: String = id.substringBefore("-"),
) {
}

data class VerseArtCategory(
    val id: String,
    val title: String,
    val description: String,
    val artworkIds: List<String>,
    val coverArtworkId: String,
)

/** Stable artwork IDs and English editorial titles; rendered Scripture uses the selected edition. */
object VerseArtCatalog {
    const val ALL = "all"
    const val SAVED = "saved"
    val artworks = listOf(
        VerseArtwork("peace", "psalm-46-10", "Be still", "Psalm 46:10",
            "Be still, and know that I am God. I will be exalted among the nations. I will be exalted on the earth."),
        VerseArtwork("strength", "nahum-1-7", "The Lord is good", "Nahum 1:7",
            "The LORD is good, a stronghold in the day of trouble; and he knows those who take refuge in him."),
        VerseArtwork("joy", "psalm-118-24", "We will rejoice", "Psalm 118:24",
            "This is the day that the LORD has made. We will rejoice and be glad in it!"),
        VerseArtwork("hope-faithful", "hebrews-10-23", "He is faithful", "Hebrews 10:23",
            "let’s hold fast the confession of our hope without wavering; for he who promised is faithful."),
        VerseArtwork("hope-abound", "romans-15-13", "Abound in hope", "Romans 15:13",
            "Now may the God of hope fill you with all joy and peace in believing, that you may abound in hope in the power of the Holy Spirit."),
        VerseArtwork("peace-safe-at-night", "psalm-4-8", "In peace", "Psalm 4:8",
            "In peace I will both lay myself down and sleep, for you alone, LORD, make me live in safety."),
        VerseArtwork("strength-through-christ", "philippians-4-13", "Through Christ", "Philippians 4:13",
            "I can do all things through Christ who strengthens me."),
        VerseArtwork("joy-path-of-life", "psalm-16-11", "Fullness of joy", "Psalm 16:11",
            "You will show me the path of life. In your presence is fullness of joy. In your right hand there are pleasures forever more."),
        VerseArtwork("comfort-he-cares", "1-peter-5-7", "He cares for you", "1 Peter 5:7",
            "Casting all your worries on him, because he cares for you."),
        VerseArtwork("gratitude-loving-kindness", "psalm-100-5", "His loving kindness", "Psalm 100:5",
            "For the LORD is good. His loving kindness endures forever, his faithfulness to all generations."),
    ) + expandedVerseArt()
    private fun category(id: String, title: String, description: String, coverArtworkId: String) =
        VerseArtCategory(id, title, description, artworks.filter { it.categoryId == id }.map { it.id }, coverArtworkId)

    val categories = listOf(
        category("hope", "Hope", "Promises for a brighter tomorrow", "hope-faithful"),
        category("peace", "Peace", "Rest in God’s presence", "peace-john-14-1"),
        category("strength", "Strength", "Courage for the days ahead", "strength-joshua-1-9"),
        category("joy", "Joy", "Reasons to rejoice", "joy-psalm-37-4"),
        category("comfort", "Comfort", "Reassurance in difficult moments", "comfort-psalm-34-18"),
        category("gratitude", "Gratitude", "Give thanks for everyday blessings", "gratitude-psalms-106-1"),
    )

    fun artwork(id: String?): VerseArtwork? = artworks.firstOrNull { it.id == id }
    fun title(categoryId: String?): String = when (categoryId) {
        ALL -> "All artwork"
        SAVED -> "Saved artwork"
        else -> categories.firstOrNull { it.id == categoryId }?.title ?: "Verse Gallery"
    }

    fun inCategory(id: String, favoriteVerseIds: Set<String>): List<VerseArtwork> = when (id) {
        ALL -> artworks
        SAVED -> artworks.filter { it.verseId in favoriteVerseIds }
        else -> categories.firstOrNull { it.id == id }?.artworkIds.orEmpty().mapNotNull(::artwork)
    }
}
