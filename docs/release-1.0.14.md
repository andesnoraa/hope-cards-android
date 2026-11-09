# Hope Cards 1.0.14 (47)

General interface resources now follow the device language and choose the bundled Malayalam font where appropriate. Quiz/certificate language remains tied to the selected Bible. Google Play Gemini manages pt, de, fr, it, ml and es; Filipino is not offered and retains existing resources. Portuguese output is also retained locally for offline/local builds. Reviewed corrections prevent “Draw a Card” from being rendered as “Buy a card.” The About feature list uses shared localized UI resources.

App ID, upload signing identity, minSdk 23, targetSdk 36 and 94 content assets are preserved. Locale splitting is disabled to retain offline selected-Bible languages. No runtime Gemini API is added. The service retains some unchanged wording; configuration does not imply every old sentence was freshly generated.

Final validation: 93 debug and 93 release JVM tests pass; four focused emulator language tests pass; debug/release lint have zero errors; local release bundle succeeds. The actual Play-signed APK has 315 surviving resources per managed language, matching placeholders, corrected controls, localized About references and unchanged content. Manual Portuguese Home/About checks pass. The emulator's original empty app-locale override is restored. This is focused translation validation, not a new claim of complete real-device, Billing or live-ad QA.

Artifact: `output/translation-final-1.0.14/hope-cards-1.0.14-v47.aab`.
SHA-256: `c444f1ab6618ac359c3817d83895a301b9fb0e3717375a258b2a1b31597b7dfd`.

Internal version 47 is available to testers. Production rollout was requested at 100% for the existing countries and is **queued pending Google's quick checks**; approval/public visibility have not been verified. See the latest [publication record](aso/2026-10-08/keyword-research/PUBLICATION.md) and [SDK audit](release-1.0.14-sdk-audit.md).

Release notes: **Bug fixes.**
