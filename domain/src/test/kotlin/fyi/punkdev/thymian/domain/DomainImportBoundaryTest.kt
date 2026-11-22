package fyi.punkdev.thymian.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * The domain module must stay free of Android so it builds and tests with the
 * Kotlin toolchain alone (datum 0.10, specification 2.1). The compile classpath
 * already lacks Android, but this check names the offending file and line, and
 * the planted violation tests prove it can fail (datum 0.4).
 */
class DomainImportBoundaryTest {
    @Test
    fun `scanner reports an android import planted in a source tree`(
        @TempDir plantedSourceRoot: File,
    ) {
        File(plantedSourceRoot, "Planted.kt").writeText(
            "package planted\n\nimport android.content.Context\n",
        )

        val violations = PlatformImportScanner.findPlatformImports(plantedSourceRoot)

        assertEquals(listOf("Planted.kt:3: import android.content.Context"), violations)
    }

    @Test
    fun `scanner reports an androidx import in a nested directory`(
        @TempDir plantedSourceRoot: File,
    ) {
        val nestedDirectory = File(plantedSourceRoot, "nested").apply { mkdirs() }
        File(nestedDirectory, "PlantedNested.kt").writeText(
            "package planted.nested\nimport androidx.work.Worker\n",
        )

        val violations = PlatformImportScanner.findPlatformImports(plantedSourceRoot)

        assertEquals(listOf("nested/PlantedNested.kt:2: import androidx.work.Worker"), violations)
    }

    @Test
    fun `scanner ignores imports that only start with the word android`(
        @TempDir plantedSourceRoot: File,
    ) {
        File(plantedSourceRoot, "NotPlatform.kt").writeText(
            "package planted\nimport androidish.Thing\nimport java.time.Instant\n",
        )

        assertEquals(emptyList<String>(), PlatformImportScanner.findPlatformImports(plantedSourceRoot))
    }

    @Test
    fun `domain main sources import nothing from android or androidx`() {
        // Gradle runs module tests with the module directory as the working directory.
        val domainSourceRoot = File("src/main/kotlin")
        assertTrue(domainSourceRoot.isDirectory, "domain source root missing: ${domainSourceRoot.absolutePath}")

        assertEquals(emptyList<String>(), PlatformImportScanner.findPlatformImports(domainSourceRoot))
    }
}
