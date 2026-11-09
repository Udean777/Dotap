# AGENTS.md — Dotap

Instructions for coding agents working in this repository. Read this file first on every task.

## What this project is

Dotap is an Android app that shows a floating dot above all apps. Single tap, double tap, and long press on the dot run configurable actions. Default: double tap takes a screenshot.

Native Kotlin, Jetpack Compose for in-app UI. Android only. No network. Architecture: **MVVM with layered (Clean) architecture** and strict clean-code standards.

## Read before coding

1. `docs/PRD.md` — what to build and acceptance criteria (FR-/NFR- IDs)
2. `docs/ARCHITECTURE.md` — layers, MVVM conventions, package layout, clean-code standards
3. `docs/ROADMAP.md` — milestones and task order; pick the next unchecked task
4. `docs/COMPLIANCE.md` — Play policy constraints that limit what you may add

## Stack

- Kotlin, Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- Jetpack Compose + Material 3, Navigation Compose, `collectAsStateWithLifecycle`
- `androidx.lifecycle.ViewModel` for screen state (presentation layer only)
- Plain Android `View` for the floating dot (not Compose)
- AndroidX DataStore (Preferences) for settings
- Kotlin Coroutines + Flow
- Manual dependency injection via `AppContainer` (no Hilt/Koin)
- Formatting and analysis: Spotless (ktlint) and detekt
- Tests: JUnit, kotlinx-coroutines-test, Turbine; hand-written fakes, no mocking library
- `minSdk` 28; `compileSdk`/`targetSdk` = latest stable
- Application ID: `com.ssajudn.dotap`
- Pin dependency versions in the version catalog using the latest stable releases at the time. Do not guess version numbers from memory; check them.

## Commands

```bash
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # install on connected device/emulator
./gradlew testDebugUnitTest      # JVM unit tests
./gradlew lintDebug              # Android lint
./gradlew spotlessCheck          # formatting check (ktlint)
./gradlew spotlessApply          # auto-format
./gradlew detekt                 # static analysis
./gradlew assembleRelease        # release build (R8 on)
```

Before declaring a task done, run: `./gradlew spotlessApply testDebugUnitTest lintDebug detekt`.

## Package layout

```
com.ssajudn.dotap
├── DotapApp.kt, di/AppContainer.kt     manual DI wiring
├── domain/        pure Kotlin: model/, gesture/, repository/, port/, usecase/
├── data/          settings/ (DataStore), status/ — implement domain interfaces
├── presentation/  Compose + ViewModels: common/, theme/, onboarding/, home/, settings/, panel/
├── system/        Android adapters: service/, overlay/, tile/
└── util/
```

Full detail is in `docs/ARCHITECTURE.md`.

## Architecture rules (MVVM + layers)

**Dependency rule:** `presentation → domain`, `data → domain`, `system → domain`. `domain` depends on nothing in this project and on **no Android or AndroidX classes**. `presentation` never imports `data` or `system`. Wiring happens only in `di/AppContainer.kt` and at the service entry point.

**Per screen, create four types** in the feature folder: `XxxUiState` (immutable data class), `XxxUiEvent` (sealed interface), `XxxUiEffect` (sealed interface for one-off effects), `XxxViewModel`.

**ViewModel rules**
- Constructor-injected collaborators; no `Context`, `View`, or Android framework types.
- Expose one `StateFlow<XxxUiState>` and one `onEvent(XxxUiEvent)`; never expose mutable flows.
- No business rules; those live in use cases or domain models.

**Composable rules**
- Each screen has a `XxxRoute` (gets ViewModel, collects state and effects) and a **stateless** `XxxScreen(state, onEvent)` with `@Preview`.
- No repository or use case access from composables.
- Every interactive element has a content description or semantic role.

**Use cases**
- Create one only when it holds logic or combines sources. **No pass-through use cases.**
- Single `operator fun invoke`. Use `delay` (not `Thread.sleep`, not raw handlers) so tests can use virtual time.

**Overlay and service**
- `DotapAccessibilityService` is a thin entry point: wire objects, forward lifecycle, no business logic.
- The dot uses `DotPresenter` (state holder exposing `StateFlow<DotUiState>`) and `DotView` that only renders state and forwards touch. The panel (Compose) uses a real `PanelViewModel`.

**Dispatchers and state**
- Never reference `Dispatchers.*` directly; use the injected `DispatcherProvider`.
- No global mutable singletons. Shared state is owned by `AppContainer`.

## Clean code rules

1. Intention-revealing names; no abbreviations beyond `dp`, `ms`, `id`.
2. Small functions (about 25 lines max, 4 parameters max, one level of abstraction).
3. One responsibility per class; one top-level class per file; files under about 300 lines.
4. No magic numbers or strings: constants go in `Defaults`/`Limits` (domain) or `strings.xml`.
5. Immutable by default (`val`, `data class`, `copy`).
6. No `!!`. No silent `catch`. Catch specific exceptions only at system boundaries.
7. Comments explain *why*. Remove dead and commented-out code.
8. `private`/`internal` by default; minimal public API.
9. Extract duplication on the third occurrence, not earlier.
10. Prefer composition and constructor injection; interfaces only at layer boundaries.
11. Leave touched code cleaner, but keep unrelated refactors in separate commits.

## Always

- Keep all user-facing strings in `strings.xml` (English default, `values-in` for Indonesian). No hardcoded text.
- Write unit tests for every change in `domain/` and every ViewModel. Use fakes, `runTest`, and Turbine.
- Clean up overlay views and coroutines when the service disconnects or is destroyed.
- Wrap overlay add/remove in try/catch for `WindowManager.BadTokenException` and `IllegalArgumentException`.
- Update `docs/ROADMAP.md` checkboxes when a task is finished.
- Write small, focused commits.

## Ask first (stop and ask the human)

- Changing `minSdk`, application ID, or package structure.
- Adding any permission to the manifest.
- Enabling accessibility capabilities: `canTakeScreenshot`, `canPerformGestures`, or anything that changes what the service can see or do. These change the Play disclosure.
- Adding a dependency not listed in **Stack** (including a DI framework or a mocking library).
- Anything that needs hidden/non-SDK APIs or reflection.
- Relaxing a detekt rule or adding a suppression.

## Never

- Add `INTERNET` or any network code, analytics, ads, or crash SDKs that phone home.
- Set `canRetrieveWindowContent="true"`, declare `accessibilityEventTypes`, or read, log, or store screen content, node trees, or text from other apps. `onAccessibilityEvent` stays empty.
- Use `SYSTEM_ALERT_WINDOW`. The dot uses `TYPE_ACCESSIBILITY_OVERLAY`.
- Write to `Settings.Secure` / `Settings.Global` or try to enable the service programmatically.
- Use the service to automate other apps (clicking buttons, filling fields). Actions are limited to global actions the user mapped to the dot.
- Import Android or AndroidX types into `domain/`.
- Put logic in `DotView`, composables, or the service class.
- Hardcode device-specific hacks without a named constant and a comment.

## Testing

- **Unit tests (JVM):** gesture state machine, use cases, domain models, ViewModels, settings mapper and DataStore repository. Required for any change in those areas.
- **Manual device tests:** accessibility services and overlays cannot be meaningfully unit-tested. For changes in `system/`, run the manual checklist in `docs/ROADMAP.md` ("QA checklist") on at least one physical device and report results in the task summary.

## Workflow per task

1. Pick the next unchecked task in `docs/ROADMAP.md` (or the one the human names).
2. Re-read the relevant FR/NFR IDs in `docs/PRD.md` and the relevant sections of `docs/ARCHITECTURE.md`.
3. Implement the smallest change that satisfies the acceptance criteria, in the correct layer.
4. Run `./gradlew spotlessApply testDebugUnitTest lintDebug detekt`.
5. Check the box in `docs/ROADMAP.md`, then summarize: what changed, what was tested, anything the human must verify on a device.

## Definition of done

- Acceptance criteria in the PRD/ROADMAP are met.
- Unit tests pass; `spotlessCheck`, `lintDebug`, and `detekt` are clean.
- The dependency rule holds (no Android imports in `domain`, no `data`/`system` imports in `presentation`).
- No new permissions, capabilities, or dependencies without approval.
- No leftover overlay or coroutine leaks on service stop.
- Strings externalized, accessibility labels set.

## Commit convention

`type(scope): short summary`, where type is `feat`, `fix`, `refactor`, `test`, `docs`, or `chore`. Example: `feat(domain): add configurable double-tap timeout`.
