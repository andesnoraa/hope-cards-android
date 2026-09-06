# Native Android app

- Hope Cards is a tracked native Kotlin and Jetpack Compose project under `android/`.
- Do not regenerate it with Expo Prebuild and do not add React Native or RevenueCat.
- Keep the application ID `com.aaronsedna.hopecards` and use the existing Hope Cards upload keystore for every Google Play update.
- Increment `versionCode` for every Play release. Never create a second Play app listing for a rewrite.
- Build production Android App Bundles locally with Gradle or Android Studio. Do not use EAS Build or EAS Submit.
- Publish locally built `.aab` files through the existing Google Play Console app and its existing track.
