# Fresh daily-verse screenshots

Captured September 29, 2026 from current version 44 source using the existing screenshot-mode debug build and `StoreScreenshotCapture#captureListingScreenshots`. Only phone emulator `emulator-5554` was used. No real device, production installation, live listing or Ads setting was changed.

The local debug and test APK builds passed. The English and Malayalam capture runs each reported `OK (1 test)` in 27.060 and 24.576 seconds respectively. The capture utility seeds a representative verse and sample favorites/journal, and restores prior data in its finally block. These are authentic app-rendered sample screens, not proof that the seeded verse was the user's naturally selected daily verse.

Twelve source PNGs were saved under `output/aso-2026-09-29/source/`, six per language. All have verified dimensions 1080 × 2424. Four daily-verse-focused candidates were inspected individually for readability and retained byte-for-byte in tracked assets:

| Language | Verse card | Daily Hope |
| --- | --- | --- |
| English | [Matthew 11:28](../../../assets/store/candidates/2026-09-29/en-US/02-card-front.png) | [Philippians 4:13](../../../assets/store/candidates/2026-09-29/en-US/03-daily-hope.png) |
| Malayalam | [Matthew 11:28](../../../assets/store/candidates/2026-09-29/ml-IN/02-card-front.png) | [Philippians 4:13](../../../assets/store/candidates/2026-09-29/ml-IN/03-daily-hope.png) |

The four reviewed images show complete verse text, reference and translation attribution with no observed missing glyphs or clipped controls. Malayalam Daily Hope now shows the localized date as rendered by the current app. English navigation labels remain intact. The app's own status area and typography are preserved; no UI content was retouched or reconstructed.

The other eight captures were dimension-checked but not individually visually approved. No screenshot has been uploaded in this task. These tall source files are not yet a new 9:16 eight-image Play upload set; they are reusable evidence/candidates for promotion and later store-asset preparation. Existing approved assets remain unchanged.

See [manifest](screenshot-candidates.json) for paths, SHA-256, dimensions and review state. Follow the [promotion package](PROMOTION-ASO.md) before any later experiment; avoid simultaneous description and image changes.
