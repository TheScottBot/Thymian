package fyi.punkdev.thymian.typography

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Scans every tracked or untracked but not ignored file in the repository, so
 * a new file is checked locally before it is ever staged. Arguments: the
 * repository root, then the explicit exclusion list. Exits 1 on any violation.
 */
fun main(arguments: Array<String>) {
    require(arguments.size == 2) { "usage: TypographyCheckMain repository_root exclusion_list" }
    val repositoryRoot = File(arguments[0])
    val excludedPaths = ExcludedPathList.parse(File(arguments[1]).readText())
    val candidatePaths = listRepositoryFiles(repositoryRoot)

    val staleExclusions = excludedPaths - candidatePaths.toSet()
    if (staleExclusions.isNotEmpty()) {
        System.err.println("typography check: excluded paths not in the repository: ${staleExclusions.sorted()}")
        exitProcess(1)
    }

    val violations = TypographyScanner.scanFiles(repositoryRoot, candidatePaths, excludedPaths)
    violations.forEach { violation -> System.err.println(violation) }
    println(
        "typography check: ${candidatePaths.size - excludedPaths.size} files scanned, " +
            "${excludedPaths.size} excluded, ${violations.size} violations",
    )
    if (violations.isNotEmpty()) exitProcess(1)
}

private fun listRepositoryFiles(repositoryRoot: File): List<String> {
    // Read only git: lists files without touching the index or history (datum 0.1).
    val gitProcess =
        ProcessBuilder("git", "ls-files", "--cached", "--others", "--exclude-standard", "-z")
            .directory(repositoryRoot)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
    val listing = gitProcess.inputStream.bufferedReader().readText()
    check(gitProcess.waitFor(60, TimeUnit.SECONDS) && gitProcess.exitValue() == 0) {
        "git ls-files failed in ${repositoryRoot.absolutePath}"
    }
    // A path deleted from the working tree but still in the index has nothing to scan.
    return listing
        .split('\u0000')
        .filter { listedPath -> listedPath.isNotEmpty() && File(repositoryRoot, listedPath).isFile }
}
