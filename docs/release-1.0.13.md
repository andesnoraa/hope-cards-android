# Release 1.0.13 (45)

Status: production release 16, version **1.0.13 (45)**, was submitted on October 6, 2026. Console confirms **Changes in review**, and explicitly states “Your changes are now in review.” Final quick checks have completed. The 100% rollout in all existing 178 targeted countries is submitted, not confirmed public. Managed publishing is off, so an approved update will publish automatically. Version 44 remains the latest verified public release. See `output/release-1.0.13/play-publication.json` and the saved Console screenshot.

The quiz completion path previously called navigation immediately after AdMob `show()`, which returns before dismissal. Restarting a timed round therefore started its countdown underneath the full-screen ad. The completion flow now suspends until dismissal or a presentation failure. Missing, expired or disallowed ads continue immediately without waiting for a load. Repeated callbacks finish once; destruction cancels the pending continuation. The existing frequency, consent, paid-user and Daily Hope placement policies remain unchanged.

This bundle also contains the previously requested quiz-review and matching PDF refinements already present in the working tree. The English public release note is exactly **Bug fixes.**, with equivalent short notes in the six other existing locales. No ad/timer implementation detail is included in those public notes. Existing store descriptions, artwork, campaign settings and listing identity are unchanged.

## Local release checks

- Debug and release JVM suites each passed all 93 tests.
- Debug/release lint reports zero errors and the existing 51/52 warnings.
- The signed AAB builds locally, validates with bundletool, preserves application ID and upload certificate, and contains R8 mapping and dependency metadata.
- The unchanged quiz asset hash and production AdMob IDs are verified; screenshot mode is false.
- The [fresh SDK audit](release-1.0.13-sdk-audit.md) checks all 133 resolved coordinates. No current vulnerability finding or required compatible SDK upgrade was identified.
- Play parsed the exact bundle with no release error, no SDK warning and zero supported-device losses. The sole nonblocking warning is missing third-party native debug symbols.

## Emulator verification

A deterministic timed-quiz regression holds the ad completion callback for 45 seconds. Results remain on screen, restart is disabled, no new question or timer exists underneath it, and dismissal starts the next round with the full 00:30 allowance. Calling dismissal twice cannot start a second round. JVM checks cover asynchronous ad failure, synchronous rejection, no inventory, frequency caps, storage errors and cancellation/late dismissal.

Google's official sample interstitial integration passed on emulator-5554. The actual SDK ad loaded and displayed; the completion callback remained uncalled and the quiz destination remained current while it covered the app. Dismissal continued once. Daily Hope suppressed the interstitial. No ad creative was clicked. The first attempt was blocked by the Android notification-permission dialog; its failure log is retained. Test setup now disables reminder prompting before launch and restores settings/scheduling afterward, without changing OS permission state.

The 99-method functional suite completed with 97 passing methods and two optional stress methods skipped, with no failures (995.341 seconds). The separately run official sample interstitial also passed. The exact Play-signed version-45 APK then installed over the emulator's Play version 41 without uninstalling or clearing data. It cold-launched successfully in 2,874 ms and rendered Home, navigation with version 1.0.13, and the Filipino quiz lobby with its saved Bible language and 30-seconds-per-question description. The normal lifecycle check exercised 26 navigation cycles, kept the artwork cache at or below 8,294,400 bytes, and passed its late heap-growth budget. Its short background observation recorded 31 ms process CPU over 30,001 ms. This is an emulator process sample, not a physical battery-life measurement. Extended opt-in stress and capture-generator jobs are outside this focused bug-fix run. Exact billing purchase/restore and production ad delivery are not inferred from a debug sideload.

## Real-phone installation

The version-45 signed universal APK was downloaded from Play Console and verified against the existing Play signing certificate. It installed successfully on Samsung SM-A165F using `install -r`; no uninstall or data clearing was performed. Before installation, ADB reported that the production package was absent, so this was a new installation rather than evidence of an in-place data migration. Version 1.0.13/45 is confirmed installed and a cold launch succeeded in 674 ms. The launch screen showed the normal notification-permission prompt; that choice was left for the user. Further tests use the emulator.

Automatic protection was already off in the existing release configuration. The established API-23 compatibility decision is preserved; Google's [current protection prerequisites](https://support.google.com/googleplay/android-developer/answer/10183279?hl=en) require API 24. This artifact is Play-signed, and is not claimed to be a protected-build test.

Local evidence is retained in `output/release-1.0.13/`, including artifact hashes, test/lint logs, sample-ad screenshot, phone installation/launch evidence and Play verification records.

## Publication confirmed October 7, 2026

Read-only ASO monitoring verified production **1.0.13 (45) Available on Google Play** at **2026-10-07T05:33:01.676Z**, with **100% rollout in 178 countries/regions**. Console displays **Released on Oct 6 8:10 AM**; its display timezone was not independently exposed. Publishing overview shows last published October 6, managed publishing off and no pending changes. The India English and Malayalam public listings show October 6 updates and bug-fix release notes. This supersedes the earlier submission/in-review observation; no additional release was submitted by monitoring. [Publication evidence](aso/2026-10-07/publication.json).
