# Dotap — Play Policy & Compliance Guide

This document defines Google Play compliance guidelines for Dotap regarding the use of the Accessibility API (`AccessibilityService`), prominent disclosure requirements, user privacy, store materials, and submission declarations.

Related docs: `PRD.md`, `ARCHITECTURE.md`, `ROADMAP.md`, `AGENTS.md`.

---

## 1. Google Play Accessibility API Policy

Dotap uses Android's `AccessibilityService` API for a narrow, clearly defined purpose:
1. Drawing a floating shortcut button (`TYPE_ACCESSIBILITY_OVERLAY`).
2. Performing user-configured global system actions (`performGlobalAction`), such as taking a screenshot, navigating Home, Back, Recents, opening Notifications, or Quick Settings.

### Core Policy Guardrails:
* **No Window Content Inspection:** `canRetrieveWindowContent` is strictly set to `false`.
* **Screenshot Capability:** `canTakeScreenshot` is `true` (Android 11+) solely so `GLOBAL_ACTION_TAKE_SCREENSHOT` can run when the user triggers the Screenshot action. It is never used to capture screen content on its own; on Android 10 and older the action reports "unavailable" instead.
* **No Event Listening:** No `accessibilityEventTypes` are declared. `onAccessibilityEvent` is empty and ignores all events.
* **No Data Collection:** Dotap does NOT collect, store, transmit, or analyze any user keystrokes, personal data, or on-screen content.
* **No Network:** Dotap does NOT declare or use `android.permission.INTERNET`.

---

## 2. Prominent Disclosure & User Consent

Google Play policy requires a **Prominent In-App Disclosure** before directing the user to enable the Accessibility Service in system settings.

### Prominent Disclosure Requirements:
1. Must be displayed within the normal app flow (not buried in a Privacy Policy or Terms of Service).
2. Must explain clearly what data the service uses (none) and why it is needed (only to display the floating dot and trigger system shortcuts like screenshots).
3. Must require an explicit affirmative action from the user ("Agree & Continue" / "Setuju & Lanjutkan") before opening the system Accessibility settings.
4. If the user selects "Not Now", the app remains functional within its limited state without repeatedly harassing the user.

### In-App Disclosure Copy

#### English
> **Why Dotap Needs Accessibility Service**
>
> Dotap uses Android's AccessibilityService API solely to:
> - Display the floating shortcut dot above other applications.
> - Perform global actions you trigger, such as taking screenshots, going Home, Back, or opening Notifications.
>
> **Privacy Guarantee:**
> - Dotap **NEVER** reads, captures, or transmits your screen content, typed text, or personal information.
> - Dotap operates entirely offline with zero network permissions.

#### Indonesian
> **Mengapa Dotap Memerlukan Layanan Aksesibilitas**
>
> Dotap menggunakan AccessibilityService API Android hanya untuk:
> - Menampilkan tombol dot pintasan melayang di atas aplikasi lain.
> - Menjalankan tindakan sistem yang Anda picu, seperti mengambil tangkapan layar, kembali ke Home, Back, atau membuka Notifikasi.
>
> **Jaminan Privasi:**
> - Dotap **TIDAK PERNAH** membaca, merekam, atau mengirimkan konten layar, teks yang diketik, atau data pribadi Anda.
> - Dotap beroperasi sepenuhnya secara offline tanpa izin internet.

---

## 3. Store Listing Materials

### 3.1 App Title & Short Description
* **App Title:** Dotap — Floating Shortcut Dot
* **Short Description (English, <= 80 chars):**
  > Floating dot for fast one-handed shortcuts and screenshots. 100% private.
* **Short Description (Indonesian, <= 80 chars):**
  > Dot melayang untuk pintasan satu tangan & tangkapan layar cepat. 100% privat.

### 3.2 Full Description (English)
```text
Dotap is a lightweight, fully private floating assistant button that brings essential Android navigation and tools right under your thumb. Designed for large screens and effortless one-handed use.

KEY FEATURES:
• Floating Shortcut Dot: Always accessible above all apps, smooth edge snapping, and custom idle transparency.
• Configurable Gestures: Assign Single Tap, Double Tap, or Long Press to any action.
• Instant Screenshots: Capture screenshots with a double tap without awkward button combinations.
• Quick-Action Panel: Open a customizable 3x3 radial menu for Home, Back, Recents, Notifications, and Power Dialog.
• Customizable Appearance: Adjust dot size (32dp - 72dp), idle fade opacity, and haptic feedback.
• Quick Settings Tile: Toggle the dot on and off anytime from your notification shade.

STRICT PRIVACY GUARANTEE:
• Zero Internet Permission: Dotap does not have network access and cannot send data anywhere.
• Zero Data Collection: Dotap never reads, records, or logs your screen content, keystrokes, or personal data.
• Clean & Open: No ads, no third-party trackers, no analytics SDKs.

ACCESSIBILITY SERVICE DISCLOSURE:
Dotap uses Android's AccessibilityService API exclusively to:
1. Render the floating dot overlay above other apps (TYPE_ACCESSIBILITY_OVERLAY).
2. Perform user-initiated global system actions (Home, Back, Recents, Screenshot, Notifications, Quick Settings, Lock Screen).
Dotap does NOT inspect your screen, read text, or monitor user input.
```

### 3.3 Full Description (Indonesian)
```text
Dotap adalah tombol asisten melayang yang ringan dan sepenuhnya privat untuk navigasi satu tangan di Android.

FITUR UTAMA:
• Dot Pintasan Melayang: Selalu mudah dijangkau di atas aplikasi lain, menempel otomatis ke tepi layar, dengan transparansi idle adaptif.
• Gestur yang Dapat Diatur: Atur Ketuk Sekali, Ketuk Dua Kali, atau Tekan Lama untuk aksi apa pun.
• Tangkapan Layar Cepat: Ambil screenshot hanya dengan ketuk dua kali tanpa kombinasi tombol fisik.
• Panel Aksi Cepat: Buka menu 3x3 untuk Home, Kembali, Aplikasi Terakhir, Notifikasi, dan Menu Daya.
• Kustomisasi Tampilan: Sesuaikan ukuran dot (32dp - 72dp), transparansi saat tidak digunakan, dan getaran haptic.
• Tile Pengaturan Cepat: Aktifkan atau nonaktifkan dot dengan mudah dari panel notifikasi.

JAMINAN PRIVASI MUTLAK:
• Tanpa Izin Internet: Dotap beroperasi sepenuhnya secara offline tanpa akses internet.
• Tanpa Pengumpulan Data: Dotap tidak pernah membaca, merekam, atau mencatat isi layar atau ketikan Anda.
• Bebas Iklan & Pelacak: Tanpa iklan, tanpa analytics pihak ketiga.

PENGUNGKAPAN LAYANAN AKSESIBILITAS:
Dotap menggunakan AccessibilityService API hanya untuk:
1. Menampilkan dot melayang di atas aplikasi lain (TYPE_ACCESSIBILITY_OVERLAY).
2. Menjalankan pintasan navigasi yang Anda pilih (Kembali, Home, Aplikasi Terakhir, Screenshot, Notifikasi).
Dotap TIDAK PERNAH membaca teks di layar atau memantau ketikan Anda.
```

---

## 4. Privacy Policy Page (Ready to Host)

```markdown
# Privacy Policy for Dotap

**Last updated:** November 2026

Dotap is committed to protecting your privacy. This Privacy Policy explains our practices regarding user data.

### 1. Information Collection and Use
Dotap does NOT collect, store, transmit, or share any personal information, usage analytics, or device identifiers.
• **No Network Access:** The application does not declare the `android.permission.INTERNET` permission. It cannot transmit data over the Internet.
• **Local Settings Only:** User preferences (such as gesture assignments and dot appearance) are stored strictly on the local device using AndroidX DataStore Preferences and never leave your phone.

### 2. AccessibilityService API Usage
Dotap uses the Android `AccessibilityService` API strictly to provide its core user-facing features:
• Drawing the floating shortcut button over other applications.
• Performing user-requested system actions via `performGlobalAction` (e.g. Back, Home, Recents, Screenshot, Notifications, Quick Settings).
Dotap does NOT use the Accessibility API to:
• Read, capture, or transmit screen contents or text typed in any app.
• Listen to accessibility events from other applications.
• Automate interactions or bypass user security controls.

### 3. Third-Party Services
Dotap does not integrate any third-party SDKs, advertising networks, or analytics frameworks.

### 4. Contact Us
If you have any questions about this Privacy Policy, please contact the developer via the official repository or support channel.
```

---

## 5. Google Play Data Safety Answers

When completing the Google Play Console Data Safety questionnaire, answer as follows:

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | **N/A** (No data collected or transmitted) |
| Do you provide a way for users to request that their data be deleted? | **Yes** (Uninstalling the app or clearing app storage deletes all local preferences) |

---

## 6. Accessibility Permission Declaration Draft (Console Form)

**Question: Does your app serve as an accessibility tool to help users with disabilities?**
*Answer:*
> Yes. Dotap serves as an assistive utility designed for users with motor impairments or single-handed limitations, providing easily accessible touch shortcuts for physical button presses (Power, Volume down for screenshots) and system navigation (Home, Back, Recents, Notifications).

**Question: Explain how the Accessibility API is used and why alternative APIs cannot be used:**
*Answer:*
> Dotap uses the AccessibilityService API for two essential functions:
> 1. To render a floating assistive touch dot using `TYPE_ACCESSIBILITY_OVERLAY`, allowing the user to reach navigation shortcuts without triggering full system alert window permission traps or interfering with touch inputs outside the dot.
> 2. To invoke standard Android system global actions (`performGlobalAction`) including `GLOBAL_ACTION_BACK`, `GLOBAL_ACTION_HOME`, `GLOBAL_ACTION_RECENTS`, `GLOBAL_ACTION_NOTIFICATIONS`, `GLOBAL_ACTION_QUICK_SETTINGS`, and `GLOBAL_ACTION_TAKE_SCREENSHOT`. Android does not provide non-accessibility public APIs for third-party apps to perform these system-level navigation and screenshot actions.
>
> Dotap explicitly disables window content inspection (`canRetrieveWindowContent="false"`), declares zero accessibility event types, operates completely offline without network permissions, and displays a prominent in-app disclosure requiring user consent before opening system settings.

---

## 7. Demo Video Script (30-45 Seconds)

* **Scene 1 (0:00 - 0:08) — First Launch & Prominent Disclosure:**
  - Launch app fresh install.
  - Show Welcome screen -> tap "Get Started".
  - Show Prominent Disclosure screen with clear text explaining Accessibility usage and 0 data collection guarantee.
  - Highlight affirmative button "Agree & Continue" and tap it.
* **Scene 2 (0:08 - 0:18) — Enabling Service in Settings:**
  - System Accessibility Settings opens automatically.
  - User scrolls to "Dotap Accessibility Service" and switches it on.
  - System confirmation dialog shown; user confirms.
* **Scene 3 (0:18 - 0:28) — Floating Dot & Edge Snapping:**
  - Floating dot appears immediately on screen.
  - User opens another app (e.g. Browser/Gallery).
  - User drags the dot freely across the screen and lets go: dot snaps smoothly to the screen edge.
* **Scene 4 (0:28 - 0:38) — Actions & Quick-Action Panel:**
  - User single-taps dot: 3x3 Quick-Action Panel expands smoothly with Home, Back, Recents, Screenshot, Notifications, Quick Settings.
  - User taps "Screenshot": dot and panel hide momentarily, capture animation triggers, dot reappears.
  - User double-taps dot to perform instant system Back or Home.
* **Scene 5 (0:38 - 0:45) — Privacy Verification:**
  - Open App Info / Permission manager showing **No permissions requested** and **No internet access**.

---

## 8. Screenshot & Asset Checklist

- [x] Screenshot 1: Home Dashboard with live service status and dot toggle.
- [x] Screenshot 2: Floating Dot active above standard applications with edge snapping.
- [x] Screenshot 3: Quick-Action Panel (3x3 grid) with customizable system shortcuts.
- [x] Screenshot 4: Gesture Customization screen (Single tap, Double tap, Long press timeout sliders).
- [x] Screenshot 5: Appearance screen (Dot size slider 32dp–72dp, Idle opacity slider 15%–100%).
- [x] Screenshot 6: Prominent Disclosure screen explaining offline privacy and accessibility purpose.

---

## 9. Launch Compliance Checklist

- [x] `canRetrieveWindowContent` is `false` in `accessibility_service_config.xml`.
- [x] `accessibilityEventTypes` is empty in `accessibility_service_config.xml`.
- [x] Manifest declares NO `android.permission.INTERNET`.
- [x] Onboarding prominently displays disclosure before directing user to settings.
- [x] User consent is explicit and affirmative ("Agree & Continue").
- [x] Service status correctly reflects system enabled state and live connection.
- [x] Android 13+ restricted settings guidance available for sideloaded installs.
- [x] Store listing descriptions, privacy policy, and Play Console declarations documented.
