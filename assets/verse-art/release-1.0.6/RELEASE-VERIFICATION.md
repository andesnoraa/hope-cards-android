# Hope Cards 1.0.6 (33) — 22 September 2026

Application ID: com.aaronsedna.hopecards. Highest uploaded version observed was 32. Built locally with the same upload certificate as v32 (SHA-256 F1:49:33:3F:75:8D:3A:82:5D:EC:39:D9:0C:92:8D:91:20:FA:AD:8D:C5:8F:41:DB:2F:33:57:8D:7A:4F:2F:82). minSdk 23 and targetSdk 36 retained.

## Changes

Adds Verse Art to the navigation drawer: 180 offline images in six categories of 30, favorites, image saving and sharing. All 180 images are now rendered from checked text with horizontal text blocks. Removed the former -4-degree rotation. The six older generated designs were also rendered from exact source text. Distinct gallery icon; cached shared exports refresh when artwork changes.

Compared all 180 verse wordings and references against the publisher's World English Bible Updated HTML archive (https://ebible.org/engwebu/engwebu_html.zip). Only Psalm 46:10 differed: corrected “in the earth” to “on the earth” in artwork and the English reading dataset. Publisher snapshot and archive hash are recorded in verified-web-verses.json. Case, punctuation and spacing are normalized for comparison; words are not paraphrased. The checked authoring text is laid out directly with Android Canvas. All six contact sheets were visually reviewed for level text and overflow.

## Verification

- 29 debug unit tests and 29 release unit tests passed.
- Release lint: zero errors, 18 existing warnings.
- Five Verse Art instrumentation tests passed on Samsung SM_A165F, including all image pairs and Bible text, navigation, favorite persistence, saving, sharing and stale-export replacement.
- Updated Samsung debug app installed in place, preserving user data.
- Signed AAB build and bundletool validation passed.
- Generated universal APK from this exact AAB using existing upload signing; installed in place on emulator-5554. Verified navigation to Hope, a previously tilted Isaiah 43:19 image, and successful saving. Screenshot: release-straight-text.png.
- All 360 image files in the AAB match the visually reviewed source files byte-for-byte.
- AAB manifest confirms AD_ID permission; dependency metadata and R8 mapping are present.

## Dependency review

Reviewed all 133 resolved release modules, including transitive dependencies, against repository version metadata and OSV vulnerability data. No known vulnerabilities returned by OSV. Newer library versions were recorded in module-version-review.json; no blanket framework upgrade was attempted during this release.

Mobile Ads 25.5.0 was evaluated, but its manifest requires API 24. Retained compatible 25.4.0 to support existing Android 6 users. Guava 33.6.0-android similarly preserves API 23 compatibility. Billing 9.1.0, UMP 4.0.0 and App Update 2.1.0 retained. Gradle dependencyInsight verifies Fragment 1.9.0, WorkManager 2.11.2 and Guava 33.6.0-android instead of old transitive requests. No SDK warning was reported by Play for build 33. Metadata remains enabled.

## Play validation

Uploaded v33 to the existing Production track. Device support is unchanged: 13,353 phones, 6,881 tablets, 8 TVs, 26 cars, 72 Chromebooks and 1 Android XR device. All existing 177 countries targeted, 100% rollout. Release notes supplied in all seven listing languages.

Play repeated the existing Advertising ID warning about one active artifact. Verified that this exact v33 AAB declares com.google.android.gms.permission.AD_ID, then acknowledged the legacy-artifact warning without changing the truthful Advertising ID declaration. Non-blocking warnings: larger download due to offline images, and unavailable native debug symbols. R8 mapping is embedded and archived. Existing automatic protection setting was already off and was not changed; Google Play app signing remains enabled.

Submission confirmed on 22 September 2026. Publishing overview shows “Changes in review” for Production 33 (1.0.6), “Start full rollout”. Google quick checks are still running (up to 10 minutes shown); changes proceed to review after successful checks. Managed publishing is off, so publication is automatic after approval.

Console: https://play.google.com/console/u/0/developers/7885801294965165245/app/4976302422419978923/publishing
