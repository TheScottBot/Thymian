plugins {
    id("com.android.application")
}

android {
    // A separate application ID so the spike never collides with the real app.
    namespace = "fyi.punkdev.thymian.spike.background"
    compileSdk = 37

    defaultConfig {
        applicationId = "fyi.punkdev.thymian.spike.background"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "spike-1"
    }
}

dependencies {
    // The specification's guidance names WorkManager for the periodic refresh; the soak tests it as shipped.
    implementation("androidx.work:work-runtime:2.12.0")
}
