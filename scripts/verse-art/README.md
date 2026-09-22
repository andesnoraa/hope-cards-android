# Legacy Verse Art authoring

The current app uses reusable backgrounds and on-demand rendering in the selected Bible edition. See [the current renderer documentation](preview/README.md). This page describes the earlier English-only authoring workflow. Its baked images are now test-only assets, excluded from app packages.

The legacy test assets contain **180 finished WebP images and 180 thumbnails**, organized into six disjoint collections of 30: Hope, Peace, Strength, Joy, Comfort, and Gratitude. No network request or image-generation service is needed in the app. Images can be saved and shared offline.

All 180 designs combine generated photographic backgrounds with six typography treatments and distinct verses from the existing World English Bible dataset. Every text block has a horizontal baseline; no layout rotates the verse. The typography includes italic serif verses, brush script, bold marker lettering, and spaced capitals. All backgrounds are rich and dark for cream lettering. Backgrounds are reused across distinct verse cards.

All 180 verses were compared with the publisher’s World English Bible Updated HTML edition on 22 September 2026. Psalm 46:10 was corrected from ‘in the earth’ to ‘on the earth’ in both the reading dataset and artwork. `verified-web-verses.json` stores the publisher’s text, source URL, and archive SHA-256; catalog generation rejects wording or reference differences (ignoring capitalization, punctuation, and whitespace).

## Sources

- `curation.txt`: reviewed verse selections and exact emphasis phrases.
- `build_catalog.py`: builds `ExpandedVerseArt.kt` and the authoring manifest from the bundled WEB text; rejects missing emphasis phrases and repeated verse text.
- `assets/verse-art/*generation-prompts.json`: initial prompts and output locations. `assets/verse-art/rich-background-prompts.json`, `assets/verse-art/reference-style-prompts.json`, and `assets/verse-art/encouraging-background-prompts.json` record the built-in image generation prompts and source paths for the replacements, reference-inspired images, and additional encouraging scenes.
- `android/app/src/androidTest/assets/`: generation manifest, background WebPs, and licensed authoring fonts.
- `GenerateVerseArtCollection.kt`: opt-in Android Canvas authoring utility; measures text before drawing and rejects overflowing cards. It is not part of production code.
- `android/app/src/androidTest/assets/verse-art/`: legacy images retained only for authoring comparisons.
- `assets/verse-art/rendered/review-*.png`: visual contact sheets, 30 cards per category.

## Regenerate after changing content or layout

1. Run `python3 scripts/verse-art/build_catalog.py` from the repository root.
2. Build the debug app and test APK locally with Gradle and install both on an emulator.
3. Run the authoring utility (replace the emulator serial if necessary):

```sh
adb -s emulator-5554 shell am instrument -w \
  -e generateVerseArt true \
  -e class com.aaronsedna.hopecards.ui.GenerateVerseArtCollection \
  com.aaronsedna.hopecards.debug.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5554 pull \
  /sdcard/Android/data/com.aaronsedna.hopecards.debug/files/verse-art-generated/. \
  /tmp/hope-verse-art/
```

4. Review the six contact sheets and representative full-size cards. Copy the generated WebPs into the test-only `android/app/src/androidTest/assets/verse-art/` directory and the contact sheets into `assets/verse-art/rendered/`.
5. Rebuild, run unit tests and lint, then run `VerseArtInstrumentedTest` against the rebuilt APK. It verifies all 180 image pairs, text against the bundled Bible, category counts, navigation and scroll restoration, favorite persistence after activity recreation, shared bytes, replacement of stale cached exports, and MediaStore image saving.

Verse Art uses its own gallery icon in the drawer; Remove Ads retains its sparkle icon. The current gallery shares native-rendered JPEGs, with a versioned bounded cache.

The native screen follows the existing app theme and appears under **Navigation drawer → Verse Art → Category**. Favorites reuse the existing verse favorite IDs and backup behavior. This legacy authoring tool produces English WEB artwork. The current app gallery follows the selected Bible translation.
