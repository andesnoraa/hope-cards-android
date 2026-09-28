# Release 1.0.10 (40)

Bible Quiz now offers an optional 30-second countdown for each question. The timer is off by default and remembers the setting; sound is on by default and can be disabled. The timer pauses during answer feedback, resets for the next question, and advances automatically at expiry. Results include accumulated answer time, excluding feedback.

Question content has separate Compose item identities so a press started on an expired question cannot select an answer on the next question.

The quiz layout keeps primary actions visible. Generate certificates is a text-only button. Results have a direct Share action for the redesigned PDF report; the export dropdown and print action are removed. Reports include every question, the selected answer, the correct answer and Scripture references.

The certificate generator creates separate A4 participation PDFs for multiple names, with a date picker, automatic language matching to the selected Bible, a competition title and an optional organization. The main sharing action reads “Share certificate” for one certificate or “Share certificates” for a batch, localized in all seven languages. The sharing-message panel and Copy button have been removed at the user's request.

The PDF share payload still includes “Generated using Hope Cards App”, an install invitation and the existing Google Play URL. Single-file and multiple-file shares carry captions in the documented Android extras and ClipData. The download URL stays outside the PDF. Actual WhatsApp delivery has not been verified; receiving apps can omit a PDF caption.

The separate certificate-language selector is removed. Older saved certificate-language preferences cannot override the selected Bible. The date dialog uses short labels in all seven languages: “Date” and “OK” in English, “തീയതി” and “ശരി” in Malayalam. Calendar localization is preserved.

Daily Hope is excluded from interstitial presentation, including navigation from quiz results and delayed callbacks from other screens. Normal navigation and notification entry invalidate pending presentation requests. Sharing and certificate generation remain immediate; the existing eligible quiz replay/exit and artwork-to-gallery ad breaks retain the shared ten-minute interval. No ad is triggered merely by resuming from the share chooser, which can also indicate cancellation. These placements follow Google's guidance to use [natural breaks between tasks](https://support.google.com/admob/answer/6201350?hl=en).

Verse-image category counts are removed, and four approved backgrounds are included. The seven existing store listings describe the certificate generator, per-question timer, detailed result PDFs and verse images without promising search rankings.

## Verification

The application ID, upload key, minSdk 23, targetSdk 36, existing Play listing and production track are preserved. Version 37 was uploaded but held from submission after a real touch-gesture regression identified an answer-selection race at expiry. Version 38 added the corrected timer, Daily Hope ad guards and shorter date labels, then was superseded before submission when the user removed the certificate-language selector. Version 39 was briefly submitted and then withdrawn to include the user's final sharing-panel and label changes. The final artifact uses versionCode 40, above every previously uploaded artifact. Runtime SDK auditing and compatibility decisions are recorded in [the SDK audit](release-1.0.10-sdk-audit.md). Fragment is updated to 1.9.1; dependency metadata remains enabled.

Fresh Console inspection confirmed Play App Signing is enabled and optional automatic protection was already off, as recorded for release 1.0.7. The existing settings were not changed. Google's current [automatic-protection help page](https://support.google.com/googleplay/android-developer/answer/10183279?hl=en) lists minimum API 24, while its older installer-check page still lists API 23. This release preserves API 23 support and does not claim testing of a Play-protected artifact.

Version-40 debug and release unit suites each passed all 82 tests. Debug and release lint completed with zero errors (51 debug warnings, 52 release warnings and one hint each). Four additional warnings identify unused strings from the removed message panel. Its 133 resolved runtime artifact coordinates match the audited graph exactly. The signed bundle passed bundletool validation and identity/signature checks.

All nine focused version-40 certificate/export checks have passing results, covering automatic Bible-language selection in all seven languages, short date controls, PDF generation, single/batch sharing labels, absence of the removed panel, restoration and companion-message/Play URL payloads. The initial run passed eight checks; one completed its assertions but timed out in `ActivityScenario.close` under host memory pressure. The unchanged case passed in isolation after stopping the extra emulator and idle Gradle daemon. Both logs are retained.

The initial version-37 full emulator suite executed 94 methods: 81 passed, eight optional capture tools skipped and five checks failed. The cache/gallery checks passed in isolation; two Share assertions were corrected for merged icon semantics and passed. A stored Compose semantics wrapper was replaced with a real touch-gesture regression, which exposed the expiry race. After the production fix, all 35 affected quiz, timer and certificate UI checks passed. After the final Daily Hope guards and shorter date labels, all eight focused checks passed, covering quiz navigation, both notification entry paths, 26 Daily Hope lifecycle cycles, calendar confirmation/restoration, Malayalam localization and the held-press expiry regression.

The version-40 debug build was installed and cold-launched successfully on the paired Samsung SM-A165F. The installed package reports versionCode 40 and versionName 1.0.10-debug, updated September 28 at 10:43:36 IST. Launch returned status `ok`. All further testing uses emulators, as requested. Installation evidence is in `output/release-1.0.10/v40/physical-install.json`.

The version-38 exact AAB-derived universal APK passed offline launch and quiz smoke checks, including default sound-on/timer-off settings, the 30-second per-question countdown and automatic advance. Version 39 passed all eight certificate UI checks and an exact-artifact offline Malayalam certificate check.

The final version-40 exact AAB-derived APK passed offline launch, certificate generation and Android sharesheet checks. The ready screen has no language selector, message panel or Copy button and displays “Share certificate.” The sharesheet shows the generated PDF; cancellation returns to the generated certificate. No message was sent and no Hope Cards crash was observed. The isolated emulator was cleaned up and network settings restored. Initial host memory pressure caused a System UI stall before the successful clean restart. Evidence: `output/release-1.0.10/v40/validation-summary.json` and `release-smoke-verification.json`. These checks do not establish Play Billing, production ad delivery or actual WhatsApp-message delivery.

- Final bundle: `output/release-1.0.10/v40/hope-cards-1.0.10-v40.aab`, 38,316,508 bytes.
- SHA-256: `d4eaefd873c69d72451c8cb68fb701773902cdb6d2ab32ee8f4ddff2c0381549`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.
- R8 mapping and dependency metadata are present in the exact artifact. Evidence: `output/release-1.0.10/v40/artifact-verification.json`.
- AD_ID is present in both the final AAB manifest and its derived APK. Evidence: `output/release-1.0.10/v40/advertising-id-manifest-verification.json`.

## Publication

All seven short and full descriptions were saved in Play Console. Version 40 was uploaded to the existing production track with release notes in all seven languages and a 100% rollout in all existing targeted countries. Console reported no supported-device losses and no outdated SDK warning. Its server-side bundle details list 20,367 supported devices, 16 KB memory-page support, and `com.google.android.gms.permission.AD_ID` among the 13 permissions.

The existing advertising-ID declaration error refers to an active artifact. Acknowledgement was initially blocked by automatic approval review. After independently confirming the permission in Play Console's parsed version-40 bundle details, in addition to the local AAB/APK checks, acknowledgement succeeded. The truthful advertising-ID declaration remains unchanged; Console labels the error “ignored for this release.” The nonblocking third-party native-debug-symbol warning remains, with R8 mapping included.

Versions 37 and 38 were never sent for review. Version 39 entered review on September 28 at approximately 10:36 IST, then was withdrawn before publication to incorporate the user's final changes.

Version 40 and all 14 listing changes were submitted on September 28, 2026 at approximately 11:05 IST (05:35 UTC). Publishing overview confirmed “Changes in review,” with version 40 as the sole production rollout and the seven short/full description pairs. Google's quick checks were still running, showing up to five minutes remaining; review and public availability were not yet confirmed. Managed publishing remains off, so the update is configured to publish after Google's approval.

Concurrent work to unify the quiz answer-review screen and PDF began after this bundle was built. Those newer source edits are excluded from version 40 and its release commit. The working files were preserved for the separate task; this release retains the verified question-specific Compose item keys.
