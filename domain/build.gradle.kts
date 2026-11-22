import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin on the JVM: no Android on the classpath, so domain tests run in seconds (2.1).
plugins {
    alias(libs.plugins.kotlin.jvm)
    // Lets the app's lint analyse domain sources instead of treating them as an external jar.
    alias(libs.plugins.android.lint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    // JSON tree parsing for provider responses; no compiler plugin needed (docs/DEPENDENCIES.md).
    implementation(libs.kotlinx.serialization.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

lint {
    warningsAsErrors = true
    abortOnError = true
}
