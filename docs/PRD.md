# Dotap — Product Requirements Document

| | |
|---|---|
| Version | 0.1 (draft) |
| Platform | Android only |
| Stack | Kotlin (native), Jetpack Compose for in-app UI |
| Architecture | MVVM with layered (Clean) architecture: domain / data / presentation / system. Clean code enforced by Spotless (ktlint) and detekt |
| Application ID | `com.ssajudn.dotap` |
| Tagline | A floating dot. Double tap, screenshot. |

Related docs: `ARCHITECTURE.md`, `ROADMAP.md`, `COMPLIANCE.md`, `AGENTS.md`.

---

## 1. Overview

Dotap is a small, fast, private Android utility. It shows a floating dot above every app. The user taps the dot to trigger an action they chose. The default setup is: **double tap = screenshot**, **single tap = open a quick-action panel**.

It is inspired by iPhone AssistiveTouch (single tap, double tap, long press custom actions, an icon menu, idle opacity), rebuilt for Android with a narrow, clearly explained scope.

## 2. Problem

- Taking a screenshot with hardware buttons is awkward on big phones and one-handed use.
- Physical buttons wear out; some users want a software alternative.
- Existing Android "assistive touch" apps are ad-heavy, bloated, and ask for broad permissions.

## 3. Goals

1. Double-tap the dot and get a system screenshot within one double-tap timeout plus about 300 ms.
2. A clean, tiny, ad-free app that works offline and collects no data.
3. Every gesture on the dot is configurable (single tap, double tap, long press).
4. A panel with common system actions (Home, Back, Recents, Notifications, Quick Settings, Lock, Screenshot).
5. Passes Google Play review as a non-accessibility-tool app that uses the Accessibility Service API for a narrow purpose.

## 4. Non-goals

- Not an automation tool, assistant, or screen reader. The service must never read screen content.
- No iOS version.
- No pointer/mouse emulation, dwell control, hot corners, or sound-triggered actions (AssistiveTouch features deliberately skipped).
- No cloud, accounts, analytics, ads, or network access in v1.
- No screen recording in v1.

## 5. Target users

- People with large phones who want one-handed screenshots.
- People with worn or broken hardware buttons.
- Power users who want fast shortcuts (Home, Back, Recents, Lock) on one dot.
- Motor-difficulty users may benefit, but Dotap does not market itself as an accessibility tool.

## 6. Release scope

| Release | Scope |
|---|---|
| **v0.1 (MVP)** | Floating dot, drag and snap, three configurable gestures, system actions, screenshot with dot hidden, settings screen, onboarding with disclosure |
| **v0.2** | Quick-action panel with default slots, idle opacity, size setting, onboarding and compliance screens |
| **v1.0** | Quick Settings tile, customizable panel slots, launch-app action, flashlight action, OEM battery guidance, Indonesian + English polish, Play Store release |
| **Later** | Own capture with save/share (`takeScreenshot`, API 30+), simple custom gestures (swipe, scroll), more actions |

## 7. Functional requirements

Priority: **P0** = MVP, **P1** = v0.2, **P2** = v1.0, **P3** = later.

### 7.1 Floating dot

| ID | Requirement | Priority |
|---|---|---|
| FR-1 | Show a circular dot overlay above all apps while the service is connected and the dot is enabled. | P0 |
| FR-2 | Dot is draggable. On release it snaps to the nearest horizontal screen edge. Position is persisted as edge + vertical ratio and re-clamped on rotation and display changes. | P0 |
| FR-3 | Dot size is configurable (32–72 dp, default 48 dp). | P1 |
| FR-4 | Idle opacity: after an idle delay (default 3 s) the dot fades to a configurable opacity (15–100%, default 40%). It returns to full opacity on touch. | P1 |
| FR-5 | Dot must not trigger the system back gesture by accident near edges (use gesture exclusion rects). | P0 |

**Acceptance (FR-2):** drag the dot anywhere, release, it animates to the nearest side edge; after rotating the device it is still fully on screen and on the same side.

### 7.2 Gestures and actions

| ID | Requirement | Priority |
|---|---|---|
| FR-10 | Dot supports single tap, double tap, and long press. Each maps to one action. Defaults: single = Open panel (until the panel ships: Home), double = Screenshot, long press = None. | P0 |
| FR-11 | Double-tap timeout is configurable (150–600 ms, default 300 ms). Long-press duration is configurable (300–1500 ms, default 500 ms). | P0 |
| FR-12 | If double tap is assigned, a single tap fires only after the double-tap timeout expires with no second tap. If double tap is None, single tap fires immediately. | P0 |
| FR-13 | Dragging (movement beyond touch slop) cancels any pending tap or long press. | P0 |
| FR-14 | Haptic feedback on action fire (on by default, can be disabled). | P0 |

**Available actions**

| Action | Mechanism | Min API | Priority |
|---|---|---|---|
| Screenshot | `GLOBAL_ACTION_TAKE_SCREENSHOT` | 30 (`canTakeScreenshot`) | P0 |
| Home | `GLOBAL_ACTION_HOME` | 16 | P0 |
| Back | `GLOBAL_ACTION_BACK` | 16 | P0 |
| Recents | `GLOBAL_ACTION_RECENTS` | 16 | P0 |
| Notifications | `GLOBAL_ACTION_NOTIFICATIONS` | 16 | P0 |
| Quick Settings | `GLOBAL_ACTION_QUICK_SETTINGS` | 17 | P0 |
| Power dialog | `GLOBAL_ACTION_POWER_DIALOG` | 21 | P0 |
| Lock screen | `GLOBAL_ACTION_LOCK_SCREEN` | 28 | P0 |
| Open panel | internal | — | P1 |
| None | — | — | P0 |
| Launch app | `PackageManager` launch intent | — | P2 |
| Flashlight | `CameraManager.setTorchMode` | 23 | P2 |

### 7.3 Screenshot behavior

| ID | Requirement | Priority |
|---|---|---|
| FR-20 | Before triggering, hide the dot (alpha 0, not touchable). Wait a short delay (default 150 ms, tunable constant). Call the screenshot global action. Restore the dot afterwards (about 800 ms later). | P0 |
| FR-21 | If the global action returns `false`, restore the dot immediately and show a brief overlay message ("Screenshot not available on this device"). | P0 |
| FR-22 | The dot must not appear in the resulting screenshot on tested devices. | P0 |
| FR-23 | Own capture (`takeScreenshot`, API 30+): save to `Pictures/Dotap` via MediaStore, show a preview chip with Share and Delete. Requires re-doing the Play disclosure. | P3 |

Note: apps with `FLAG_SECURE` (banking, DRM video) produce black or blocked screenshots. This is expected and not fixable. Document it in the UI help text.

Note: screenshots need Android 11+ (API 30) and the `canTakeScreenshot` service capability. On Android 10 and older the action reports unavailable.

### 7.4 Quick-action panel

| ID | Requirement | Priority |
|---|---|---|
| FR-30 | Tapping the dot (when single tap = Open panel) opens a rounded panel of 3–8 icons near the dot, AssistiveTouch-style (3×3 grid with the centre cell as Close). Tap outside or Back dismisses it. | P1 |
| FR-31 | Default slots: Home, Back, Recents, Screenshot, Notifications, Quick Settings. | P1 |
| FR-32 | User can reorder, add, and remove slots (3–8). | P2 |
| FR-33 | Selecting a slot runs its action and closes the panel. Screenshot from the panel hides the panel and dot first. | P1 |

### 7.5 Onboarding and settings

| ID | Requirement | Priority |
|---|---|---|
| FR-40 | Onboarding explains what Dotap does, shows a **prominent disclosure** screen with an explicit "Agree" action, then opens Accessibility settings. See `COMPLIANCE.md`. | P0 |
| FR-41 | Detect whether the service is enabled (source of truth: system enabled-services list, not only in-process state) and show clear status on the home screen. | P0 |
| FR-42 | Home screen has a toggle to show or hide the dot without disabling the service. | P0 |
| FR-43 | Settings screen (Compose): actions, timeouts, size, idle opacity, haptics, snap-to-edge. Changes apply live to the dot. | P0 (basic), P1 (all) |
| FR-44 | Handle Android 13+ "Restricted setting" for sideloaded installs: show guidance to allow restricted settings from App info. | P1 |
| FR-45 | Quick Settings tile toggles dot visibility. | P2 |
| FR-46 | OEM battery guidance screen (Xiaomi, Oppo, Vivo, Samsung, etc.) linking to a vendor-specific help page. | P2 |
| FR-47 | Localization: English and Indonesian. | P0 (strings externalized), P2 (review) |

## 8. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-1 | `minSdk` 28 (needed for screenshot and lock global actions). `targetSdk` and `compileSdk` = latest stable at project creation. |
| NFR-2 | No `INTERNET` permission. No analytics, ads, or crash SDKs that use the network. |
| NFR-3 | The accessibility service must not request window content or listen to accessibility events. |
| NFR-4 | Dot touch response feels instant (no dropped frames while dragging on a mid-range device). |
| NFR-5 | Idle CPU and battery impact negligible: no polling, no wake locks, no foreground service. |
| NFR-6 | Release APK/AAB under 8 MB (R8 enabled). |
| NFR-7 | Survives configuration changes, rotation, split-screen, and process death without leaving a stale overlay. |
| NFR-8 | Dot and panel are usable with TalkBack: content descriptions on all controls. |
| NFR-9 | Maintainability: MVVM per `ARCHITECTURE.md`; the `domain` layer has no Android dependencies; `presentation` never depends on `data` or `system`; one responsibility per class; no magic numbers. |
| NFR-10 | Testability: all domain logic (gesture engine, use cases, models) and all ViewModels are unit-tested on the JVM with fakes; aim for 80% or more line coverage in those layers. |
| NFR-11 | Code quality gates: `spotlessCheck`, `detekt`, `lintDebug`, and unit tests pass in CI before merge. |

## 9. UX flow

1. **First launch:** short intro → disclosure screen (Agree / Not now) → button to open Accessibility settings.
2. **User enables the service** in system settings and returns.
3. **Home screen:** status chip (On/Off), dot visibility toggle, shortcut cards: Gestures, Appearance, Help.
4. **Dot on screen:** tap/double tap/long press perform the configured actions.
5. **Settings:** list rows with dropdowns for actions; sliders for timeouts, size, opacity.

Design direction: minimal Material 3, dark/light, dot is a soft translucent circle with a subtle inner ring. Exact visual identity is under the Dibuilt brand and is not specified here.

## 10. Permissions and privacy

- Declared: `BIND_ACCESSIBILITY_SERVICE` (on the service). Dot uses `TYPE_ACCESSIBILITY_OVERLAY`, so **no** `SYSTEM_ALERT_WINDOW` is needed.
- Not requested: `INTERNET`, storage, location, contacts, microphone, camera (flashlight in v1.0 needs no permission).
- Data: Dotap collects, stores, and transmits no personal data. Settings stay on the device.
- Data Safety form: "No data collected, no data shared".

## 11. Success metrics

For a small independent app, use Play Console vitals and store signals only (no in-app analytics):

- Crash-free users above 99.5%.
- ANR rate below the Play bad-behavior threshold.
- Install-to-enabled-service conversion (estimated from reviews and retention in Play Console).
- Average rating of 4.3 or higher after the first 100 ratings.

## 12. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Play rejection of Accessibility API use | Cannot publish on Play | Narrow purpose, prominent disclosure, declaration form, demo video; fall back to APK/F-Droid distribution |
| OEMs kill the service or hide the overlay | Dot disappears | Battery guidance, detect disconnect, clear status in app |
| Dot appears in screenshots on some devices | Bad core experience | Tunable hide delay, per-device testing, setting for delay |
| `FLAG_SECURE` apps block screenshots | User confusion | Help text; nothing to bypass |
| Double-tap delays single tap | Perceived lag | Delay only when double tap is assigned; configurable timeout; explain in UI |
| Compose inside a service overlay is fiddly | Time sink | Plain `View` for the dot, Compose only for panel and app UI; documented lifecycle owner pattern |

## 13. Open questions

1. Monetization: free and open-source, free with one-time donation, or a small paid pro tier (custom gestures, more themes)?
2. Distribution: Play Store first, or APK/F-Droid first to avoid the declaration process during early development?
3. License if open source (MIT, Apache-2.0, GPL)?
4. Visual identity: icon, dot style, and colors under the Dibuilt brand.

## 14. Glossary

- **Dot**: the floating button.
- **Panel**: the quick-action grid opened from the dot.
- **Global action**: a system action performed through `AccessibilityService.performGlobalAction`.
- **Overlay**: a window drawn above other apps via `TYPE_ACCESSIBILITY_OVERLAY`.
