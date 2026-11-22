package fyi.punkdev.thymian.spike.background

import android.content.Context
import java.io.File

/**
 * Spike only. One line per worker run in app private storage, capped so a long
 * soak cannot grow without bound. Read back over adb with `run-as`.
 */
object SoakLog {

    private const val FILE_NAME = "soak.log"
    private const val MAXIMUM_LINES = 500

    private fun logFile(context: Context) = File(context.filesDir, FILE_NAME)

    @Synchronized
    fun append(context: Context, line: String) {
        val keptLines = readLines(context).takeLast(MAXIMUM_LINES - 1) + line
        logFile(context).writeText(keptLines.joinToString(separator = "\n", postfix = "\n"))
    }

    fun readLines(context: Context): List<String> {
        val file = logFile(context)
        return if (file.exists()) file.readLines().filter { storedLine -> storedLine.isNotBlank() } else emptyList()
    }
}
