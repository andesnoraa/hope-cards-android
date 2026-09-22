# Release 1.0.8 (35)

## Changes

Updated navigation selection colors across themes, simplified Hope Cards / Remove ads labels, and kept All artwork and Saved artwork visible above the category list. The drawer scrolls on smaller displays. The requested Share Hope Cards drawer entry is absent.

Gallery captions use the selected Bible edition's reference instead of an English editorial title. Replaced two unsuitable backgrounds with the approved forest-road photograph; the library now contains 29 unique reusable WebPs (3,514,662 bytes). Upright lettering, short complete verses, selective ink contrast, and no washout filters remain in place. Notifications use the supplied sparkle vector as their small icon while retaining the app identity.

## Release audit — 2026-09-23

- Play Console verified version 34 (1.0.7) as the highest uploaded bundle and the current production release before upload. Version 35 preserves the application ID, upload key, listing, minSdk 23 and targetSdk 36.
- Inspected releaseRuntimeClasspath and primary Google Maven/Maven Central metadata for all 133 resolved artifact coordinates. OSV returned no reported vulnerabilities. All metadata requests succeeded. This is a point-in-time advisory check.
- Compatible dependencies remain unchanged from 1.0.7: Ads 25.4.0, Billing 9.1.0, UMP 4.0.0, Fragment 1.9.0, WorkManager 2.11.2, Guava 33.6.0-android, Core 1.18.0 and Lifecycle 2.10.0. Verified Gradle dependency insight for Ads, Fragment, WorkManager and Guava. Newer Ads/Guava require API 24; newer Core/Lifecycle require compileSdk 37 / AGP 9.1. Retained Android 6 compatibility and the current stable build system. SDK-owned transitive dependency sets were inspected without independently forcing unrelated major versions.
- Production monetization validation, debug/release lint, 44 debug unit tests and 44 release unit tests passed. Lint has no errors; existing advisory warnings remain. The two static-context warnings refer to applicationContext-only renderer/repository instances, not Activities. The inlined balanced-line-breaking constant is passed to StaticLayout's API-23 builder and has the same numeric value as Layout.BREAK_STRATEGY_BALANCED.
- Release AAB/APK builds, bundletool validation and APK signature verification passed. R8, resource shrinking and SDK dependency metadata remain enabled; dependencies.pb and R8 mapping are in the AAB.
- Signed AAB: 36,713,352 bytes; SHA-256 `f03a802efe79cfc9ffcba27e7bab0cf81c01d109498df30b57d6eed817a015bd`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.

## Device verification

- Samsung SM-A165F: full instrumentation run completed in 213 seconds, 30 passing tests and six optional authoring/capture methods skipped. Coverage includes ten Bible editions, complete short artwork text, bounded bitmap caches, backups, saved-image export, notification routing, reminders, share cancellation/back navigation, date localization, card responsiveness and audio lifecycle.
- After warm-up, Java plus native heap was 121,678,600 bytes; after 8/16/24 navigation cycles: 117,973,176 / 117,951,176 / 117,851,384 bytes. Music paused for interruptions and backgrounding, resumed on focus, and stopped after leaving Daily Hope. No crash, ANR, OOM or leak diagnostic was recorded for this test process. This is a regression check, not a battery benchmark.
- Physical-device manual checks confirmed drawer labels, absent Share Hope Cards item, saved verses, existing journal entry, Settings and the Remove ads screen. A Google test banner rendered correctly. Bible language and theme remained Malayalam / Classic.
- Installed the exact AAB-derived signed universal APK over the existing emulator app without clearing data. Verified 1.0.8 navigation and offline artwork rendering in the retained Filipino edition. The phone's existing portrait behavior remained stable during an orientation change. Restored emulator airplane mode, Wi-Fi and rotation settings.
- Real purchases, pending/restore/refund cases and production ad delivery are not claimed from a debug or upload-key sideload. These require a Play-installed build and configured license tester. Entitlement and consent policies were checked in source and existing unit tests.

## Play validation

- Bundle 35 accepted with no outdated SDK warning and no supported-device loss. Play estimates 31.2 MB for new installs (628 KB smaller) and 3.75 MB for updates.
- An advertising-ID error applies to one of the active artifacts. Verified the exact version-35 bundle manifest contains AD_ID; retained the truthful declaration and acknowledged the legacy-artifact error for this release.
- Nonblocking warning: third-party native code lacks native debug symbols; the R8 mapping is attached. Existing automatic protection configuration was unchanged.
- Release notes provided in all seven listing languages. Submitted for full production rollout to the existing 177 countries at approximately 01:35 IST on September 23, 2026. Publishing overview confirms “Changes in review” for 35 (1.0.8), with quick checks still running. Managed publishing is off: Google will publish automatically after successful checks and approval. Approval/live availability is not yet claimed.

Primary references: [Mobile Ads releases](https://developers.google.com/admob/android/rel-notes), [Billing releases](https://developer.android.com/google/play/billing/release-notes), [UMP releases](https://developers.google.com/admob/android/privacy/release-notes).
