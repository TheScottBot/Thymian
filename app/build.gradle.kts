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
val keystorePassword: String? = providers.environmentVariable(keystorePasswordVariable).orNull
val keyPassword: String? = providers.environmentVariable(keyPasswordVariable).orNull

android {
    namespace = "fyi.punkdev.thymian"
    // Newest stable API level supported by AGP 9.5 (specification 1.5, CWD2).
    compileSdk = 37

    defaultConfig {
        applicationId = "fyi.punkdev.thymian"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = releaseKeystoreFile
            keyAlias = releaseKeyAlias
            storePassword = keystorePassword
            this.keyPassword = keyPassword
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
}

val verifyReleaseSigningInputs =
    tasks.register("verifyReleaseSigningInputs") {
        group = "verification"
        description = "Stops a release build early when the keystore or its passwords are missing."
        doLast {
            val problems =
                buildList {
                    if (!releaseKeystoreFile.isFile) add("release keystore not found at ${releaseKeystoreFile.absolutePath}")
                    if (keystorePassword.isNullOrEmpty()) add("environment variable $keystorePasswordVariable is not set")
                    if (keyPassword.isNullOrEmpty()) add("environment variable $keyPasswordVariable is not set")
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
