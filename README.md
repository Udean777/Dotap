<div align="center">

  <h1>🔘 Dotap</h1>
  <p><strong>The floating assistive dot for Android that puts customizable navigation, screenshots, and system actions right under your thumb.</strong></p>

  <p>
    <em>One tap, double tap, or long press—navigate with ease, take instant screenshots, and operate your device effortlessly with one hand.</em>
  </p>

  <p>
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"></a>
    <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=android&logoColor=white" alt="Compose M3"></a>
    <a href="#"><img src="https://img.shields.io/badge/Android-9.0%2B_(API_28%2B)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android minSdk 28"></a>
    <a href="#"><img src="https://img.shields.io/badge/Zero_Network-100%25_Offline-00C853?style=flat-square" alt="Zero Network"></a>
    <a href="#"><img src="https://img.shields.io/badge/Size-%3C_3.5_MB-00B0FF?style=flat-square" alt="App Size"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-FF6F00?style=flat-square" alt="License"></a>
  </p>

</div>

---

## 💡 Why Dotap?

Modern smartphones are tall and difficult to navigate with one hand. Reaching for the top of the screen to pull down notifications, stretching for the power button to lock your phone, or contorting your fingers to capture a screenshot shouldn't be a daily struggle.

**Dotap** brings an elegant, floating assistive touch button to any Android screen:
- **Zero Network Permissions**: Dotap does not have the `INTERNET` permission in its manifest. Your data, actions, and usage patterns never leave your phone.
- **Privacy by Design**: The accessibility service operates purely as a hardware-level trigger. It **never** reads screen content, window trees, or text from other applications.
- **Featherlight Footprint**: Optimized with R8 down to just **~3.5 MB APK** and **< 20 MB** idle runtime memory, ensuring zero impact on battery life and frame rates.

---

## ✨ Features

### 🎯 Floating Assistive Dot
- **Customizable Gestures**: Map independent actions to:
  - **Single Tap**: Instant response or configurable timeout.
  - **Double Tap**: Fast dual-tap detection (default: **Take Screenshot**).
  - **Long Press**: Press-and-hold trigger with vibration confirmation.
- **Edge Snapping**: Naturally glides and sticks to the nearest screen edge with smooth spring physics when released.
- **Idle Fading & Opacity**: Choose your preferred active visibility and let the dot fade out automatically (down to 10% opacity) when not in use.
- **Custom Sizing**: Choose between **Small (36 dp)**, **Medium (44 dp)**, and **Large (56 dp)** to match your ergonomics.
- **Haptic Feedback**: Subtle vibration cues on gesture triggers and edge snaps.

---

### 🎛️ Quick-Action Panel
Expand a sleek, floating Material 3 action panel directly from the dot to access vital system functions with a single hand:

| Action | Description |
|:---|:---|
| 📸 **Screenshot** | Capture the current screen instantly without pressing physical buttons. |
| 🔒 **Lock Screen** | Lock your device immediately to reduce power button wear. |
| 🔔 **Notifications** | Pull down the notification drawer effortlessly. |
| ⚙️ **Quick Settings** | Expand the quick settings panel with one tap. |
| 📱 **Recent Apps** | Open the multitasking app switcher. |
| 🏠 **Home** | Navigate back to your launcher home screen. |
| ◀️ **Back** | Perform system back navigation. |
| ⚡ **Power Dialog** | Open the system restart/power menu. |
| 🪟 **Split Screen** | Toggle multi-window split-screen multitasking. |

---

### ⚡ Quick Settings Tile
- Toggle the floating dot on or off at any moment directly from your Android notification shade without opening the app.
- Live status reflection: automatically indicates whether the service is running, paused, or requiring permission.

---

### 🔋 OEM Battery Protection Guidance
Different phone manufacturers apply aggressive background app killers that can unexpectedly pause accessibility overlays. Dotap includes built-in, device-tailored guidance for:
- **Xiaomi / Redmi / POCO** (MIUI & HyperOS battery saver & autostart)
- **Samsung Galaxy** (One UI background usage limits & "Never sleeping apps")
- **OPPO / Realme** (ColorOS app startup management)
- **Vivo / iQOO** (FuntouchOS & OriginOS background power consumption)
- **Huawei** (EMUI app launch management)
- Direct deep links to system battery settings and [dontkillmyapp.com](https://dontkillmyapp.com).

---

### ♿ Accessibility & Inclusivity First
- **TalkBack Optimized**: Full content descriptions, semantic action roles, and accessible long-press triggers.
- **Dynamic Color**: Seamlessly adapts to your Android 12+ Material You wallpaper theme.
- **Display Scaling Safe**: All screens and dialogs adapt cleanly to large font sizes and high-DPI zoom levels without clipping.
- **Bilingual**: 100% native translation parity between **English** and **Bahasa Indonesia**.

---

## 🛠️ For Developers

Interested in cloning, modifying, or contributing to Dotap? 

Detailed technical specifications, architecture blueprints, build instructions, and testing workflows are documented separately:

- 📖 **[Developer Setup & Contribution Guide](docs/DEVELOPMENT.md)** — Prerequisites, Gradle commands, local keystore setup, and PR checklist.
- 🏛️ **[Architecture Blueprint](docs/ARCHITECTURE.md)** — MVVM with Layered Clean Architecture, Port & Adapter patterns, and clean code standards.
- 🛡️ **[Play Store Compliance & Permissions](docs/COMPLIANCE.md)** — Accessibility disclosures, permission policies, and safety justifications.

---

## 📄 License

Dotap is open-source software licensed under the **[Apache License 2.0](LICENSE)**.
