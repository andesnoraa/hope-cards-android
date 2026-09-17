# Hope Cards Google Play assets

This directory contains the production Play Store metadata and artwork for Hope Cards.

## Recommended listing order

1. Draw a card — communicates the core interaction immediately.
2. Read Scripture — shows the complete verse card and typography.
3. Daily Hope — establishes the daily-use reason to return.
4. Favorites — shows that meaningful verses can be kept.
5. Journal — shows notes linked directly to their verses.
6. Themes — shows visual personalization without suggesting paid feature gating.

The phone set is 1080 × 1920. The tablet set is 1600 × 2560. Each image uses an actual app capture inside a consistent navy, gold, and warm-ivory Play Store layout. The first two tablet images show that the card scales properly on larger screens.

## Locales

- `en-US` — English (United States)
- `es-419` — Spanish (Latin America)
- `fr-FR` — French (France)
- `de-DE` — German (Germany)
- `it-IT` — Italian (Italy)
- `ml-IN` — Malayalam (India)
- `fil-PH` — Filipino / Tagalog (Philippines)

Each locale includes a title, short description, full description, screenshot captions and alt text, feature-graphic copy, six phone screenshots, two tablet screenshots, one 1024 × 500 feature graphic, and one 1080p localized promo video.

The brand name remains “Hope Cards” in every market. The localized copy is adapted around the terms users naturally associate with daily Bible verses, encouragement, favorites, notes, and reminders. It does not claim that the entire application interface is localized. The app currently offers Bible translations in these languages while the surrounding interface remains English; complete UI localization should be released before marketing the interface itself as translated.

## Suggested Play configuration

- Category: Books & Reference
- Primary positioning: daily Bible verse cards and calm Scripture encouragement
- Secondary positioning: Daily Hope, favorites, verse-linked journal notes, reminders, and themes
- Monetization wording: every feature is free; occasional ads; optional one-time purchase to remove ads; no subscription
- Privacy policy: `https://aaronsedna.com/privacy/hope-cards/`

## Store listing experiments

Traffic is currently too small for a statistically useful multi-variant test. Start with the prepared set. Once the listing has enough visitors, test one substantial change at a time:

1. Screenshot 1 caption: “Draw a card. Find a timely verse.” versus “A Bible verse for the moment you need it.”
2. Screenshot ordering: card front first versus card back first.
3. Feature graphic: stacked cards versus a single open verse card.

Keep each experiment running until Google Play reports a meaningful result; do not make decisions from a few dozen visits.

## Regeneration and validation

Run from the repository root on macOS:

```sh
swift scripts/generate-play-store-assets.swift
scripts/generate-play-store-videos.sh
scripts/validate-play-store-assets.sh
```

The generator uses the bundled Poppins and Source Serif fonts and the source captures under `assets/store/source/`. Update those captures whenever the UI changes, then regenerate every locale so the visual system remains consistent.

### Fresh native screenshots

`StoreScreenshotCapture` is an opt-in instrumentation capture utility. Build the debug app and instrumentation APK with `-PscreenshotMode=true`, install both on an emulator, then run:

```sh
adb -s EMULATOR_SERIAL shell am instrument -w -r \
  -e class com.aaronsedna.hopecards.ui.StoreScreenshotCapture \
  -e captureStore true -e captureDevice phone \
  com.aaronsedna.hopecards.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Use `captureDevice tablet` on a tablet emulator for the two tablet screens. Optional `captureLocale en-US` limits a run to one language. Captures use real translated verses and sample journal content, wait for entrance animations, and restore the prior settings, favorites, and journal afterward. Output is under the debug app's external files directory, `store-captures/phone/<locale>/` or `store-captures/tablet/<locale>/`. The utility is skipped unless both screenshot mode and the explicit capture argument are enabled.

For uncropped captures at 1080 × 1920 (phone) and 1600 × 2560 (tablet), export fresh, opaque sRGB screenshots to a separate directory:

```sh
swift scripts/generate-play-store-assets.swift \
  --source-root /path/to/store-captures \
  --output-root /path/to/export \
  --full-screens --screenshots-only
```

This writes only screenshot artwork, using localized phone and tablet sources where available.

## Preview videos

The localized videos are 32-second, 1920 × 1080 MP4 files under `videos/<locale>/`. Each scene remains visible long enough to read comfortably and uses the supplied “Open Hands Glow” music with gentle fades. Upload each video to YouTube as **Unlisted**, turn monetization/ads off, keep age restriction disabled, and paste its YouTube URL into the matching Play Store language listing. Google Play may autoplay up to 30 seconds muted, so the core experience appears within that window. The opening and closing frame is generated in the locale language; the center scenes use the localized screenshot artwork and real app captures.
