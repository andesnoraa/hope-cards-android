# Release 1.0.9 (36)

Bible Quiz follows the selected Bible translation across English, Malayalam, German, French, Italian, Spanish and Filipino. Randomized rounds have clear correct/incorrect feedback, Scripture references, optional sound, wrong-answer vibration, and localized sharing, PDF export and printing. Results remain visible before an eligible interstitial is considered when leaving or starting another round. Approved verse-image backgrounds rotate on gallery entry without a background timer.

## Release verification, September 27, 2026

- Preserved application ID, upload key, minSdk 23, targetSdk 36 and existing Play listing. Previous highest uploaded artifact was 35.
- Audited all 133 release runtime artifact coordinates against primary Maven metadata and OSV. Metadata requests succeeded; no reported OSV vulnerabilities at audit time. The 99 newer stable coordinates include incompatible SDK/toolchain updates. This is a point-in-time advisory check.
- Verified Ads, Fragment, WorkManager and Guava with Gradle dependency insight. Retained compatible Ads 25.4.0, WorkManager 2.11.2 and Guava 33.6.0-android: newer versions require API 24. New Core, Compose and lifecycle-compose versions require compileSdk 37 / AGP 9.1. Billing 9.1.0 and UMP 4.0.0 retained. SDK dependency metadata remains enabled.
- Production monetization validation, 57 debug and 57 release unit tests, debug/release lint and release bundle build passed. Existing lint advisory warnings remain.
- Physical Samsung SM-A165F instrumentation completed. A stale gallery-label assertion was corrected to match the existing parenthesized translation label. That test and an intermittent share-navigation test passed in an isolated rerun. Quiz interactions, navigation and PDF coverage passed.
- Validated and installed the exact AAB-derived universal APK on a separate emulator data image, preserving the existing emulator's differently signed installation. Verified offline quiz operation. Production purchases and Play-delivered ad behavior are not claimed from sideload testing.
- Signed bundle: `output/release-1.0.9/hope-cards-1.0.9-v36.aab`, 37,681,593 bytes. SHA-256: `394940eac65a7925e114d3d28a2bd5ab8f6092f1a2a9807c42325f07f45b3e89`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.
- Bundletool validation, APK signature and manifest checks passed. AD_ID, R8 mapping and dependency metadata are present. Play accepted the bundle without an outdated SDK warning or supported-device loss. Nonblocking third-party native-symbol warning remains; existing protection configuration preserved.

## Publication

Play Console verified version 36 (1.0.9) as **Available on Google Play**, released September 27 at 9:47 AM, 100% production rollout across 177 countries/regions and 20,367 supported devices. The public web listing temporarily continued to show the September 23 metadata after Console confirmed the release.

Question counts are omitted from promotional copy and revised release notes. Marketing headline: “Daily Bible Verses, Bible Quiz, Beautiful Verse Images & More.” No “Coming soon” or “A little Scripture” wording.

## Store screenshots

`QuizStoreScreenshotCapture` is an opt-in instrumentation authoring tool requiring both `-PscreenshotMode=true` and `-e captureQuizStore true`. It uses the real app, questions and completed rounds, restores original app settings, and does not change production ad behavior. All seven languages were captured on the physical Galaxy A16 at its native 1080 × 2340 resolution and density 450. Source images and logs are under `output/quiz-launch-2026-09-27/galaxy-a16/` and its parent directory. Earlier emulator captures are superseded for listing use.

The question and results images for every locale were uploaded, applied and saved in the existing Play listing. All seven screenshot changes were submitted and Play Console confirmed **Changes in review**. Managed publishing is off, so approval publishes these metadata changes automatically. Version 36 itself is already live. Count-free English release notes were verified on the live release details. The normal debug app was reinstalled successfully on the Galaxy A16 after capture.

See [quiz launch notes](quiz-launch-2026-09-27.md) for promotional assets, published announcement links, community research and the user's subsequent draft-only social publication preference.
