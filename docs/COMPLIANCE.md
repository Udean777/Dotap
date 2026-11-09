# Dotap — Play Policy & Compliance Guide

This document defines Google Play compliance guidelines for Dotap regarding the use of the Accessibility API (`AccessibilityService`), prominent disclosure requirements, and user privacy.

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
4. If the user selects "Not Now", the app must remain functional within its limited state without repeatedly harassing the user.

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

## 3. Google Play Data Safety Declaration

* **Data collection:** No data collected.
* **Data sharing:** No data shared with third parties.
* **Security practices:** All settings are stored locally on the device via DataStore Preferences.
