# Release 1.0.13 SDK audit

The version-45 `releaseRuntimeClasspath` was resolved and inspected on October 6, 2026. All 133 resolved Maven coordinates exactly match version 44. Fresh official Google Maven/Maven Central metadata checks completed with no errors or unlisted versions. OSV checks completed for all 133 coordinates with no reported vulnerabilities; this is a point-in-time advisory check.

Mobile Ads 25.4.0, WorkManager 2.11.2 and Guava 33.6.0-android retain the existing Android 6/API 23 support. Their newer stable versions require API 24. Billing 9.1.0, UMP 4.0.0 and Fragment 1.9.1 remain selected. Gradle dependency insight verifies Mobile Ads and the direct Fragment override. No compatible mandatory SDK update was identified. The 98 coordinates with newer stable metadata are not 98 required upgrades. The transitive deprecated LocalBroadcastManager remains an advisory, without a current Play SDK warning.

Dependency metadata remains enabled. The signed AAB contains dependencies.pb and R8 mapping. Play parsed version 45 with no outdated-SDK warning, no release errors and no supported-device losses. It reports API 23+, target 36 and 16 KB page support. Its sole validation warning is unavailable debug symbols for third-party native libraries.

Sources: [Mobile Ads releases](https://developers.google.com/admob/android/rel-notes), [Billing releases](https://developer.android.com/google/play/billing/release-notes), [UMP releases](https://developers.google.com/admob/android/privacy/release-notes), [WorkManager releases](https://developer.android.com/jetpack/androidx/releases/work), [Guava releases](https://github.com/google/guava/releases), [LocalBroadcastManager status](https://developer.android.com/jetpack/androidx/releases/localbroadcastmanager).

Evidence: `output/release-1.0.13/modules.json`, `version-audit.json`, `osv-audit.json`, `audit-summary.json`, `dependency-insight.log`, `test-harness-and-insight.log`, and `artifact-verification.json`.
