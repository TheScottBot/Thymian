package fyi.punkdev.thymian.weather

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/** What came back from one GET, before any parsing. */
sealed interface HttpReply {
    data class Body(
        val status: Int,
        val bytes: ByteArray,
    ) : HttpReply

    data class NetworkFailure(
        val exceptionName: String,
    ) : HttpReply
}

/**
 * One HTTPS GET with timeouts that reads at most [maximumBytes]. Callers pass one
 * byte more than their parser accepts, so an oversized body is refused by the
 * parser without being read in full (datum 0.10). Shared by every network
 * adapter so they cannot drift apart.
 */
class BoundedHttpGet(
    private val maximumBytes: Int,
) {
    fun get(url: String): HttpReply {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MILLISECONDS
            connection.readTimeout = TIMEOUT_MILLISECONDS
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            val stream = if (status in HTTP_SUCCESS) connection.inputStream else connection.errorStream
            HttpReply.Body(status, stream?.use { bodyStream -> readAtMost(bodyStream) } ?: ByteArray(0))
        } catch (networkFailure: IOException) {
            HttpReply.NetworkFailure(networkFailure::class.simpleName ?: "IOException")
        } finally {
            connection.disconnect()
        }
    }

    private fun readAtMost(stream: InputStream): ByteArray {
        val buffer = ByteArray(maximumBytes)
        var filled = 0
        while (filled < maximumBytes) {
            val readCount = stream.read(buffer, filled, maximumBytes - filled)
            if (readCount < 0) break
            filled += readCount
        }
        return buffer.copyOf(filled)
    }

    companion object {
        val HTTP_SUCCESS = 200..299
        private const val TIMEOUT_MILLISECONDS = 15_000
    }
}
