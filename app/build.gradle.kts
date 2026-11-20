import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

// Release credentials come from local keystore.properties or environment variables (CI).
val keystoreProps = Properties()
rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use {
    keystoreProps.load(it)
}
val releaseStorePath = keystoreProps["storeFile"]?.toString() ?: System.getenv("KEYSTORE_FILE")
val releaseStoreFile =
    releaseStorePath?.let { path ->
        file(path).takeIf { it.exists() } ?: rootProject.file(path.removePrefix("/"))
    }
val releaseStorePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProps["storePassword"] as String?
val releaseKeyAlias = System.getenv("KEY_ALIAS") ?: keystoreProps["keyAlias"] as String?
val releaseKeyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps["keyPassword"] as String?
val releaseVersionCodeInput = System.getenv("VERSION_CODE")
val releaseVersionNameInput = System.getenv("VERSION_NAME")

android {
    namespace = "com.ssajudn.dotap"
    compileSdk {
        version = release(37)
    }

    signingConfigs {
        create("release") {
            storeFile = releaseStoreFile
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "com.ssajudn.dotap"
        minSdk = 28
        targetSdk = 37
        versionCode = releaseVersionCodeInput?.toIntOrNull() ?: 1
        versionName = releaseVersionNameInput ?: "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

val validateReleaseInputs =
    tasks.register("validateReleaseInputs") {
        group = "verification"
        description = "Checks required signing and version inputs before creating a release artifact."
        val configuredReleaseStoreFile = releaseStoreFile
        val configuredReleaseStorePassword = releaseStorePassword
        val configuredReleaseKeyAlias = releaseKeyAlias
        val configuredReleaseKeyPassword = releaseKeyPassword
        val configuredReleaseVersionCode = releaseVersionCodeInput
        val configuredReleaseVersionName = releaseVersionNameInput

        doLast {
            val missing =
                buildList {
                    if (configuredReleaseStoreFile == null || !configuredReleaseStoreFile.isFile) {
                        add("KEYSTORE_FILE / storeFile")
                    }
                    if (configuredReleaseStorePassword.isNullOrBlank()) add("KEYSTORE_PASSWORD / storePassword")
                    if (configuredReleaseKeyAlias.isNullOrBlank()) add("KEY_ALIAS / keyAlias")
                    if (configuredReleaseKeyPassword.isNullOrBlank()) add("KEY_PASSWORD / keyPassword")
                }
            check(missing.isEmpty()) {
                "Release build refused. Set valid values for: ${missing.joinToString()}."
            }
            if (configuredReleaseVersionCode != null) {
                check(configuredReleaseVersionCode.toIntOrNull()?.let { it > 0 } == true) {
                    "VERSION_CODE must be a positive integer."
                }
            }
            if (configuredReleaseVersionName != null) {
                val semverRegex = Regex("\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.-]+)?")
                check(configuredReleaseVersionName.matches(semverRegex)) {
                    "VERSION_NAME must use semantic version format, for example 1.2.3 or 1.2.3-beta.1."
                }
            }
        }
    }

tasks.configureEach {
    if (name == "bundleRelease" || name == "assembleRelease") {
        dependsOn(validateReleaseInputs)
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            ),
        )
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
