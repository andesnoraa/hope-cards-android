# Release 1.0.7 (34)

## Changes

Reminder hour/minute fields select the entire value on focus or a repeated tap. Typing replaces the selected value; two-digit numeric input is validated (hour 1–12, minute 0–59), empty/zero-hour values cannot be saved, Next moves to minutes, and Done dismisses the keyboard. The dialog survives recreation and scrolls above the keyboard. Existing reminder preferences and opt-outs are preserved.

Includes the approved reusable verse-card work: 30 WebP backgrounds (4,142,594 bytes), upright lettering, short complete verses across ten editions, adaptive text contrast without a washout layer, varied scene ordering, and the approved Quiet Branch replacement. Production contains no legacy flattened verse-card collection. Legacy artwork and authoring previews are test-only. Daily verse source verification, notification routing, music cleanup and compact save/share feedback are included.

## Dependency and artifact audit — 2026-09-22

- Existing application ID and upload signing identity retained. Google Play's highest uploaded version was 33 before this release; version 34 supersedes it.
- Inspected the complete releaseRuntimeClasspath and primary Google Maven/Maven Central version metadata. Queried OSV for all 183 resolved Maven packages: no reported vulnerabilities. This is a point-in-time advisory scan, not a guarantee that no vulnerabilities exist.
- Updated Core KTX to 1.18.0, Splashscreen to 1.2.0, and Lifecycle Compose to 2.10.0. Gradle AAR checks verified compatibility with compileSdk 36 / AGP 8.13.2.
- Core 1.19.0 and Lifecycle Compose 2.11.0 require compileSdk 37 and AGP 9.1; retain compatible stable versions without an unrelated build-system migration.
- Mobile Ads 25.4.0 retains Android 6 support; 25.5.0 requires API 24. Guava 33.6.0-android likewise retains API 23 support. minSdk remains 23, targetSdk 36.
- Verified dependency insight for Ads 25.4.0, Fragment 1.9.0, WorkManager 2.11.2, Guava 33.6.0-android and Lifecycle 2.10.0. Billing 9.1.0 and UMP 4.0.0 are current stable versions checked for this release.
- Production AdMob identifiers validated; SDK dependency metadata remains enabled and is present in the signed AAB. R8 and resource shrinking are enabled. Bundletool validation and APK signature verification passed; installed the AAB-derived universal APK for smoke testing.
- AAB SHA-256: `2d6dc0bedbcff073df1db9b922d7a6bd9b6942c8fa9a0b01aa7013df3d1aa306`. Size: 37,344,902 bytes. Play reports a 31.8 MB new-install download, 25.7 MB less than version 33, with no supported-device loss.

## Verification

- 44 debug and 44 release unit tests passed. Debug/release lint and signed release AAB/APK builds passed (no lint errors).
- Samsung SM-A165F: actual touch selection/replacement, invalid input, keyboard actions, recreation, persistence, cancel and hour wrapping checked. Capture before fix: local `output/time-input-fix/before.png`; after: `output/time-input-fix/after.png`.
- Instrumentation coverage includes all ten editions, short-card eligibility, artwork decoding/layout, bounded caches, backup round trips, notification cold launch, audio interruptions, repeated lifecycle navigation, saving/sharing and back navigation. Headless asset/file checks are separated from the Compose-rule test dispatcher; UI checks wait for asynchronous gallery restoration. Date assertions allow Android ICU capitalization variations while preserving words, punctuation and date order.
- After 24 physical-device Daily Hope background/resume/navigation cycles, settled Java+native allocations were 91,166,456 bytes versus 94,973,360 after warm-up; the last two batches differed by 149,152 bytes. Audio stopped in the background and after leaving Daily Hope. This short regression check is not a battery benchmark.
- Final signed AAB-derived app installed over the existing emulator package without clearing data. Verified launch, reminder selection (`08` replaced by `11`), Cancel preserving 08:00, and offline gallery rendering. Restored airplane mode after testing.
- Real purchases and production ad delivery are not claimed from sideload testing; those require the Play-installed signing identity and configured license tester. Debug tests use Google test ads.

## Google Play validation

- Production rollout targets the existing 177 countries, 100% of users. Release notes supplied in all seven listing languages.
- Play raised an advertising-ID declaration error for an active artifact. This exact AAB/APK explicitly contains `com.google.android.gms.permission.AD_ID`; the existing truthful “uses advertising ID” declaration was retained and the legacy-artifact error acknowledged for this release.
- Nonblocking warning: third-party native code has no uploaded native debug symbols; the R8 mapping is attached. No outdated SDK warning was raised for build 34.
- Automatic protection was already off on the existing listing; no security setting was disabled during this release.

Primary references: [Mobile Ads releases](https://developers.google.com/admob/android/rel-notes), [Billing releases](https://developer.android.com/google/play/billing/release-notes), [Fragment releases](https://developer.android.com/jetpack/androidx/releases/fragment), [WorkManager releases](https://developer.android.com/jetpack/androidx/releases/work), [UMP releases](https://developers.google.com/admob/android/privacy/release-notes).
