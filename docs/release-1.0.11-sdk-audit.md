# Release 1.0.11 SDK audit

The release runtime graph was inspected again at `2026-09-28T06:09:13.063808+00:00`, before building version 41. All 133 resolved coordinates match the version-40 graph. Primary Google Maven/Maven Central metadata was available for every coordinate, every selected version remains listed, and all 133 OSV queries completed without reported vulnerabilities. This is a point-in-time advisory check, not proof that a dependency has no defects.

The [version-40 compatibility decisions](release-1.0.10-sdk-audit.md) still apply. In particular, newer Mobile Ads, WorkManager and Guava releases require API 24; selected newer AndroidX releases require compileSdk 37/AGP 9.1. This update retains minSdk 23, compileSdk 36 and AGP 8.13.2. Billing 9.1.0, UMP 4.0.0 and Fragment 1.9.1 remain selected. The 98 coordinates with newer stable releases are not 98 required upgrades. The deprecated transitive LocalBroadcastManager remains an advisory rather than a demonstrated policy blocker.

Gradle `releaseRuntimeClasspath` and dependency insight were rerun. Dependency metadata remains enabled. The signed bundle contains both dependency metadata and its R8 mapping; bundletool validation and identity/signature checks pass.

Local evidence is under `output/quality-2026-09-28/`: `modules.json`, `version-audit.json`, `osv-audit.json`, `audit-summary.json`, `dependency-insight.log`, `unit-dependencies.log`, and `v41/artifact-verification.json`. Google Play's validation of the exact uploaded bundle remains a separate release gate.
