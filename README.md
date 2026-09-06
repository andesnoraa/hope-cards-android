# Hope Cards

Hope Cards is a native Android app written entirely in Kotlin with Jetpack Compose. The application ID remains `com.aaronsedna.hopecards`, so this codebase upgrades the existing Google Play app rather than creating a new listing.

Every app feature is free. Monetization uses:

- an anchored adaptive banner on selected browsing screens;
- a frequency-capped interstitial after every fifth completed card, with a minimum ten-minute cooldown;
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

## Google Play Billing

Create and activate a one-time product with product ID `remove_ads_lifetime` in the existing Google Play Console app. It must be a non-consumable purchase. The app queries ownership, handles pending purchases, acknowledges completed purchases, and restores ownership through Google Play.

The migration also recognizes an active legacy Google Play subscription as ad-free so existing Premium subscribers are not downgraded when RevenueCat is removed.

Billing availability and real purchases must be tested with a Play-installed build and a license tester. A directly installed debug APK can compile and exercise the UI but Google Play may not return product details for it.

## Notifications, performance, and layout

- The phone activity requests portrait orientation.
- Compose layouts use bounded widths, adaptive grids, scrolling content, safe drawing insets, and adaptive ad sizes.
- Android 16 and later may override fixed orientation on large-screen devices; the responsive layouts remain usable in those configurations as required by current Android guidance.
- Daily Hope uses one inexact, idle-aware alarm instead of an exact repeating alarm.
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
