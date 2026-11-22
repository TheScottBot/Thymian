plugins {
    id("com.android.application")
}

android {
    // A separate application ID so the spike never collides with the real app.
    namespace = "fyi.punkdev.thymian.spike"
    compileSdk = 37

    defaultConfig {
        applicationId = "fyi.punkdev.thymian.spike"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "spike-1"
    }
}
