plugins {
    alias(libs.plugins.android.application)
}

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
