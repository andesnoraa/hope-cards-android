# Daily Hope Scripture curation

The app contains 365 active cards per edition, with the same stable canonical IDs across all ten editions. The September 2026 revision replaces 83 passages and verifies the text of the complete collection against its edition source. New entries use full verses or adjacent full verses; no AI-written translation, paraphrase or ellipsis is inserted. Replacements are manually selected in `replacements.json`, with a specific reason for each.

User review:

- [Interactive before/after report](../../docs/daily-verses/review.html): all ten editions, references, full text, reasons, source links, search and edition filters.
- [English replacement list](../../docs/daily-verses/replacements.md): all 83 choices and their full BSB text.
- [All changes as CSV](../../docs/daily-verses/changes.csv).

## Source checks

`sources.json` identifies the exact edition downloads, retrieval date and SHA-256 hashes. BSB, BBE, KJV, WEB Updated, Luther 1912, Segond 1910, Riveduta 1927 and Reina-Valera 1909 come from [eBible](https://ebible.org/Scriptures/). USFX verse text is checked against the separately parsed VPL export for every active and archived passage. Malayalam 1910 and Ang Dating Biblia 1905 come from their named [GetBible distributions](https://getbible.net/).

`cross-check-results.json` records further checks:

- All 450 distinct Malayalam source verse units against the [Free Bible Foundation 1910 USFM transcription](https://github.com/tfbf/Bible-Malayalam-Sathyavedapusthakam-1910/tree/5f0f30ea9726c419d4b5ee53aaab93ff3480a98d). Two passages differ only in a layout space before Selah; biblical words are identical.
- All 83 Tagalog replacements (88 verse units including ranges) against separately downloaded chapter files from the same distributor. This is a second extraction check, not a claim of an independent translation or publisher.
- All 221 active/archived German Psalm cards against [CrossWire's Luther-to-KJV numbering map](https://github.com/crosswire/jsword/blob/master/src/main/resources/org/crosswire/jsword/versification/Luther.properties), with explicit checks for 2 Corinthians 13:13 and archived Deuteronomy 13:4. CrossWire credits Jens Grabner and DIB / STEP Bible for that map; the downloaded reference file retains its LGPL notice. The app contains only factual verse-number mappings, not CrossWire software.

`reference-map.json` distinguishes the canonical passage, the distributor's source key and the displayed edition reference. It also records a hash of every bundled verse's exact UTF-8 text. This catches accidental later edits with the offline verifier.

## Numbering and formatting

- Segond numbers many Psalm superscriptions as verses. The curated Psalm offsets are 0, 1 or 2, verified from source chapter lengths; no Psalm 13 verse-boundary ambiguity is present in this selection.
- eBible distributes Luther 1912 with KJV-numbered source keys. The app displays Luther's numbering, cross-checked against CrossWire. For Psalm 46, the German heading belongs to Luther verse 1; the displayed body is the complete verse 2.
- Malayalam 1910 reorders Philippians 1:3–6; its verse 4 contains the assurance corresponding to English verse 6. The card uses the actual Malayalam verse number, 1:4.
- The final blessing of 2 Corinthians is 13:13 in French, German, Spanish and Italian. Italian's literal distributor annotation `(G13-13)` is moved into the reference. Spanish's publisher colophon is excluded from the verse body.
- Only layout whitespace and paragraph symbols are normalized. Notes and separately marked section headings are excluded. Superscriptions embedded in the source's verse text are retained except for the explicitly separated German Psalm 46 heading above. Biblical spelling, accents, words and punctuation are preserved. Italics are represented as plain text, not added brackets.
- The originals of all 180 WEB artworks still match the verified Scripture word-for-word. Existing display capitalization and quotation styling in artwork are allowed by its presentation test; the underlying verse assets retain publisher capitalization and punctuation.

## Compatibility and resource use

Retired passages live in `assets/verses/archive/`. Stable IDs still resolve for favorites, journal entries, already-delivered notifications and existing Verse Art. They are excluded from random cards and the new daily selection. A previously selected verse remains available for its current day.

The archive is loaded only on an ID lookup that misses the active set. Active and archived data share one cache limited to two editions. All Scripture is bundled: this change adds no runtime network requests, background workers, timers or wake locks. Full Bible downloads and review reports are not packaged in the app.

## Reproduce

The pre-change baseline is git commit `c1726b5cb9260fb34f42e37a9c8820ffaac6a8f4`. Export `android/app/src/main/assets/verses/*.json` from that commit into a temporary baseline directory. Then:

```sh
python3 scripts/daily-verses/download.py /tmp/hope-verse-sources
python3 scripts/daily-verses/build.py /tmp/hope-verse-sources /tmp/hope-verses-before
python3 scripts/daily-verses/cross_check.py /tmp/hope-verse-sources
python3 scripts/daily-verses/verify.py
python3 scripts/daily-verses/report.py
```

Do not use the already-updated active assets as the baseline. Source hashes intentionally fail if a distributor changes a download; inspect the changed edition before adopting a new hash. Rebuilding is a developer operation, never an on-device task.

`verify.py` checks all 3,650 active entries, 830 archived entries, 830 replacements, source-text hashes, non-overlapping selections, consistent IDs, references, the reviewed length limit, and compatibility with all 180 verified artworks. Android instrumented tests additionally exercise archived-ID lookup/cache eviction, edition-specific references, daily selection and the longest card in every edition through the notification route.
