plugins {
    alias(libs.plugins.android.application)
}

// Release signing (specification 2.6, CWD8, CWD28). The keystore lives outside
// the repository and the passwords come only from the environment, so neither
// can be committed. Release builds run on the release Mac only; CI never has the passwords.
val releaseKeystoreFile = File(System.getProperty("user.home"), "Keystores/thymian-release.jks")
val releaseKeyAlias = "thymian"
val keystorePasswordVariable = "THYMIAN_KEYSTORE_PASSWORD"
val keyPasswordVariable = "THYMIAN_KEY_PASSWORD"
// Named apart from the signing config's own storePassword and keyPassword: inside its block, a
// value with the same name resolves to the config's property and would assign it to itself.
val releaseKeystorePasswordFromEnvironment: String? = providers.environmentVariable(keystorePasswordVariable).orNull
val releaseKeyPasswordFromEnvironment: String? = providers.environmentVariable(keyPasswordVariable).orNull

android {
    namespace = "fyi.punkdev.thymian"
    // Newest stable API level supported by AGP 9.5 (specification 1.5, CWD2).
    compileSdk = 37

    defaultConfig {
        applicationId = "fyi.punkdev.thymian"
        minSdk = 34
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreFile
            keyAlias = releaseKeyAlias
            storePassword = releaseKeystorePasswordFromEnvironment
            keyPassword = releaseKeyPasswordFromEnvironment
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
        // These compare against whatever was published today, so the same commit
        // would pass one day and fail the next; versions are pinned in the catalogue.
        disable += setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(project(":domain"))
    // Background refresh (specification 2.3 guidance; docs/DEPENDENCIES.md).
    implementation(libs.androidx.work.runtime)

    // On device tests for what only Android can answer, such as locale date patterns and RemoteViews.
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}

val verifyReleaseSigningInputs =
    tasks.register("verifyReleaseSigningInputs") {
        group = "verification"
        description = "Stops a release build early when the keystore or its passwords are missing."
        // Checks the signing config Gradle will actually use, not only the environment: on
        // 22 November 2026 a shadowed name left the key password unset while both variables were set.
        val releaseSigningConfig = android.signingConfigs.getByName("release")
        doLast {
            val problems =
                buildList {
                    if (!releaseKeystoreFile.isFile) add("release keystore not found at ${releaseKeystoreFile.absolutePath}")
                    if (releaseKeystorePasswordFromEnvironment.isNullOrEmpty()) {
                        add(
                            "environment variable $keystorePasswordVariable is not set",
                        )
                    }
                    if (releaseKeyPasswordFromEnvironment.isNullOrEmpty()) add("environment variable $keyPasswordVariable is not set")
                    if (releaseSigningConfig.storeFile == null) add("the release signing config has no keystore file")
                    if (releaseSigningConfig.storePassword.isNullOrEmpty()) add("the release signing config has no keystore password")
                    if (releaseSigningConfig.keyAlias.isNullOrEmpty()) add("the release signing config has no key alias")
                    if (releaseSigningConfig.keyPassword.isNullOrEmpty()) add("the release signing config has no key password")
                }
            if (problems.isNotEmpty()) {
                throw GradleException(
                    "Release signing inputs missing:\n" + problems.joinToString(separator = "\n") { problem -> "  - $problem" } +
                        "\nExport both passwords for the one release command; CI never builds release.",
                )
            }
        }
    }

// Every release variant task chain starts with preReleaseBuild, so checking there
// fails before any compilation instead of at the final signing step.
tasks.matching { candidateTask -> candidateTask.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseSigningInputs)
}
