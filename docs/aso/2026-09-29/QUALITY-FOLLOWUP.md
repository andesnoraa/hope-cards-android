# Version 44 Console advisories

Observed September 29, 2026 during the read-only ASO heartbeat, around 05:09 UTC. Keep this product-quality follow-up separate from listing optimization and under the existing release gates. No code or artifact changes were made.

Console's Monitor and improve page displayed two “Memory usage” recommendations for release **1.0.12 (44)**:

1. BitmapFactory usage without downsampling. Console recommends target-resolution decoding or an image library.
2. R8 configuration may cause higher memory usage or lower performance.

These are verified **Console advisories**, not verified production regressions. Crash, ANR and cold-start metrics remain unavailable. The source did not provide a measured memory increase, stack trace, affected-device count or battery-drain measurement.

A bounded source inspection provides useful context:

- `android/app/src/main/java/com/aaronsedna/hopecards/data/VerseArtImages.kt:106` decodes a generated cached bitmap without options. Cache keys include requested size, and the loader accepts only 360 or 1080 pixels and validates decoded dimensions. Full decoding of an already correctly sized file is not automatically a defect. A corrupt/wrong-size cache would allocate before dimensions are checked; evaluate a bounds-first decode if reproducing meaningful excess memory.
- `VerseArtRenderer.kt:129` already sets `inSampleSize` to 2 for thumbnails and 1 for exports. Any optimization should respect target dimensions and shared-image quality.
- `android/app/build.gradle:76` enables minification and line 82 uses `proguard-android-optimize.txt`. `android/app/proguard-rules.pro` has a broad keep rule for Google's internal consent SDK. Determine why that rule exists and test consent/ads behavior before narrowing it; do not remove it merely to silence a recommendation.

The existing [release record](../../release-1.0.12.md) contains the banner ownership fix and nuanced heap findings. Preserve its failed raw heap-budget evidence and test-Ad-Inspector attribution; do not replace it with an unqualified memory-pass claim.

Follow-up, when separately doing product/release work: inspect the exact Play recommendation details and retained R8 graph, reproduce with the emulator and representative thumbnails/exports, then run relevant consent, ad, memory and release checks if a change is justified. No new binary is authorized by this ASO heartbeat.
