# Release 1.0.10 SDK audit

Audited the final 133 resolved `releaseRuntimeClasspath` artifact coordinates on September 28, 2026 (India time; query timestamp `2026-09-27T20:03:18.693448+00:00`). All 133 primary Maven metadata requests succeeded, every resolved version exists in its registry, and OSV returned no reported vulnerabilities for these exact versions. This is a point-in-time advisory check, not a guarantee of absence of defects or Google Play acceptance.

## Dependency decisions

| Dependency | Release decision and evidence |
|---|---|
| Fragment | Updated 1.9.0 to **1.9.1**, the current stable release. Its published AAR requires minSdk 23, compileSdk 34 and AGP 8.1.1, compatible with this project. Gradle dependency insight confirms 1.9.1 replaces the older transitive requests. [Release notes](https://developer.android.com/jetpack/androidx/releases/fragment#1.9.1). |
| Google Mobile Ads | Retained **25.4.0**. Version 25.5.0 requires API 24, which would remove Android 6 support. The 25.x line remains supported; its published deprecation date is June 30, 2027. [Release notes](https://developers.google.com/admob/android/rel-notes), [support timeline](https://developers.google.com/admob/android/deprecation). |
| WorkManager | Retained **2.11.2**. Stable 2.12.0 raises minSdk from 23 to 24. [Release notes](https://developer.android.com/jetpack/androidx/releases/work#2.12.0). |
| Guava | Retained the existing **33.6.0-android** constraint. Newer 33.7.x requires API 24. `listenablefuture:9999.0-empty-to-avoid-conflict-with-guava` is the intentional empty companion artifact, not an outdated implementation. [Guava 33.7.0 release](https://github.com/google/guava/releases/tag/v33.7.0). |
| Play Billing | **9.1.0** remains current stable. Billing 9 is supported for new apps and updates through August 31, 2028. [Release notes](https://developer.android.com/google/play/billing/release-notes), [version support](https://developer.android.com/google/play/billing/deprecation-faq). |
| User Messaging Platform | **4.0.0** remains current stable and supports API 23. [Release notes](https://developers.google.com/admob/android/privacy/release-notes). |
| Core, Compose and Lifecycle | Retained current compatible versions. Published AAR metadata for Core 1.19.1, Compose Foundation 1.12.1 and both Lifecycle Compose 2.11.0 artifacts requires compileSdk 37 and AGP 9.1.0. The project uses compileSdk 36 and AGP 8.13.2. Metadata URLs and contents are recorded in the compatibility evidence. |
| Other direct runtime libraries | Activity 1.13.0, DataStore 1.2.1, Core Splashscreen 1.2.0, DocumentFile 1.1.0 and Play App Update 2.1.0 match the current stable versions in primary metadata. |

Primary version metadata lists 98 coordinates with a newer stable version. This includes artifacts whose newer releases require a different Android baseline/toolchain, plus dependencies selected by the SDKs themselves. The count is not a list of 98 required upgrades. Unrelated transitive major versions were not independently forced into the release.

## Deprecated transitive dependency

`androidx.localbroadcastmanager:localbroadcastmanager:1.0.0` remains transitively requested through Compose UI, Transition 1.6.0, DynamicAnimation 1.0.0 and `legacy-support-core-utils`. Application source contains no `LocalBroadcastManager` usage. The entire library, including its latest 1.1.0 version, is deprecated, so a version bump alone would not remove the deprecation. This is recorded as a dependency-maintenance advisory, with no OSV finding or demonstrated Play submission blocker. [AndroidX deprecation notice](https://developer.android.com/jetpack/androidx/releases/localbroadcastmanager).

SDK dependency metadata must stay enabled. Google Play's checks on the actual uploaded bundle remain authoritative for SDK-policy warnings. No broad SDK migration or minSdk increase is justified by the findings above.

## Evidence

The release output directory contains:

- `modules.json`: final resolved artifact coordinates.
- `version-audit.json`: per-module current version, newest stable version, primary registry URL and query timestamp.
- `osv-audit.json`: one OSV result per exact resolved coordinate.
- `audit-summary.json`: totals, query failures and findings.
- `newer-sdk-compatibility.json`: primary AAR metadata and manifests for the Fragment update and incompatible newer SDKs.
- `fragment-insight.txt`: Gradle resolution evidence for Fragment 1.9.1.
- `audit_runtime_dependencies.py`: reproducible metadata and OSV query script.

The audit did not run builds, modify application code, use devices, or submit a Play release. Release build, tests, lint, remaining dependency-insight reports and Play validation are tracked separately.
