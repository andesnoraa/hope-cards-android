# Native Android app

- Hope Cards is a tracked native Kotlin and Jetpack Compose project under `android/`.
- Do not regenerate it with Expo Prebuild and do not add React Native or RevenueCat.
- Keep the application ID `com.aaronsedna.hopecards` and use the existing Hope Cards upload keystore for every Google Play update.
- Increment `versionCode` for every Play release. Never create a second Play app listing for a rewrite.
- Build production Android App Bundles locally with Gradle or Android Studio. Do not use EAS Build or EAS Submit.
- Publish locally built `.aab` files through the existing Google Play Console app and its existing track.
- Before each Play release, inspect `releaseRuntimeClasspath` for outdated direct and transitive SDKs. Resolve Play SDK warnings with compatible stable dependencies, keep dependency metadata enabled, and verify the resolved versions before building the bundle.
- Keep Google Play automatic protection enabled when the exact release bundle validates and the protected build passes testing. Disable it only when Play blocks that release or protected-build testing finds a real incompatibility; do not raise `minSdk` merely to retain this optional protection without an explicit compatibility decision.

# UI writing preferences

- Do not use middle dots or bullet dots as inline text separators. Use full stops between sentences, parentheses for short metadata, or separate layout elements.
- Follow the app's implemented design language for new screens and mockups. Reuse HopeTheme colors, Poppins UI typography, Noto Sans Malayalam for Malayalam, and existing component shapes. Refine hierarchy and spacing without introducing an unrelated visual style.
