# Hope Cards Google Play assets

This directory contains Hope Cards' tracked Play Store metadata and artwork. Some files are historical and do not mirror the latest Console listing. Confirm the current publication record and live Console before treating a tracked file as the published version.

## September 28 certificate-generator metadata

The `short-description.txt` and `full-description.txt` files in all seven listing languages now contain the prepared app-release copy. Existing titles are preserved. The descriptions introduce the Bible Quiz Certificate Generator, multiple-participant A4 PDFs, certificate-language selection and sharing. They also describe the optional 30-second countdown for each question, automatic advancement when time expires, and question-by-question PDF report. The English and Malayalam copy removes the old print action and fixed background count; the other five locale files previously lacked the quiz sections. This locally prepared text is not proof of publication. See [the metadata record](../../docs/aso/2026-09-28/certificate-generator-update.md) before saving or reporting its status.

## September 27 English and Malayalam screenshot sets

The September 27 ASO work prepared eight phone screenshots per locale under `output/aso-2026-09-27/en-US/` and `ml-IN/`. Both sets were saved in Console and included in the accepted submission, with this verified order:

1. Read Scripture — makes the daily Bible verse purpose immediately clear.
2. Daily Hope — shows the daily-use reason to return.
3. Bible Quiz — shows a real question and answer choices.
4. Quiz results — shows the score, answer review and export options.
5. Draw a card — explains the card interaction.
6. Favorites — shows that meaningful verses can be kept.
7. Journal — shows notes linked to their verses.
8. Themes — demonstrates visual personalization.

Each submitted image is an opaque RGB PNG at 1080 × 1920. The full approved source image is uniformly fitted onto a Hope ivory canvas without cropping content, stretching, reconstructing the UI or rewriting text. See `output/aso-2026-09-27/SCREENSHOTS.md` and `screenshot-manifest.json` for the source mappings, exact filenames and verification.

At **2026-09-27T15:19:59.614Z**, Publishing overview showed “Changes in review” and “Running quick checks” at 11%, with text saying the changes would be sent when checks succeeded. The submission was accepted into the publishing/review queue; quick-check success, approval and public visibility were not yet confirmed. Managed publishing is off. See [`docs/aso/2026-09-27/EXECUTION.md`](../../docs/aso/2026-09-27/EXECUTION.md) for the six submitted fields and follow-up record. No binary was released.

## Historical six-image listing order

The original locale sets used this order. It remains the mapping for the six files under `assets/store/screenshots/phone/`, rather than the submitted eight-image order above.

1. Draw a card — communicates the core interaction immediately.
2. Read Scripture — shows the complete verse card and typography.
3. Daily Hope — establishes the daily-use reason to return.
4. Favorites — shows that meaningful verses can be kept.
5. Journal — shows notes linked directly to their verses.
6. Themes — shows visual personalization without suggesting paid feature gating.

Do not infer dimensions from an old generation recipe. Direct inspection on September 27 found the original English and Malayalam phone PNGs are 1080 × 2424. The A16 quiz captures used for the new sets are 1080 × 2340. These original tall images are preserved, but they are not the new 9:16 upload files. Inspect each asset's actual dimensions before upload. The original tablet sets show how the card scales on larger screens.

## Locales

- `en-US` — English (United States)
- `es-419` — Spanish (Latin America)
- `fr-FR` — French (France)
- `de-DE` — German (Germany)
- `it-IT` — Italian (Italy)
- `ml-IN` — Malayalam (India)
- `fil-PH` — Filipino / Tagalog (Philippines)

Each original locale set includes a title, short description, full description, screenshot captions and alt text, feature-graphic copy, six phone screenshots, two tablet screenshots, a feature graphic, and a localized promo video. The new English and Malayalam eight-image sets are separate. Their tracked full descriptions were aligned with the verified pre-edit Console copy during this task, with no full-description change submitted. Other tracked metadata may still lag behind Console; do not replace live descriptions with an older file or an alternate candidate without checking the actual content.

The app's brand is “Hope Cards.” Some historical localized titles abbreviated it to “Hope,” and the live English title at the start of the September 27 audit omitted the brand. The ASO task addresses exact-brand discovery; the presence of a local title file does not prove that Google Play has published it.

Bible content and store copy are localized. Core app navigation and settings intentionally stay in English, as implemented in `Localization.kt`. Bible Quiz has separate language behavior matching the selected Bible translation. Do not claim that choosing a Bible translation fully localizes every screen, and do not replace accurate English controls in a Malayalam screenshot with invented translated UI.

## Suggested Play configuration

- Category: Books & Reference
- Primary positioning: daily Bible verse cards and calm Scripture encouragement
- Secondary positioning: Daily Hope, Bible Quiz, quiz participation certificates, verse images, favorites, verse-linked journal notes, reminders, and themes
- Monetization wording: every feature is free; occasional ads; optional one-time purchase to remove ads; no subscription
- Privacy policy: `https://aaronsedna.com/privacy/hope-cards/`

## Store listing experiments

Traffic is currently too small for a statistically useful multi-variant test. Start with the prepared set. Once the listing has enough visitors, test one substantial change at a time:

1. Screenshot 1 caption: “Draw a card. Find a timely verse.” versus “A Bible verse for the moment you need it.”
2. Screenshot ordering: card front first versus card back first.
3. Feature graphic: stacked cards versus a single open verse card.

Keep each experiment running until Google Play reports a meaningful result; do not make decisions from a few dozen visits.

## Regeneration and validation

The original authoring tools can reproduce the historical asset design from the repository root on macOS:

```sh
swift scripts/generate-play-store-assets.swift
scripts/generate-play-store-videos.sh
scripts/validate-play-store-assets.sh
```

The generator uses bundled fonts and source captures under `assets/store/source/`. Its fixed frames and marketing layout differ from the current screenshot-only sets. Do not blindly regenerate or overwrite approved assets. Use a separate output directory, inspect the result, and validate the files intended for upload. The existing validator assumes six 1080 × 1920 phone images per locale; it does not validate the separate eight-image ASO sets and will not pass the preserved taller originals merely because they were previously uploaded.

### Fresh native screenshots

`StoreScreenshotCapture` is an opt-in instrumentation capture utility. Build the debug app and instrumentation APK with `-PscreenshotMode=true`, install both on an emulator, then run:

```sh
adb -s EMULATOR_SERIAL shell am instrument -w -r \
  -e class com.aaronsedna.hopecards.ui.StoreScreenshotCapture \
  -e captureStore true -e captureDevice phone \
  com.aaronsedna.hopecards.debug.test/androidx.test.runner.AndroidJUnitRunner
```

Use `captureDevice tablet` on a tablet emulator for the two tablet screens. Optional `captureLocale en-US` limits a run to one language. Captures use real translated verses and sample journal content, wait for entrance animations, and restore the prior settings, favorites, and journal afterward. Output is under the debug app's external files directory, `store-captures/phone/<locale>/` or `store-captures/tablet/<locale>/`. The utility is skipped unless both screenshot mode and the explicit capture argument are enabled.

The legacy generator accepts `--full-screens --screenshots-only` and a separate output root:

```sh
swift scripts/generate-play-store-assets.swift \
  --source-root /path/to/store-captures \
  --output-root /path/to/export \
  --full-screens --screenshots-only
```

Those flags do not guarantee proportional fitting. The current implementation draws the complete source into a fixed inner rectangle, which can stretch taller captures; it also retains its generated screenshot layout. Do not use it for an unaltered capture unless the source and destination aspect ratios match and the result has been visually checked.

For the prepared September 27 English and Malayalam sets, the separate renderer preserves source proportions:

```sh
swift -module-cache-path /private/tmp/hope-aso-swift-cache \
  output/aso-2026-09-27/render-aspect-fit.swift
```

It writes only the new output sets, contact sheets and source manifest, leaving approved originals unchanged. Verify dimensions, opacity, source aspect ratio, all visible text and controls, and the intended upload order before saving in Console. Any source-image or renderer change requires a new visual check. Manifest hashes should be refreshed after regeneration.

## Preview videos

The localized videos are 32-second, 1920 × 1080 MP4 files under `videos/<locale>/`. Each scene remains visible long enough to read comfortably and uses the supplied “Open Hands Glow” music with gentle fades. Upload each video to YouTube as **Unlisted**, turn monetization/ads off, keep age restriction disabled, and paste its YouTube URL into the matching Play Store language listing. Google Play may autoplay up to 30 seconds muted, so the core experience appears within that window. The opening and closing frame is generated in the locale language; the center scenes use the localized screenshot artwork and real app captures.
