import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The datum 0.8 prose and typography scan, run over the whole tree by `check`.
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

val checkTypography = tasks.register<JavaExec>("checkTypography") {
    group = "verification"
    description = "Fails on em dashes, en dashes and hyphen runs anywhere in the repository (datum 0.8)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("fyi.punkdev.thymian.typography.TypographyCheckMainKt")
    val repositoryRoot = rootProject.layout.projectDirectory
    args(repositoryRoot.asFile.absolutePath, repositoryRoot.file("config/typography-excluded-paths.txt").asFile.absolutePath)
    // The repository contents change without Gradle seeing them, so the scan always runs.
    outputs.upToDateWhen { false }
}

tasks.check {
    dependsOn(checkTypography)
}
