import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// One time conversion of the vendored Meteocons SVGs to Android vector drawables
// (CWD24). Ships in nothing built; recorded with its version and invocation in
// docs/DEPENDENCIES.md (datum 0.13).
plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(libs.android.sdk.common)
    implementation(project(":domain"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    // The tests compare committed output with a fresh conversion, so those files are inputs:
    // without this Gradle would call the tests up to date after a hand edit and skip them.
    val repositoryRoot = rootProject.layout.projectDirectory
    inputs.dir(repositoryRoot.dir("third_party/meteocons/monochrome"))
    inputs.dir(repositoryRoot.dir("app/src/main/res/drawable"))
    inputs.file(repositoryRoot.file("app/src/main/kotlin/fyi/punkdev/thymian/widget/WeatherIconDrawables.kt"))
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<JavaExec>("convertWeatherIcons") {
    group = "generation"
    description = "Regenerates the weather icon drawables and their lookup from the vendored Meteocons SVGs."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("fyi.punkdev.thymian.icons.WeatherIconConversionKt")
    args(rootProject.layout.projectDirectory.asFile.absolutePath)
}
