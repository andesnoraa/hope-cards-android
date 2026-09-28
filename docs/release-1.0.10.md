# Release 1.0.10 (37)

Bible Quiz now offers an optional 30-second countdown for each question. The timer is off by default and remembers the setting; sound is on by default and can be disabled. The timer pauses during answer feedback, resets for the next question, and advances automatically at expiry. Results include accumulated answer time, excluding feedback.

The quiz layout keeps primary actions visible. Generate certificates is a text-only button. Results have a direct Share action for the redesigned PDF report; the export dropdown and print action are removed. Reports include every question, the selected answer, the correct answer and Scripture references.

The certificate generator creates separate A4 participation PDFs for multiple names, with a date picker, seven certificate languages, a competition title and an optional organization. Its PDF share payload includes “Generated using Hope Cards App”, an install invitation and the existing Google Play URL. Single-file and multiple-file shares carry captions in the documented Android extras and ClipData. A visible message preview and explicit Copy message action cover receiving apps that omit a PDF caption, including the user's reported WhatsApp flow. The download URL stays outside the PDF. Actual WhatsApp delivery has not been verified.

Verse-image category counts are removed, and four approved backgrounds are included. The seven existing store listings describe the certificate generator, per-question timer, detailed result PDFs and verse images without promising search rankings.

## Verification

The application ID, upload key, minSdk 23, targetSdk 36, existing Play listing and production track are preserved. Play Console's highest uploaded artifact was 36 before this release. Runtime SDK auditing and compatibility decisions are recorded in [the SDK audit](release-1.0.10-sdk-audit.md). Fragment is updated to 1.9.1; dependency metadata remains enabled.

Fresh Console inspection confirmed Play App Signing is enabled and optional automatic protection was already off, as recorded for release 1.0.7. The existing settings were not changed. Google's current [automatic-protection help page](https://support.google.com/googleplay/android-developer/answer/10183279?hl=en) lists minimum API 24, while its older installer-check page still lists API 23. This release preserves API 23 support and does not claim testing of a Play-protected artifact.

The final debug and release unit runs each passed all 80 tests. Debug and release lint completed with zero errors. The locally signed release bundle passed bundletool validation and identity checks. Final emulator test and device-install results are pending.

The exact AAB-derived universal APK was installed on an isolated emulator data image, preserving existing emulator installations. Offline launch and quiz operation passed. Observed the default sound-on/timer-off settings, 30-second per-question countdown and automatic advance, text-only certificate entry, generation of a sample certificate, the complete share-message preview and Copy message confirmation. Temporary network settings were restored. No app crash was observed. This does not establish Play Billing, production ad delivery or actual WhatsApp-message delivery.

- Bundle: `output/release-1.0.10/hope-cards-1.0.10-v37.aab`, 38,338,189 bytes.
- SHA-256: `10259ea2a73d0f1a1d192835921253541f777bd616bbc9533f326e2482c5c17f`.
- Upload certificate SHA-256: `f149333f758d3a825dec39d90c928d9120faad8dc58f41db2f33578d7a4f2f82`.
- AD_ID permission, R8 mapping and dependency metadata are present in the exact artifact.

## Publication

All seven short and full descriptions were saved in Play Console and confirmed as changes ready to send for review. Version 37 was uploaded to the existing production track with release notes in all seven languages. Console reported no supported-device losses and no outdated SDK warning. The existing advertising-ID declaration error for an active legacy artifact was acknowledged for this release after checking AD_ID in the exact version-37 AAB and APK; the truthful declaration remains unchanged. The nonblocking third-party native-debug-symbol warning remains.

The release is saved at 100% rollout across the existing countries, pending final emulator checks and submission for review.
