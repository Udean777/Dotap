<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="Dotap Cat Logo" />
  <h1>Dotap</h1>
  <p><strong>A lightweight, privacy-first floating dot &amp; quick-action assistant for Android.</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Platform-Android%20(minSdk%2028)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
    <img src="https://img.shields.io/badge/Network-None%20(100%25%20Offline)-0891B2?style=flat-square" alt="Offline" />
  </p>
</div>

---

## Features

- **Custom Floating Dot Styles:** Choose between **Default Dot**, **App Icon (Dotap Cat)**, or your own **Custom Image** via Android Photo Picker (no storage permission required).
- **Idle Peeking Cat Mode:** When using the App Icon style, the cat automatically peeks from the screen edge when idle and pops back out on touch.
- **Rich Gestures:** Configure **Single Tap**, **Double Tap**, **Long Press**, and **4-way Swipe/Flick** (*Up, Down, Left, Right*) to run actions.
- **Built-in Floating App Search:** Trigger `Search Apps` from any gesture or panel to open a blurred, auto-focused floating search bar and launch any installed app instantly.
- **Quick Action Panel:** Customizable 3–8 slot floating panel for Screenshot, Home, Back, Recents, Notifications, Quick Settings, Lock Screen, Flashlight, Open Dotap, and App Shortcuts.
- **In-App Theme & Language:** Switch between **System / Light / Dark** themes and **System / English / Indonesian** languages in real time without restarting.
- **Tactile Animations & Auto-Fade:** Spring-based tap/swipe feedback, edge peeking, configurable idle opacity, delay, size, haptic feedback, and automatic edge docking.
- **Privacy First:** Zero `INTERNET` permission, zero analytics, zero screen-content reading.

## Tech Stack & Architecture

- **Architecture:** MVVM + Layered Clean Architecture (`domain` → `data` / `system` ← `presentation`)
- **UI:** Jetpack Compose + Material 3 (Brand-matched Indigo & Cyan palette, Light & Dark modes)
- **Persistence:** AndroidX DataStore (Preferences)
- **DI:** Manual dependency injection via `AppContainer`
- **Quality:** Spotless (ktlint), detekt, Android Lint, JUnit + Coroutines Test + Turbine

## Build & Verify

```bash
./gradlew assembleDebug
./gradlew spotlessApply testDebugUnitTest lintDebug detekt
```

## Privacy Policy

[Privacy Policy](https://udean777.github.io/Dotap/privacy-policy.html) ([source](docs/privacy-policy.html))
