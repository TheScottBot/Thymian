plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

// ktlint runs from its pinned command line jar rather than a Gradle plugin, so
// no third party plugin has to keep pace with AGP's built in Kotlin.
val ktlintClasspath: Configuration =
    configurations.create("ktlintClasspath") {
        // ktlint-cli publishes a shadowed and an external variant; the shadowed jar is self contained.
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        }
    }

dependencies {
    ktlintClasspath(libs.ktlint.cli)
}

// ktlint's bundled Kotlin compiler uses sun.misc.Unsafe, which JDK 25 warns about on every run.
val ktlintJvmArguments = listOf("--sun-misc-unsafe-memory-access=allow")

val ktlintSourceGlobs = listOf("**/src/**/*.kt", "**/*.kts", "!**/build/**", "!spikes/**")

val ktlintCheck =
    tasks.register<JavaExec>("ktlintCheck") {
        group = "verification"
        description = "Checks Kotlin sources and build scripts against the official Kotlin style (CWD26)."
        classpath = ktlintClasspath
        mainClass.set("com.pinterest.ktlint.Main")
        workingDir = rootDir
        jvmArgs(ktlintJvmArguments)
        args(ktlintSourceGlobs)
    }

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Rewrites Kotlin sources and build scripts to the official Kotlin style."
    classpath = ktlintClasspath
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir
    jvmArgs(ktlintJvmArguments)
    args(listOf("--format") + ktlintSourceGlobs)
}

tasks.register("check") {
    group = "verification"
    description = "Runs every module's checks plus ktlint over the whole repository."
    dependsOn(ktlintCheck)
    dependsOn(subprojects.map { subproject -> "${subproject.path}:check" })
}
