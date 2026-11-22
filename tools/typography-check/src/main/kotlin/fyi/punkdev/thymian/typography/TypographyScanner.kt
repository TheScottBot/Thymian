package fyi.punkdev.thymian.typography

import java.io.File

/** The datum 0.8 rules that can be checked mechanically. */
enum class TypographyRule(
    val description: String,
) {
    EM_DASH("em dash (U+2014)"),
    EN_DASH("en dash (U+2013)"),
    HYPHEN_RUN("double or triple hyphen run"),
    DEX_UNSAFE_TEST_NAME("on device test name with a character dex cannot hold"),
}

/** One offending character or run, located by 1 based line and column. */
data class TypographyViolation(
    val relativePath: String,
    val lineNumber: Int,
    val columnNumber: Int,
    val rule: TypographyRule,
) {
    override fun toString(): String = "$relativePath:$lineNumber:$columnNumber: ${rule.description}"
}

object TypographyScanner {
    private const val EM_DASH = '\u2014'
    private const val EN_DASH = '\u2013'

    private val hyphenRunPattern = Regex("-{2,}")

    // A Markdown table separator row (pipes, colons and hyphen runs only) is syntax, exempted by the datum.
    private val tableSeparatorRowPattern = Regex("""^\s*\|?(\s*:?-{3,}:?\s*\|)*\s*:?-{3,}:?\s*\|?\s*$""")

    // A command line flag token such as --no-daemon, exempted by the datum.
    private val flagPrefixCharacters = setOf(' ', '\t', '(', '`', '"', '\'')

    // Dex refuses some characters in method names, so a backtick test name with one fails only at the
    // dexing step of an on device build, which CI never runs; it broke the build three times in CWE2 to CWE6.
    private val backtickNamePattern = Regex("fun `([^`]*)`")
    private val dexUnsafeCharacters = setOf(',', '\'', '.', ';', ':', '/', '[', ']', '<', '>')
    private const val ON_DEVICE_TEST_PATH = "/androidTest/"

    fun scanText(
        relativePath: String,
        text: String,
    ): List<TypographyViolation> =
        text.split('\n').flatMapIndexed { lineIndex, rawLine ->
            val line = rawLine.removeSuffix("\r")
            val dexViolations = if (ON_DEVICE_TEST_PATH in relativePath) dexUnsafeTestNamesIn(line) else emptyList()
            (findViolationsInLine(line) + dexViolations).map { (columnIndex, rule) ->
                TypographyViolation(relativePath, lineIndex + 1, columnIndex + 1, rule)
            }
        }

    private fun dexUnsafeTestNamesIn(line: String): List<Pair<Int, TypographyRule>> =
        backtickNamePattern
            .findAll(line)
            .filter { name -> name.groupValues[1].any { nameCharacter -> nameCharacter in dexUnsafeCharacters } }
            .map { name -> name.range.first + "fun ".length to TypographyRule.DEX_UNSAFE_TEST_NAME }
            .toList()

    /**
     * Scans each listed file under [repositoryRoot], skipping [excludedPaths]
     * (vendored or generated files kept verbatim) and binary files, which hold
     * no prose. A file containing a NUL byte is treated as binary.
     */
    fun scanFiles(
        repositoryRoot: File,
        relativePaths: List<String>,
        excludedPaths: Set<String>,
    ): List<TypographyViolation> =
        relativePaths
            .filterNot { relativePath -> relativePath in excludedPaths }
            .sorted()
            .flatMap { relativePath ->
                val fileBytes = File(repositoryRoot, relativePath).readBytes()
                if (fileBytes.contains(0)) emptyList() else scanText(relativePath, fileBytes.toString(Charsets.UTF_8))
            }

    private fun findViolationsInLine(line: String): List<Pair<Int, TypographyRule>> {
        val dashViolations =
            line.withIndex().mapNotNull { (characterIndex, character) ->
                when (character) {
                    EM_DASH -> characterIndex to TypographyRule.EM_DASH
                    EN_DASH -> characterIndex to TypographyRule.EN_DASH
                    else -> null
                }
            }
        if (tableSeparatorRowPattern.matches(line)) return dashViolations
        val hyphenRunViolations =
            hyphenRunPattern
                .findAll(line)
                .filterNot { hyphenRun -> isExemptHyphenRun(line, hyphenRun.range) }
                .map { hyphenRun -> hyphenRun.range.first to TypographyRule.HYPHEN_RUN }
                .toList()
        return (dashViolations + hyphenRunViolations).sortedBy { (columnIndex, _) -> columnIndex }
    }

    private fun isExemptHyphenRun(
        line: String,
        runRange: IntRange,
    ): Boolean {
        if (runRange.count() != 2) return false
        val characterBefore = line.getOrNull(runRange.first - 1)
        val characterAfter = line.getOrNull(runRange.last + 1)
        val isFlagToken =
            (characterBefore == null || characterBefore in flagPrefixCharacters) &&
                characterAfter != null && characterAfter.isLetter()
        val isXmlCommentOpener = line.regionMatches(runRange.first - 2, "<!", 0, 2)
        val isXmlCommentCloser = characterAfter == '>'
        return isFlagToken || isXmlCommentOpener || isXmlCommentCloser
    }
}

/** The explicit list of paths the scan skips, one per line, `#` for comments. */
object ExcludedPathList {
    private val patternCharacters = setOf('*', '?', '[')

    fun parse(listText: String): Set<String> =
        listText
            .lineSequence()
            .map { listLine -> listLine.trim() }
            .filter { listLine -> listLine.isNotEmpty() && !listLine.startsWith("#") }
            .onEach { listedPath ->
                // The datum excludes by explicit list because a pattern eventually excludes something it should not.
                require(listedPath.none { pathCharacter -> pathCharacter in patternCharacters }) {
                    "excluded path must be explicit, not a pattern: $listedPath"
                }
            }.toSet()
}
