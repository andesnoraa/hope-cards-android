package com.aaronsedna.hopecards.model

data class Verse(
    val id: String,
    val category: String,
    val text: String,
    val reference: String,
    val translation: String,
    val tags: List<String>,
    val edition: Translation,
) {
    val displayReference: String
        get() = BibleReferenceFormatter.format(reference, edition)
}

enum class Translation(
    val id: String,
    val assetName: String,
    val label: String,
    val displayName: String,
    val language: String,
    val attribution: String,
) {
    BSB("bsb", "en-bsb.json", "BSB", "Berean Standard Bible", "English", "Berean Bible Translation Committee"),
    BBE("bbe", "en-bbe.json", "BBE", "Bible in Basic English", "English", "Samuel Henry Hooke"),
    KJV("kjv", "en-kjv.json", "KJV", "King James Version", "English", "King James Bible translation committees"),
    WEB("web", "en-web.json", "WEB", "World English Bible", "English", "Michael Paul Johnson and contributors"),
    MAL1910("mal1910", "ml-mal1910.json", "MAL", "Sathyavedapusthakam (1910)", "Malayalam", "British and Foreign Bible Society"),
    RV1909("rv1909", "es-rv1909.json", "RV1909", "Reina-Valera (1909)", "Spanish", "Casiodoro de Reina & Cipriano de Valera"),
    LSG1910("lsg1910", "fr-lsg1910.json", "LSG", "Louis Segond (1910)", "French", "Louis Segond"),
    LUT1912("lut1912", "de-lut1912.json", "LUT", "Lutherbibel (1912)", "German", "Martin Luther"),
    RIV1927("riv1927", "it-riv1927.json", "RIV", "Riveduta (1927)", "Italian", "Bible Society in Italy"),
    ADB1905("adb1905", "tl-adb1905.json", "ADB1905", "Ang Dating Biblia (1905)", "Tagalog", "Philippine Bible Society");

    companion object {
        fun fromId(id: String?): Translation = entries.firstOrNull { it.id == id } ?: BSB
    }
}

enum class ThemeName(val id: String, val label: String, val description: String) {
    CLASSIC("classic", "Classic", "Warm cream, navy, and gold."),
    ROSE_DAWN("roseDawn", "Rose Dawn", "Soft rose, plum, and champagne."),
    OLIVE_GROVE("oliveGrove", "Olive Grove", "Deep olive, linen, and brass."),
    SERENITY("serenity", "Evergreen Edition", "Fresh white, green leather, and gold."),
    STILL_WATER("stillWater", "Still Water", "Deep teal with clear blue."),
    MIDNIGHT("midnight", "Midnight", "Black cards with antique gold."),
    COASTAL_LINEN("coastalLinen", "Coastal Linen", "Natural linen, deep teal, and terracotta."),
    QUIET_LAVENDER("quietLavender", "Quiet Lavender", "Soft lavender, plum, and muted lilac."),
    VINTAGE_HERITAGE("vintageHeritage", "Vintage Heritage", "Ivory, black leather, antique gold.");

    companion object {
        fun fromId(id: String?): ThemeName = when (id) {
            "sunlitParchment" -> VINTAGE_HERITAGE
            else -> entries.firstOrNull { it.id == id } ?: CLASSIC
        }
    }
}

data class AppSettings(
    val showDrawButton: Boolean = true,
    val enableHaptics: Boolean = true,
    val dailyHopeReminderEnabled: Boolean = false,
    val dailyHopeMusicEnabled: Boolean = true,
    val dailyHopeReminderHour: Int = 8,
    val dailyHopeReminderMinute: Int = 0,
    val themeName: ThemeName = ThemeName.CLASSIC,
    val preferredTranslation: Translation = Translation.BSB,
)

data class DailyHopeRecord(
    val date: String,
    val verseId: String,
    val translation: String,
)

data class JournalEntry(
    val id: String,
    val date: String,
    val verseId: String,
    val reference: String,
    val prompt: String,
    val note: String,
    val updatedAt: String,
)

data class BackupInfo(
    val createdAt: String,
    val fileName: String,
    val version: Int,
    val favoriteCount: Int,
    val journalEntryCount: Int,
)

enum class Destination(val title: String) {
    HOME("Hope Cards"),
    DAILY("Daily Hope"),
    FAVORITES("Favorites"),
    JOURNAL("Journal"),
    REMOVE_ADS("Remove Ads"),
    SETTINGS("Settings"),
    PRIVACY("Privacy Policy"),
    ABOUT("About"),
}

data class BillingState(
    val connected: Boolean = false,
    val loading: Boolean = true,
    val isAdFree: Boolean = false,
    val price: String? = null,
    val canPurchase: Boolean = false,
    val pending: Boolean = false,
    val message: String? = null,
)
