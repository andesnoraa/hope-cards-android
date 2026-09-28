# Release 1.0.11 (41)

Quiz answer review and its PDF now share one localized content snapshot and the same visual hierarchy: a navy and gold score summary, numbered Bold questions, explicitly labeled selected and correct answers, outcome badges, explanations, and Bible-reference panels. Native review supports scalable text and the existing themes. PDFs use A4 with 54-point margins, page numbers and grouped answer cards. Install invitations remain in the companion share text, outside PDF pages. See [quiz verification](quiz/review-pdf-parity-2026-09-28.md).

Checkout now queries fresh Google Play product details for each attempt, prevents duplicate pending lookups, and abandons delayed results after leaving the purchase screen or destroying its activity. The activity is weakly referenced. Existing purchased/pending, acknowledgement, restore and offline entitlement rules are preserved.

A stale interstitial-load success can no longer reset the state of a newer load after consent or entitlement changes. Daily Hope exclusion and the existing ten-minute interval remain intact. The verse-art renderer indexes reviewed designs once instead of allocating and scanning a list for each lookup; the key includes edition, verse ID and verified text to preserve duplicate-ID handling.

## Verification

- Debug and release JVM suites: 85 tests each passed, including three new fresh-checkout coordinator cases.
- Debug/release lint: zero errors; 51/52 warnings match the previous release.
- Release runtime SDK audit: all 133 resolved coordinates checked, no OSV findings or metadata failures. [Audit and compatibility decisions](release-1.0.11-sdk-audit.md).
- Initial broad emulator batch: 49/50 passed. The navigation check encountered a legitimately eligible test ad. It now seeds the debug package's cooldown to isolate navigation; its rerun passed. Live display is exercised separately.
- Final affected batch: 17/18 passed. The remaining PDF check expected SemiBold after the requested change to Bold; updating that explicit expectation preserved all line/pagination assertions. All seven PDF tests then passed.
- Four strict native review checks passed: English, Malayalam, Malayalam at 200% font scale, and Midnight. Both shared-content parity tests passed. All four pages of regenerated English/Malayalam sample PDFs were visually inspected.
- All three artwork rendering checks passed, including every artwork in every Bible edition without missing source text and bounded, edition-specific caching.
- Official Google sample interstitial loaded and displayed at a quiz break, remained suppressed on Daily Hope, and returned to Home after its visible close control was used. Initial test attempts incorrectly assumed Android Back/accessibility-click dismissal; the final visible-control check passed. No ad creative or production ad was clicked. This does not establish production fill rate.
- Daily Hope lifecycle checks passed, including repeated visits, leaving/backgrounding and audio focus. Initial post-warmup heap observations over 8/16/24 visits were 58,394,144 / 58,341,520 / 58,351,248 bytes.
- Extended mixed navigation checked 26 gallery/navigation/background cycles. The image cache reached 8,294,400 bytes and remained below its 8 MiB limit. Combined Java/native heap samples after each four-cycle batch were 50,223,968 / 54,368,336 / 59,782,720 / 61,728,464 / 63,935,264 / 65,979,696 bytes. Total process heap had not plateaued, so this is not proof of no memory leaks. No crash, ANR or OOM was observed in these app checks.
- After backgrounding and settling, process CPU increased 153 ms over 30,001 ms wall time (earlier run: 117 ms). This is emulator process activity, not measured physical battery consumption.

Version 41 debug was installed over the existing debug app on Samsung SM-A165F and opened successfully at September 28, 2026 12:07:58 IST. It reports `1.0.11-debug` / versionCode 41. User data was preserved. All functional tests remained on the emulator, as requested.

At the user's renewed installation request, the exact AAB-derived signed **release** APK was also installed over `com.aaronsedna.hopecards` on the Samsung at 12:21:08 IST. It reports `1.0.11` / versionCode 41 and cold-launched with status `ok` in 2,401 ms. No uninstall or data clearing was needed. Evidence: `output/quality-2026-09-28/v41/physical-install.json`.

## Exact artifact

The existing application ID, upload signing identity, Play listing, minSdk 23 and targetSdk 36 are preserved. Version 41 exceeds every previously uploaded version. SDK dependency metadata and R8 mapping remain included.

- AAB: `output/quality-2026-09-28/v41/hope-cards-1.0.11-v41.aab` (38,331,637 bytes).
- AAB SHA-256: `ef4770363ef2b7185247a7ee4469e36ce5b7be91c4d98fdc42943d50a08cf612`.
- Derived universal APK SHA-256: `5e48d9108b915ad9ef1b353284d3a98ecccd9b8cf1e00021da1768a06fb0d3bc`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.
- Bundletool validation, release manifest and signature checks passed.

## Google Play and outstanding gates

Version 40 became available in production during this audit. Version 41 was uploaded and submitted to the existing closed Alpha track for matching Play-build billing validation before promotion. At approximately 06:50 UTC on September 28, Console showed “Changes in review” for the sole Alpha rollout, while quick checks still showed up to eleven minutes remaining and said review would begin after they completed. This is not approval or public availability. This is separate from the ASO observation task; store descriptions/assets and Google Ads settings were unchanged.

Google Play parsed version 41 as supporting 20,367 devices with no support losses, API 23+, target 36 and 16 KB page support. Its parsed permission list explicitly includes `com.google.android.gms.permission.AD_ID`, matching local AAB/APK checks. The warning about a missing permission in an active artifact was acknowledged for this release without altering the truthful advertising-ID declaration. No outdated-SDK warning was shown; the existing third-party native-debug-symbol warning remains. R8 mapping is included. Console confirms R8 Full Mode and resource shrinking, while its newer “Resource Shrinking Optimized” and “Repackage Classes” indicators are off. Optional automatic protection remains None, preserving the established API-23 compatibility decision.

The active lifetime Remove Ads purchase option is available in India at INR 100.00 and 173 countries. License testing already has an owner QA account configured. End-to-end Play purchase, pending, cancel, restore and revoke checks are still being completed; neither a differently signed sideload nor debug-package tests establish those results. Production publication is not yet confirmed for version 41.

Local logs and screenshots are under `output/quality-2026-09-28/`. Test outputs include initial failures as well as successful reruns; they are not replaced by a blanket full-suite pass claim.
