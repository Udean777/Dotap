# Dotap — Roadmap

Each task is sized for one focused agent session. Do tasks in order unless the human says otherwise. Check the box when done. FR/NFR IDs refer to `PRD.md`.

---

## M0 — Project setup

- [x] **M0.1** Create the Gradle project (Kotlin DSL, version catalog, Compose + Material 3), application ID `com.ssajudn.dotap`, `minSdk` 28, latest stable `compileSdk`/`targetSdk`. Add `../AGENTS.md` at the repo root and the other docs under `docs/`. Create the empty `domain/`, `data/`, `presentation/`, `system/`, `di/` packages, `DotapApp` with an empty `AppContainer`, and the `DispatcherProvider` port with its default implementation.
  *Accept:* `./gradlew assembleDebug` succeeds; app launches to an empty Compose screen.
- [x] **M0.2** Add app theme (Material 3, light/dark), `strings.xml` with `values-in`, launcher icon placeholder.
  *Accept:* theme follows system dark mode; no hardcoded strings.
- [x] **M0.3** Add code-quality tooling: Spotless (ktlint) and detekt with a config file using the starting thresholds in `ARCHITECTURE.md` section 13; add test dependencies (JUnit, kotlinx-coroutines-test, Turbine). No suppressions or baseline.
  *Accept:* `./gradlew spotlessCheck detekt` passes on the empty project.
- [x] **M0.4** Add CI (GitHub Actions): `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `spotlessCheck`, `detekt` on push/PR.
  *Accept:* CI is green on the main branch.

## M1 — Service and overlay skeleton (FR-1, FR-2, FR-5, NFR-7)

- [x] **M1.1** Add `DotapAccessibilityService` (thin entry point), manifest entry, `accessibility_service_config.xml` exactly as in `ARCHITECTURE.md` (no event types, no window content). Add `ServiceConnectionTracker` owned by `AppContainer`; the service reaches it through `DotapApp`.
  *Accept:* service appears in Settings → Accessibility; enabling it sets the tracker to connected, disabling it sets it to disconnected.
- [x] **M1.2** Add `OverlayController` (WindowManager adapter), `DotView` (renders `DotUiState`, no logic), `DotPresenter` and `DotUiState`: show a fixed dot with `TYPE_ACCESSIBILITY_OVERLAY` when the service connects, remove it on unbind/destroy. Unit-test `DotPresenter` state changes.
  *Accept:* dot visible over other apps after enabling; disappears after disabling; no crash when toggling repeatedly.
- [x] **M1.3** Dragging and edge snapping with animation; gesture exclusion rects; persist side and Y ratio in memory first (DataStore comes in M3); re-clamp on rotation.
  *Accept:* drag works smoothly; dot snaps to the nearest side; stays on screen after rotation and split-screen.

## M2 — Gesture engine and actions (FR-10 to FR-14, FR-20 to FR-22)

- [x] **M2.1** Implement `GestureEngine` in `domain/gesture` (pure Kotlin) with an injected `Scheduler` and the state machine in `ARCHITECTURE.md`.
  *Accept:* unit tests cover single, double, long press, drag cancel, double-tap-unassigned (immediate single), config change, cancel in every state; all pass.
- [x] **M2.2** Wire `DotView` touch events to the engine and drag to `OverlayController`.
  *Accept:* on device, drag never fires a tap; long press fires once and not a tap on release.
- [x] **M2.3** Add `ActionType`, the `ActionPerformer` port, `GlobalActionPerformer` (system adapter), `ExecuteActionUseCase`, and `HandleGestureUseCase` for Home, Back, Recents, Notifications, Quick Settings, Power dialog, Lock, None. Use `Defaults` for the initial mapping (single = Home, double = Screenshot, long = None).
  *Accept:* each action works on a test device; use-case unit tests with fake ports cover mapping, haptics, and call order.
- [x] **M2.4** `TakeScreenshotUseCase` with the `OverlayVisibility` port: hide dot, `delay`, perform screenshot, restore; failure message; restore in `finally`. Unit-test with fakes and `runTest` (call order, delays, restore on failure and on exception).
  *Accept:* double tap takes a system screenshot; the dot is not visible in the saved image on the test devices; if the action returns false the dot comes back and a message shows.

## M3 — Settings and persistence (FR-11, FR-43)

- [x] **M3.1** `DotSettings` model with validation (`Defaults`, `Limits`, `with...` clamping methods) and the `SettingsRepository` interface in `domain`; `DataStoreSettingsRepository` and `SettingsMapper` in `data` with the keys and defaults in `ARCHITECTURE.md`.
  *Accept:* tests cover defaults, clamping, unknown stored values, and a round trip through a temp-file DataStore.
- [x] **M3.2** Service collects settings and applies them live (timeouts, assigned actions, haptics, dot position persisted across restarts).
  *Accept:* changing a setting changes behavior without restarting the service; dot position survives app and device restart.
- [x] **M3.3** MVVM settings and home screens: `SettingsViewModel`/`HomeViewModel` with `UiState`, `UiEvent`, and `UiEffect`; Route + stateless Screen with `@Preview`; Gestures (action dropdowns, timeouts), haptics and snap-to-edge toggles; dot visibility toggle. (Size and idle-opacity controls are added in M4.3.) Navigation Compose wiring.
  *Accept:* ViewModel unit tests (fake repository, Turbine) pass; all controls have content descriptions; values are validated in the domain model; changes apply live.

## M4 — Quick-action panel (FR-30, FR-31, FR-33)

- [ ] **M4.1** `OverlayLifecycleOwner` (lifecycle, saved-state, and ViewModelStore owner) and `PanelHost` showing a `ComposeView` panel as an accessibility overlay; implement the `PanelLauncher` port.
  *Accept:* panel opens next to the dot, no crash on rapid open/close, no leaked views after close.
- [ ] **M4.2** Panel UI with `PanelViewModel` (`PanelUiState`, `PanelUiEvent`, close effect): rounded 3×3 grid, centre Close cell, default six slots, tap-outside/Back dismiss. Add `OPEN_PANEL` action and make it the default single-tap action. Unit-test `PanelViewModel`.
  *Accept:* every slot runs its action and closes the panel; Screenshot from the panel hides panel and dot before capture.
- [ ] **M4.3** Idle opacity and dot size: add the settings controls (Appearance screen) and apply them live (FR-3, FR-4).
  *Accept:* dot fades after the idle delay and returns on touch; size slider resizes the dot immediately.

## M5 — Onboarding and compliance (FR-40, FR-41, FR-44, FR-47)

- [ ] **M5.1** Onboarding flow with `OnboardingViewModel`: intro → prominent disclosure screen (text from `COMPLIANCE.md`, explicit Agree) → effect that opens Accessibility settings. Unit-test that the settings effect is emitted only after consent.
  *Accept:* the disclosure is a separate screen, not buried in a privacy policy; the user cannot reach the system settings button without agreeing.
- [ ] **M5.2** `ServiceStatusRepository` (`AccessibilityStatusRepository`) using the system enabled-services list, combined with the `ServiceConnectionTracker` hint in `ObserveServiceStatusUseCase`. Home screen shows the status with a button to open settings. Unit-test the use case for all three `ServiceStatus` values.
  *Accept:* status is right after process death and after the user disables the service from system settings.
- [ ] **M5.3** Android 13+ restricted-settings guidance for sideloaded installs.
  *Accept:* a help screen explains where to allow restricted settings; tested on an Android 13+ device with a sideloaded APK.
- [ ] **M5.4** Prepare store materials: privacy policy page (no data collected), short and full description, screenshots, Data Safety answers, Permission Declaration draft, demo video script. See `COMPLIANCE.md`.
  *Accept:* checklist in `COMPLIANCE.md` is complete.

## M6 — Polish and release (FR-45, FR-46, NFR-4 to NFR-8)

- [ ] **M6.1** Quick Settings tile toggling dot visibility.
- [ ] **M6.2** OEM battery guidance screen (Xiaomi, Oppo, Vivo, Samsung, Huawei) with a link to a vendor-specific help page.
- [ ] **M6.3** TalkBack, dynamic color, large font/display sizes audit.
- [ ] **M6.4** R8 keep rules check; release build size and memory audit (< 5 MB APK, < 30 MB runtime heap).
- [ ] **M6.5** Pre-launch checklist: translation completeness, licensing, reproducible release build.
