# Hope Cards 1.0.14 (47) release SDK audit

The final `releaseRuntimeClasspath` resolves 133 Maven coordinates, exactly matching the graph freshly checked for the version-46 translation preview on October 8, 2026. Official Google Maven/Maven Central metadata checks completed for every coordinate. OSV checked all 133 and reported no vulnerability findings at 2026-10-08T16:13:38.351167+00:00. A point-in-time scan does not establish absence of every defect.

102 coordinates have newer stable metadata, including transitives. Compatibility and API-23 support matter: Ads 25.4.0, WorkManager 2.11.2 and Guava 33.6.0-android retain API 23; their newer lines require API 24. Billing 9.1.0, UMP 4.0.0 and the Fragment 1.9.1 override remain resolved. Deprecated transitive LocalBroadcastManager remains an advisory from the SDK graph. No known vulnerability or mandatory policy upgrade was identified. Optional Core/Lifecycle newer versions were not silently claimed as current.

Final Gradle dependency insight verifies Ads and Fragment. Dependency metadata stays enabled. Final Play validation of Enhanced 47 on internal and production tracks reports zero errors and one advisory for missing native debug symbols in bundled prebuilt native code, with no SDK warning. API 23, target 36, supported devices and signing identity are preserved. Automatic protection retains its existing off state because this optional protection requires API 24; minSdk was not raised.

Final checks: 93 debug and 93 release unit tests pass, four focused emulator language tests pass, debug/release lint have zero errors (66/67 warnings and one hint), and bundle build succeeds. The actual Google-signed APK was downloaded and checked independently for reviewed controls, resource completeness, placeholders and unchanged content.

Evidence: `output/translation-preview-1.0.14/{version-audit,osv-audit,audit-summary}.json`; `output/translation-final-1.0.14/{modules,content-and-dependency-validation,artifact-verification,google-generated-validation}.json`; `resolved-insight-corrected.log`; [production validation](aso/2026-10-08/keyword-research/v47-production-validation-expanded.txt).

Official sources and compatibility notes are linked in [TRANSLATION-PREVIEW.md](aso/2026-10-08/keyword-research/TRANSLATION-PREVIEW.md). Initial failures and corrected reruns are preserved there.
