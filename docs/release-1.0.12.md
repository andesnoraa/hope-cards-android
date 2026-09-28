# Release 1.0.12: live version 42 and version 44 update

**Live status:** Play Console reports version 42 available on Google Play, released September 28 at 10:12 PM with a 100% production rollout. Version 44 is saved as production release 15, combining the banner memory fix with the requested About Us additions. Version 43 was never submitted and is now marked “Superseded by another release.” Its exact Play-signed APK is installed and launch-tested on the Samsung. The September 29 submission is accepted: Publishing overview shows “Changes in review,” with quick checks still running before Google review. Managed publishing is off, so approval will publish automatically; version 44 is not yet confirmed live.

This update adds 200 original Bible quiz questions, bringing each of the seven quiz-language banks to 400, and completes localized Bible-book/reference mappings. The first 200 entries retain their IDs, text, answer order and rotation history. No puzzle feature was added. See [quiz expansion verification](quiz/expansion-v3-verification.json).

The user selected this English release note, with equivalent translations for the other six existing Play locales: “Improved Bible reference translations and checked the question bank for duplicates.” See [all release notes](release-notes-1.0.12.txt).

## Build and SDK verification

- Version code 42 exceeds the highest existing Play artifact (41). Application ID, upload signing identity, minSdk 23, targetSdk 36 and the existing Play app/production track are preserved.
- Debug and release JVM suites each passed all 87 tests.
- Debug/release lint: no errors; 51/52 existing warnings.
- The signed release bundle builds locally. Bundletool validation, manifest and signing checks pass. The bundle contains R8 mapping and dependency metadata. [Fresh 133-coordinate SDK audit](release-1.0.12-sdk-audit.md).
- Exact quiz asset bytes match the verified source bank in both the AAB and Google Play-signed APK.
- Play parsed version 42 with no release errors, no SDK warning and no supported-device losses. The sole warning concerns unavailable third-party native debug symbols. API 23+, target 36 and 16 KB page support remain unchanged.

## Functional and physical-device checks

The isolated Android 15 tablet run completed 94 tests: 93 passed initially; one navigation test was blocked by the system notification-permission dialog. Its setup now temporarily disables reminder prompting before launching the Activity, then restores original settings and reminder scheduling. It preserves OS permission state. That test passed its targeted rerun, so all 94 selected methods were verified. Capture/generator-only tests and the separate screenshot-only gallery test were excluded. Original failure evidence remains available.

The suite covers question-bank integrity across all editions, quiz scoring/state restoration/history/timers, localized and large-text layouts, artwork rendering, repositories, backups, notifications, report/certificate export and app lifecycle. The official Google sample interstitial loaded, displayed and closed successfully; no advertisement creative was clicked.

The exact Google Play-signed version-42 APK was installed over the existing Samsung Galaxy A16 installation without uninstalling or clearing data. On-device APK SHA-256 matches the downloaded Play artifact. Version 1.0.12 / 42 launched successfully (warm launch 467 ms). The earlier upload-key-signed sideload was correctly rejected because the phone now has Google Play’s signing identity; no data was removed to work around that rejection.

Eight focused physical-device instrumentation checks also passed, including all quiz editions, scoring and restoration, localized layouts, history and artwork interaction. A cold launch after Android reclaimed the cached release process succeeded. That low-memory process exit made the physical-device background CPU sample inconclusive; it is not evidence of zero battery use.

The exact Google Play-signed version-43 APK was subsequently installed in place over version 42 on the Samsung Galaxy A16, with no uninstall or data clear. Its on-device SHA-256 is `2a32f2b45718c84202332fc85f27a909a95f07abbbb92a01a4b6c6346a6e81c2`; the Play signing certificate matches the existing app. UID, data directory and first-install time were unchanged, and the saved Malayalam preference and verse state remained visible. Private app data was not directly inspected. Cold launch succeeded in 448 ms (467 ms wait time). Home, the drawer, the Malayalam quiz lobby and Daily Hope rendered; no quiz was started. The focused log check found no new crash, ANR, native crash or low-memory exit.

Version-43 gallery navigation and the final Home state were not verified: unexpected screen/state changes suggested concurrent interaction, so phone input stopped. This was a focused installation and smoke check, with no ad, battery or memory stress on the Play-signed artifact. Evidence is retained in `output/release-1.0.12/v43/phone-play/summary.json`.

## Extended memory gate

The opt-in lifecycle stress test recreates the Activity 70 times while exercising artwork, Daily Hope audio, quizzes and primary navigation. It uses weak references and repeated collection to detect destroyed Activities that remain strongly reachable, plus late-run Java/native heap growth budgets.

- With ads disabled, the test passed with zero retained destroyed Activities. Late heap growth was 0.49 MiB, with a fitted trend of 18.6 KiB per cycle. The artwork cache remained bounded at 8,294,400 bytes. Background CPU time was 118 ms over 30,006 ms on the emulator; this is a short idle sample, not a battery-life guarantee.
- With official sample ads enabled, version 42 failed: 69 destroyed Activities remained reachable, with 43.78 MiB late heap growth and a 1.46 MiB-per-cycle trend. Independent heap analysis traced strong references through Google Ads banner WebView/active-view tracking and window contexts. The bitmap cache was bounded and did not explain the growth.
- The first version-43 candidate adds explicit, idempotent banner teardown: detach from the parent, clear callbacks, destroy and release the held view. This alone still retains destroyed Activities in the same stress test.
- Constructing the banner with application context removes the SDK's Activity/window references; adaptive sizing still uses the current screen configuration. Initial loaded-banner tests also exposed two test-harness references: a pending Compose test-clock continuation and a local weak-reference referent on the instrumentation stack. Draining the test clock and counting referents in a separate helper corrected those measurement artifacts.
- Application context alone is insufficient. In a rapid-navigation run, all 70 detached banners survived after `destroy()` raced initial loads. SDK 25.4 bytecode and heaps showed that requests could complete afterward and allocate new SDK WebViews. The deferred-cleanup candidate waits for the terminal load callback, pauses and detaches immediately, and releases app ownership after a 60-second deadline if the callback is missing. After that deadline it holds only a weak reference; a late result can still perform the first and only `destroy()`. An in-flight automatic retry uses the same cleanup path.
- The deferred normal-load path released all 70 tracked banners and all 70 old Activities on the tablet. Whole-heap inspection found zero AdViews or destroyed Activities, with only three SDK WebViews. Raw late heap growth still exceeded the investigation budgets (10.77 MiB, approximately 367 KiB/cycle). Background CPU was 104 ms over 30,001 ms.
- On the Samsung, 25 distinct official sample banners loaded and rendered with the expected 1080 × 338 pixel dimensions. The 24 earlier banners and the destroyed host collected; only the latest detached application-context banner remained in an SDK native view map. Raw late heap growth was 1.53 MiB with a 166.6 KiB/cycle trend, above the 128 KiB investigation limit.
- Heap analysis attributes 13,520,956 bytes on the tablet and 4,669,914 bytes on the phone to test-mode Ad Inspector request history. Runtime limits and published SDK bytecode confirm a 1,000-record limit and a response-log threshold of 20,971,520 characters, not a blanket cap on total SDK memory. Normal non-test, non-debug-linked devices skip this history. Google [documents increased test-device memory usage](https://developers.google.com/admob/android/ad-inspector). The tablet also had stale network connections awaiting cleanup.
- A paired 25-load tablet run captured baseline and endpoint heaps after more than 310 seconds idle. Both network pools emptied. Inspector's measured Java ownership growth accounts for 94.1% of the settled Java increase; the remaining combined Java/native attribution estimate is 612,218 bytes over 20 measured loads (29.9 KiB/load). After closing the host, the whole heap contains zero MainActivities and only the latest banner. The original 221 KiB/cycle raw slope failure is preserved. This resolves the original accumulating-host/banner investigation with a documented test-diagnostics exception; it does not turn the raw test into a pass or guarantee every SDK path. [Full memory audit, exact APKs and coverage limits](release-1.0.12-memory-audit.md).

The final version-43 source passed both 87-test JVM suites and lint with no errors (51 debug and 52 release warnings). Its focused Samsung debug test passed: three distinct sample banners loaded, rendered, resumed, detached and survived navigation/recreation correctly; no earlier banners or destroyed hosts remained. The 60-second late-result and server-driven retry paths were source-reviewed but not deterministically exercised. The separate Play-signed installation and smoke check is complete within the coverage limits above; it does not extend the debug memory evidence to an exact-release stress result. No production ad creative was clicked and no private SDK flags were changed to suppress diagnostic memory.

## Production advertising

AdMob was checked for the exact package com.aaronsedna.hopecards. It reports Verified, Ready and ad serving enabled, with no current Policy Center serving restrictions and verified app-ads.txt. The production app ID and both active ad-unit IDs match the final Play-signed release DEX/manifest. Screenshot mode is false.

September 28 app-level reporting showed 24 requests, 24 matched requests, 7 impressions and approximately US$0.03 estimated revenue: interstitial 22 matched requests / 5 impressions / US$0.02, banner 2 matched requests / 2 impressions / US$0.01. These are app-wide historical/current figures, not version-42-only results or guaranteed final earnings. [AdMob overview](https://admob.google.com/v2/apps/7640322410/overview).

## Exact artifacts and publication

- Local AAB: `output/release-1.0.12/hope-cards-1.0.12-v42.aab`, 38,416,865 bytes.
- AAB SHA-256: `89625f5a0da0413ef08fe0ddbf45baa75108556ab39f90fb28f0464c06140f81`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.
- Google Play artifact ID: `4860235082760759427`.
- Play-signed universal APK SHA-256: `b967b07e3360b07041b1ef06fa5759ccce24d82e6a7340806eb9d3d7709a8067`.
- Play signing certificate SHA-256: `4d3737670369ab894229be2fafddaac6d702f011a2b57fb550037587da197a6a`.

Production release 13 now reports **Available on Google Play**, released September 28 at 10:12 PM. It contains version 42 and the approved seven localized release notes, with a 100% rollout available to 18 users in all 178 targeted countries. At the verification instant, the version adoption metric still showed 0.00%; this is delayed reporting, not proof that nobody has installed it. Managed publishing remains off; automatic protection remains None under the existing API-23 compatibility decision. No listing, permission, pricing, ad-unit or consent setting was changed during these checks.

Version 43 is saved separately as production draft release 14, retaining the same seven approved notes. Its locally built AAB is `output/release-1.0.12/v43/hope-cards-1.0.12-v43.aab`, 38,418,461 bytes, SHA-256 `caf6512e7b98763450c22e4a0e33b3a20b91b81161117301c980107882bf0d94`. Bundle validation, upload certificate, application ID, API levels, production AdMob IDs, quiz asset hash, R8 mapping and dependency metadata all pass verification. Its exact Play-signed APK passed the limited phone checks above. This draft was never submitted and Play now marks it as superseded by version 44.

## Version 44 final update

About → Features now includes “Image verses”, the existing localized “Bible Quiz” label and “And much more” in all seven localized resource variants. All ten previous entries remain, using the existing layout and styling. This copy-only change and the version-code increment follow the final version-43 source; the tested banner implementation is byte-for-byte unchanged.

Both JVM suites again passed all 87 tests each. Debug/release lint report zero errors and 51/52 warnings. All 133 resolved release SDK coordinates match the audited graph. Bundletool, signing, application identity, API levels, production ad IDs, quiz asset hash, mapping and dependency metadata checks pass. Play reports no errors or SDK warnings, no supported-device losses, 20,367 supported devices and 16 KB page support. Its sole warning remains missing native symbols from third-party code.

- Version 44 AAB: `output/release-1.0.12/v44/hope-cards-1.0.12-v44.aab`, 38,418,760 bytes.
- AAB SHA-256: `9f451a8b41c6ef67a08aef508767ded8da228f9fcb3ec356910043d8dc9a3940`.
- Google Play artifact ID: `4860235137695339235`.
- Play-signed universal APK: `output/release-1.0.12/v44/play-signed-universal.apk`, 34,427,504 bytes.
- Play APK SHA-256: `1cf4ca6cedc6285d5f291058451b8cc0d52c3be3ed51a44157c44dd9cfc9c7a1`.
- Upload and Play certificates match the identities recorded above.

Production release 15 retains all seven approved release notes, a 100% rollout and all existing targeted countries. Its exact Play-signed APK is installed on the Samsung in place: on-device hash matches, app ID/data directory/original first-install timestamp are preserved, and a cold Home launch succeeded in 604 ms. The About-screen tap was followed by an unexpected Verse Gallery detail screen, so input stopped; the three new About entries were not visually verified on the phone. No concurrent actor is assumed. The resource-only additions compiled in all seven variants, while the earlier functional navigation suite remains the broader coverage. Captured launch/navigation logs contain no app crash, ANR or OOM. On September 29 the release was sent for review; Publishing overview now lists the single version-44 full-rollout change under “Changes in review,” with automated quick checks still running. Managed publishing is off. Version 42 remains the last verified live release until version 44 is approved.

Local evidence and original test outputs are retained in `output/release-1.0.12/`.
