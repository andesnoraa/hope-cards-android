# Translation preview artifact

Purpose: obtain Google's generated app-string translations in a draft, inspect them, and validate their integration without restarting the active metadata review. The sections below preserve intermediate version-46 evidence; the final version-47 checks supersede incomplete preview attempts. Publication status is recorded separately in PUBLICATION.md.

The latest-bundles page freshly shows 45 as the highest uploaded version. The local preview uses **1.0.14 (46)**, the existing application ID and existing upload signing configuration. Production remains **1.0.13 (45)**. The existing internal track's served version is 41; a new draft is separate from its serving release.

## SDK inspection

The freshly resolved release graph contains 133 Maven coordinates and exactly matches the previously audited version-45 graph. Official Google Maven/Maven Central metadata checks completed for every coordinate, with no lookup errors or unlisted current versions. OSV checked all 133 coordinates and reported no vulnerability findings at `2026-10-08T16:13:38.351167+00:00`. This is a point-in-time advisory check, not a guarantee that all code is defect-free.

102 coordinates have newer stable metadata. That includes transitive libraries; it does not mean 102 mandatory or compatible upgrades. Mobile Ads 25.4.0, WorkManager 2.11.2 and Guava 33.6.0-android retain the existing API-23 support; the newer lines require API 24. Billing 9.1.0, UMP 4.0.0 and the Fragment 1.9.1 override remain selected. The deprecated transitive LocalBroadcastManager advisory remains recorded. No known vulnerability, incompatible policy deadline or mandatory SDK change was identified from this graph. Optional Core/Lifecycle updates are outside the translation change and are not silently represented as current latest versions.

Gradle dependency insight verifies Mobile Ads and Fragment. Dependency metadata stays enabled. Fresh Console validation of the exact preview artifact must still be recorded; the prior artifact's successful validation is not a substitute.

Sources: [Mobile Ads release notes](https://developers.google.com/admob/android/rel-notes), [Billing release notes](https://developer.android.com/google/play/billing/release-notes), [Fragment](https://developer.android.com/jetpack/androidx/releases/fragment), [Core](https://developer.android.com/jetpack/androidx/releases/core), [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle), [WorkManager](https://developer.android.com/jetpack/androidx/releases/work), [Guava](https://github.com/google/guava/releases), [LocalBroadcastManager](https://developer.android.com/jetpack/androidx/releases/localbroadcastmanager).

Local audit evidence: `output/translation-preview-1.0.14/modules.json`, `version-audit.json`, `osv-audit.json`, `audit-summary.json`, `dependency-audit.log` and `fragment-insight.log`.

## Integration changes

- General UI follows the device language instead of forcing US English. Quiz and certificate controls retain their separate selected-Bible resource context.
- Device-language changes refresh the remembered UI context.
- Malayalam general UI uses bundled Noto Sans Malayalam, with an API-23-compatible locale lookup. Other UI uses Poppins. Explicit quiz/Scripture font choices remain tied to their language.
- Language splitting is disabled because users select quiz/certificate languages independently of the device language and need those bundled translations offline.
- `app_name` and `nav_home` protect the Hope Cards brand from translation.
- Gallery controls are now eligible for Gemini. Those 14 new resource names intentionally fall back to English in local builds and unsupported Gemini languages. `MissingTranslation` is scoped to that Google-managed resource file; other translation checks remain enabled. No SDK warning or dependency metadata is suppressed.
- The Bible chooser explains that it selects Bible text and quiz language. It does not select the general interface language.

## Verification record

Initial debug compilation failed because the new test referenced a nonexistent resource name; the test was corrected to use `nav_settings`. Initial lint caught the API-24 locale accessor and gallery translations that are supplied during Play processing. The production accessor now uses ConfigurationCompat, and the file-specific Google-managed fallback is documented above.

The initial emulator run completed four methods: three passed, and French-interface/German-quiz rendering failed during activity teardown with `Activity never becomes requested state [DESTROYED]`. Its text assertions were not reported as failures. An isolated rerun passed in 11.789 seconds. Both outputs are preserved in this directory. The isolated rerun alone did not establish a complete clean suite.

The final debug build passed all four emulator methods in **53.156 seconds**, with no failures. Both JVM suites passed **93 tests** with no failures/errors/skips. Debug and release lint report **zero errors**, with 61/62 warnings and one hint respectively. Ten new warnings concern localized duplicates of the now-protected brand names; their text remains Hope Cards. The dynamic-language warning persists in lint; inspect the exact bundle's split configuration rather than infer that the Gradle declaration was applied. Other warnings are existing advisories.

Actual French and Malayalam home screens rendered translated controls without clipping; the Malayalam menu used the bundled font and fit its labels, including a two-line remove-ads label. These captures use existing local resource wording and do not claim to preview Gemini's generated text. App locales were temporarily set for the debug package only; the original empty override was restored. Evidence is `emulator-french-interface.png`, `emulator-malayalam-interface.png`, `emulator-malayalam-menu.png`, `verification.json` and the before/after locale records.

The final signed bundle build completed successfully. Bundletool validation and upload-certificate verification passed. The actual `BundleConfig` contains `LANGUAGE` with `negate: true`, confirming disabled language splitting despite lint's advisory. Every file in `base/assets/` has an identical SHA-256 to version 45, preserving Scripture, quiz questions/answers and artwork. Production ad IDs, non-debug mode, mapping and dependency metadata were verified. This validates the uploaded source artifact; it does not validate Google's later enhanced output.

## Google preview findings and corrected candidate

Google initially failed German generation. Retrying the same source bundle succeeded; the saved internal draft showed Enhanced 46. Console validation reported no blocking errors and one advisory about absent native debug symbols for bundled prebuilt native code. No SDK warning was shown.

The Console-generated Play-signed universal APK was downloaded and verified. It contains Portuguese and the gallery translations for all six selected languages, with no string-format placeholder mismatch in surviving app strings. All 94 assets match the source bundle. Some existing wording is unchanged, and the updated English Bible-picker explanation initially retained the old wording in five languages. Those five explanations were reviewed and corrected in Google's catalog.

Actual Portuguese emulator rendering exposed “Comprar uma carta” (“Buy a card”) for the free Draw a Card button. Five related drawing controls were corrected in the catalog. The signed downloadable draft still contained its initial wording immediately after catalog edits; it was not cleared for rollout. Re-uploading the accepted version 46 was rejected as already used. The saved draft was restored and a new candidate uses **version 47**, with versionName 1.0.14. The first failed German attempt, accepted draft, version-used response and restored draft remain distinct events.

The final source retains Google's Portuguese export locally, plus the reviewed five control corrections. Eleven unused resource names that were removed by release shrinking retain English fallback; they are not missing visible release strings. The About feature list now references individual UI labels, so Portuguese and other locales share the same Google-managed terminology rather than an untranslated or older independent array. The Bible-picker explanations also have corrected local fallback, including Filipino, which Google does not offer.

Adding a partial Portuguese fallback first caused 307 MissingTranslation errors. This was resolved by retaining the complete Google-generated export, rather than suppressing all Portuguese translation checks. Final validation below supersedes that incomplete attempt.

The final version-47 build succeeded in 2m 24s. Debug/release JVM suites each passed 93 tests. All four emulator language tests passed in 12.84 seconds. Debug/release lint report zero errors and 66/67 warnings plus one hint; the new Portuguese typography/plural advisories remain documented rather than hidden. The initially incorrect Gradle init-script path was corrected; the actual final resolved 133-module graph exactly matches the freshly audited v46 graph. Bundletool, signing identity, production configuration, disabled language splitting and 94 unchanged assets all pass for the actual v47 bundle. These local checks do not replace testing Google's final generated APK.


## Final Google-generated version 47

The final Play-signed APK was downloaded from Console and installed over the existing production package on **emulator-5554**. Its version code, Play certificate and SHA-256 were verified. Google's output applies all five reviewed Portuguese drawing controls, all five corrected Bible-picker explanations, and the resource-referenced About feature list. All **315 surviving app resource names** are present in each of **pt, de, fr, it, ml, es** with no string-format placeholder mismatch. This is resource completeness and reviewed-control validation, not a native-speaker review of every sentence. Google's service keeps some unchanged wording for consistency; not every earlier translation was freshly rewritten. All 94 packaged assets remain byte-identical to the source bundle, including Scripture and quiz content.

Actual version-47 Portuguese Home and About screens show **Tirar uma carta** and the localized feature list without clipping. Evidence: `google-v47-portuguese-home.png/.xml`, `google-v47-portuguese-about.png/.xml`, and `output/translation-final-1.0.14/google-generated-validation.json`. These captures supersede the incorrect version-46 Portuguese control. The original empty production-package locale override was restored after testing (`emulator-final-locale-restored.txt`). The four focused instrumentation tests were run on the final debug build; manual Google-signed APK checks are a separate evidence scope.

Exact version-47 internal and production validation each report **zero blocking errors** and **one native-debug-symbol advisory**, with no SDK warning and unchanged device coverage. Enhanced version 47 was published to the existing internal track and is **available to internal testers**, released on October 8 at 10:56 PM (Console display). The final production status is recorded in [PUBLICATION.md](PUBLICATION.md).
