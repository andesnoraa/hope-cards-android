# Hope Cards 1.0.16 (49) release audit

Release date: 2026-11-19. Application ID: `com.aaronsedna.hopecards`.

## Google Play preflight

Before upload, the existing Production listing showed version 48 (1.0.15) available at 100% rollout in 178 of 178 countries. Submission 41 was published on November 9; no newer submission or unpublished change was shown. The active bundle details showed target SDK 36, minimum API 24, 16 KB page-size support, and no SDK warning in the displayed bundle details. The historical 1.0.14 audit records a non-blocking advisory for native debug symbols from prebuilt libraries.

## Release runtime dependency review

The resolved `releaseRuntimeClasspath` and dependency insight were reviewed for Play SDK warnings, outdated dependencies, and security overrides. Selected versions include Google Mobile Ads 25.5.0, Unity Ads 4.21.0, Unity mediation 4.21.0.0, UMP 4.0.0, Billing 9.1.0, Fragment 1.9.1, WorkManager 2.12.0, DataStore Preferences 1.3.0, Guava 33.7.2-android, and OkHttp 4.12.0. Dependency metadata remains enabled. WorkManager 2.12.0 is compatible with this app's existing minimum API 24; DataStore 1.3.0 passes AAR metadata checks with the current toolchain.

Stable Compose BOM 2026.09.00 and Core 1.19.1 were evaluated but rejected for this release: their AAR metadata requires compile SDK 37 and AGP 9.1+, while the project uses compile SDK 36 and AGP 8.13.2. Lifecycle remains on 2.10.0 because stable 2.11.0 requires AGP 9.2 for its Compose compile SDK. No compile SDK, target SDK, or minimum API increase was made.

Guava and OkHttp remain constrained to the versions addressing the vulnerabilities recorded in the prior SDK audit. That audit's OSV scan was a dated scan of its then-current graph, not a fresh scan of this bundle. No OSV scanner is installed in the build environment; this release record does not claim a new OSV scan.

The AAB contains three arm64 native libraries. Their ELF `PT_LOAD` segments were checked with NDK `llvm-readobj`; each reports 16 KB alignment.

## Artifact

- Version: 1.0.16 (versionCode 49)
- Bundle: `android/app/build/outputs/bundle/release/app-release.aab`
- SHA-256: `88a9a28aa1d6dc6e2f38af4d835305a97cddf89a04ce664fbd33010db1795196`
- Bundle manifest: package `com.aaronsedna.hopecards`, min API 24, target SDK 36
- `jarsigner -verify`: bundle signature verified; Gradle used the existing release signing configuration.

## Validation

- Debug and release unit tests passed.
- Debug and release lint completed without errors; existing warnings remain.
- `validateReleaseMonetization` passed.
- The full emulator instrumentation suite on API 37 completed with 91 passed, 10 skipped, and 11 failed. Repeating the 11 affected tests on API 34 completed with 8 passed and 3 failed. Failures reproduced across API levels: the daily-title/gallery text assertions and a quiz exit callback assertion after saved-state restoration. Other full-suite failures were certificate date-picker visibility/value assertions, quiz navigation after sharing/review, and Daily Hope lifecycle audio state. The ad placement test `officialTestInterstitialShowsAtQuizBreakAndNeverOnDailyHope`, all quiz timer tests, and certificate PDF unit tests passed. This is a known test gate and is not reported as a clean device suite.
- Emulator: Pixel 9 API 37 / 16 KB image and Speedometer API 34 tablet image. A first connected-test invocation was stopped after it also began installing on the attached phone; subsequent test runs used explicit emulator serials only.
- The production bundle was signed and checked before instrumentation; no app source changes were made after it was built.

## Play submission

Pending Console upload and submission; record the submission status and identifier after the Play Console workflow completes.
