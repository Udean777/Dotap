# Developer Setup & Contribution Guide

This guide walks you through setting up, building, testing, and contributing to **Dotap** on your local machine.

---

## 🛠️ Prerequisites

Before getting started, make sure your development environment has:

- **JDK**: Version 21 (Temurin or OpenJDK recommended)
- **Android Studio**: Android Studio Koala Feature Drop / Ladybug / Meerkat or later
- **Android SDK**:
  - `compileSdk`: **37**
  - `minSdk`: **28** (Android 9.0 Pie)
  - `targetSdk`: **37**
- **Git**: Installed and configured on your machine

---

## 📥 Cloning and Opening the Project

1. **Clone the repository**:
   ```bash
   git clone https://github.com/ssajudn/dotap.git
   cd dotap
   ```

2. **Open in Android Studio**:
   - Open Android Studio and choose **Open an Existing Project**.
   - Navigate to the `dotap` directory and select it.
   - Wait for Gradle to synchronize dependencies.

---

## 🚀 Gradle Commands

Dotap includes a standard set of Gradle tasks for formatting, static analysis, testing, and building:

```bash
# Code Formatting (Spotless / ktlint)
./gradlew spotlessCheck          # Check formatting compliance
./gradlew spotlessApply          # Automatically format Kotlin and Gradle files

# Static Analysis (Detekt)
./gradlew detekt                 # Run Detekt analysis

# Unit Tests & Lint
./gradlew testDebugUnitTest      # Run JVM unit tests (Turbine, coroutines-test)
./gradlew lintDebug              # Run Android Lint

# Building & Installing
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install debug APK on connected device/emulator
./gradlew assembleRelease        # Build release APK (R8 optimization enabled)
./gradlew bundleRelease          # Build release Android App Bundle (AAB)
```

> [!IMPORTANT]
> **Pre-Commit Verification**: Before committing or opening a pull request, always ensure all checks pass:
> ```bash
> ./gradlew spotlessApply testDebugUnitTest lintDebug detekt
> ```

---

## 🔑 Local Release Keystore Setup (Optional)

If you want to test signed release builds locally:

1. Copy the template properties file:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
2. Generate a local release keystore if you don't already have one:
   ```bash
   keytool -genkeypair -v -keystore keystores/dotap-release.keystore -alias dotap -keyalg RSA -keysize 2048 -validity 10000 -storetype PKCS12
   ```
3. Update `keystore.properties` with your credentials:
   ```properties
   storeFile=/keystores/dotap-release.keystore
   storePassword=your_keystore_password
   keyAlias=dotap
   keyPassword=your_key_password
   ```

*(Note: `keystore.properties` and `keystores/` are strictly ignored by `.gitignore` and must never be committed).*

---

## 🏛️ Architecture Overview

Dotap strictly enforces **Clean Architecture + MVVM** with manual dependency injection:

```
com.ssajudn.dotap
├── DotapApp.kt, di/AppContainer.kt     Manual DI wiring & container
├── domain/        Pure Kotlin: model/, gesture/, repository/, port/, usecase/
├── data/          settings/ (DataStore), status/ (ContentObserver)
├── presentation/  Jetpack Compose + ViewModels: home/, settings/, panel/, battery/
├── system/        Android adapters: service/, overlay/, tile/
└── util/
```

### Architectural Rules
- **Dependency Rule**: `presentation → domain`, `data → domain`, `system → domain`.
- **Zero Android in Domain**: `domain` is pure Kotlin and **never** imports `android.*` or `androidx.*`.
- **Isolated Presentation**: `presentation` never imports `data` or `system`.
- **No Passing-through**: Use cases are created only when business logic or multiple sources exist.
- **Overlay Rendering**: The floating dot uses a custom lightweight Android `View` (`DotView` + `DotPresenter`) for zero-overhead drawing; the quick action panel is rendered using Jetpack Compose (`PanelContent`).
- **Zero Network**: The app has **no network permission** (`android.permission.INTERNET` is strictly forbidden).

For the complete technical blueprint, see [`docs/ARCHITECTURE.md`](file:///Users/ssajudn/projects/Dotap/docs/ARCHITECTURE.md).

---

## 🧪 Testing Guidelines

- **Domain & ViewModels**: All domain logic, gesture state machines, and ViewModels must have accompanying unit tests.
- **Test Tools**: We use hand-written fakes, `kotlinx-coroutines-test`, and `Turbine` (no mocking frameworks like Mockito or MockK).
- **Physical Device QA**: Accessibility Services and overlay windows cannot be fully tested via JVM unit tests. Manual testing on at least one physical device is required for changes in `system/`.

---

## 🤝 Contributing & Submitting Pull Requests

1. Create a feature branch from `dev`:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. Write clean, self-documenting code adhering to [Clean Code Rules](file:///Users/ssajudn/projects/Dotap/AGENTS.md).
3. Ensure all tests and analysis tools pass:
   ```bash
   ./gradlew spotlessApply testDebugUnitTest lintDebug detekt
   ```
4. Commit your changes using **Conventional Commits**:
   - `feat(scope): short description`
   - `fix(scope): short description`
   - `test(scope): short description`
   - `docs(scope): short description`
5. Push your branch and open a Pull Request targeting `dev`.
