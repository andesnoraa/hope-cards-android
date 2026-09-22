# Hope Cards

Hope Cards is a native Android app written entirely in Kotlin with Jetpack Compose. The application ID remains `com.aaronsedna.hopecards`, so this codebase upgrades the existing Google Play app rather than creating a new listing.

Every app feature is free. Monetization uses:

- an anchored adaptive banner on selected browsing screens;
- a shared interstitial limit of ten completed cards/artwork visits and a minimum ten-minute cooldown;
- a non-consumable Google Play one-time product, `remove_ads_lifetime`, to remove all ads permanently.

Purchases use Google Play Billing directly. RevenueCat is not used.

## Requirements

- Android Studio with Android SDK 36 and platform tools
- JDK 17 (Android Studio's bundled JBR is supported)
- the existing, private Hope Cards upload keystore for release builds

Open the `android/` directory in Android Studio, or build from the terminal:

```bash
cd android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew app:assembleDebug
```

Install on a connected device without clearing existing app data:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Do not uninstall the existing app when validating data migration. The native app imports compatible settings, favorites, journal entries, Daily Hope state, backup metadata, and ad counters from the previous Expo/React Native installation on its first launch.

## AdMob configuration

Debug builds always use Google's official test ad units. Provide production identifiers to a release build as Gradle properties or environment variables:

```text
ADMOB_APP_ID=ca-app-pub-0000000000000000~0000000000
ADMOB_BANNER_AD_UNIT_ID=ca-app-pub-0000000000000000/0000000000
ADMOB_INTERSTITIAL_AD_UNIT_ID=ca-app-pub-0000000000000000/0000000000
```

The app requests consent through Google's User Messaging Platform before initializing Mobile Ads and exposes Ad Privacy Choices when required. Configure and publish the applicable privacy messages in AdMob before release.

Verse Art counts a visit once when returning from the artwork to its gallery, using the same counter as card draws. Sharing, cancelling the share sheet, app resume, category navigation, and leaving the app do not trigger an interstitial. At an eligible gallery transition, only an already loaded ad can show; otherwise navigation continues immediately. Pending work is cancelled when the activity is destroyed, and backgrounding or navigating elsewhere invalidates the opportunity. No additional background timer, polling, or ad cache is introduced.

## Google Play Billing

Create and activate a one-time product with product ID `remove_ads_lifetime` in the existing Google Play Console app. It must be a non-consumable purchase. The app queries ownership, handles pending purchases, acknowledges completed purchases, and restores ownership through Google Play.

The migration also recognizes an active legacy Google Play subscription as ad-free so existing Premium subscribers are not downgraded when RevenueCat is removed.

Billing availability and real purchases must be tested with a Play-installed build and a license tester. A directly installed debug APK can compile and exercise the UI but Google Play may not return product details for it.

## Notifications, performance, and layout

- The phone activity requests portrait orientation.
- Compose layouts use bounded widths, adaptive grids, scrolling content, safe drawing insets, and adaptive ad sizes.
- Android 16 and later may override fixed orientation on large-screen devices; the responsive layouts remain usable in those configurations as required by current Android guidance.
- Daily Hope reminders default to enabled at 6:00 am local time. Existing saved opt-outs and custom times are preserved. Android 13+ permission is requested once; Settings lets users change the time and open Android notification controls.
- Tapping either reminder time field selects its full number so typing replaces it. Hour/minute input validates ranges, survives recreation, and keeps Save above the keyboard.
- Daily Hope uses one inexact, idle-aware alarm instead of an exact repeating alarm. Android battery/idle policies may delay delivery. Reboots, app updates, and clock/time-zone changes reschedule it.
- The notification and Daily Hope screen share an atomic daily verse selection using the local calendar day and selected Bible translation. Notifications show a localized reference and expandable verse, the existing small icon, and no edition label or timestamp.
- Tapping a notification opens Daily Hope with the notified verse ID, even if the saved daily selection has since changed. The screen uses its normal music setting and stops audio when leaving or pausing.
- Notification routing happens before Compose renders; the launch splash remains until Daily Hope's verse is ready, so the Draw a Card screen does not flash during startup.
- The verse notification channel supports lock-screen content and heads-up banners. Android controls layout, content visibility, and interruptions. Channel migration preserves explicit importance, blocked/quiet settings, and sound/vibration preferences; on Android 8–9, existing importance is conservatively retained. Users can enable banners through Settings → Notification display.
- Routine feedback (image saved, backup results, and purchase messages) uses a compact accessible snackbar rather than a modal “A quick note” dialog.

For physical-device notification previews outside instrumentation, the debug build provides an ADB-only receiver (protected by the shell's `DUMP` permission and absent from release builds):

```sh
adb shell am broadcast -n com.aaronsedna.hopecards.debug/com.aaronsedna.hopecards.notifications.NotificationPreviewReceiver
```

It posts today's verse without changing reminder preferences. On Samsung, the phone's Brief/Detailed popup and Icons/Cards lock-screen settings control the resulting presentation.
- Music starts only after a user action and its `MediaPlayer` is released when playback stops, the screen leaves composition, or the app goes to the background.
- Verse data uses a two-translation LRU cache instead of retaining every translation in memory.
- Ad views and billing connections are disposed with their owners; the app does not retain an Activity globally.

## Signed release bundle

The private, untracked files expected by the release signing configuration are:

```text
credentials.json
credentials/android/keystore.jks
```

Before every Google Play upload:

1. Confirm `versionCode` in `android/app/build.gradle` is greater than every uploaded artifact.
2. Set the production AdMob identifiers.
3. Confirm the existing Hope Cards upload keystore is available.
4. Build locally:

```bash
cd android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew clean app:bundleRelease
```

The bundle is written to `android/app/build/outputs/bundle/release/app-release.aab`.

## Preserve the current closed test

Upload future builds to the existing `com.aaronsedna.hopecards` Play listing, signed with the existing upload key, using a higher version code. Keep the current closed-testing track and tester opt-in configuration. Do not create a new application, change the package name, replace the signing identity, deactivate the existing track, or ask testers to leave and rejoin. Updating the existing closed track does not restart the tester opt-in period; the 14-day requirement follows continuous tester participation.
