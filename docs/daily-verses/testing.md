# Verification — 22 September 2026

- Exactly 365 active cards in each of 10 editions, with aligned canonical IDs and no overlapping passage ranges.
- 83 replaced cards per edition (830 entries), with specific reasons and full before/after text in the review report.
- All 4,480 bundled active/archive entries match recorded source-text hashes. Eight editions were also checked against their VPL exports. All 450 Malayalam source verse units match the original 1910 transcription apart from two layout spaces; all 88 Tagalog replacement verse units match separate chapter exports.
- German displayed Psalm numbers checked for all 221 active/archive Psalm cards against CrossWire's Luther numbering map.
- All 180 existing Verse Art designs retain their original biblical words and references. Artwork's display capitalization/quotation styling is distinct from the exact publisher typography retained in the source verse assets.
- Offline verifier passed; debug APK and instrumentation APK built; Android lint passed; 39 unit tests passed.
- Updated debug build installed on Android emulator `emulator-5554` (API 37). The physical phone was unavailable.
- 7 instrumented tests passed. They cover complete edition data, all retired-ID lookups and cache eviction, edition-specific numbering, shared daily selection, the longest card in all 10 editions through the Daily Hope notification route, and all 180 artwork assets/word comparisons.
- Screenshots captured for every edition; the longest English, Malayalam and Tagalog cards were also visually inspected and fit with their save/share controls visible.
- The self-contained report was opened in the browser; all 10 edition filters returned 83 replacements, and searching for Philippians in Malayalam showed the corrected 1:4 reference.

Logs and screenshots are in `output/daily-verse-review/`. This is a local debug build, not a Google Play release.
