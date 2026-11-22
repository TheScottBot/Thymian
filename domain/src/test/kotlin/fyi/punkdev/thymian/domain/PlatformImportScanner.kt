package fyi.punkdev.thymian.domain

import java.io.File

/** Finds Kotlin import lines that reach into the Android platform or AndroidX. */
object PlatformImportScanner {
    private val platformImportPattern = Regex("""^\s*import\s+(android|androidx)\.""")

    /** Returns `relative/path.kt:line: text` for each platform import under [sourceRoot], sorted for stable output. */
    fun findPlatformImports(sourceRoot: File): List<String> =
        sourceRoot
            .walkTopDown()
            .filter { candidateFile -> candidateFile.isFile && candidateFile.extension == "kt" }
            .flatMap { sourceFile ->
                val relativePath = sourceFile.relativeTo(sourceRoot).invariantSeparatorsPath
                sourceFile.readLines().mapIndexedNotNull { lineIndex, lineText ->
                    if (platformImportPattern.containsMatchIn(lineText)) {
                        "$relativePath:${lineIndex + 1}: ${lineText.trim()}"
                    } else {
                        null
                    }
                }
            }.sorted()
            .toList()
}
