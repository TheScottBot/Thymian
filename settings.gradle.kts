pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "thymian"

include(":app")
include(":domain")
include(":typography-check")
include(":icon-conversion")
project(":icon-conversion").projectDir = file("tools/icon-conversion")
project(":typography-check").projectDir = file("tools/typography-check")
