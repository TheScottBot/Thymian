package fyi.punkdev.thymian.weather

import android.util.Log
import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.weather.ForecastParseResult
import fyi.punkdev.thymian.domain.weather.OpenMeteoForecast
import fyi.punkdev.thymian.domain.weather.WeatherFetchFailure
import fyi.punkdev.thymian.domain.weather.WeatherFetchResult
import fyi.punkdev.thymian.domain.weather.WeatherSource
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Adapter: fetches the Open-Meteo forecast over HTTPS. Reads at most one byte
 * past the parser's bound, so an oversized body is refused without being read
 * in full (datum 0.10). Logs categories and lengths, never the body or the
 * coordinates (datum 0.15).
 */
class HttpWeatherSource : WeatherSource {
    override fun fetch(coordinates: Coordinates): WeatherFetchResult {
        val connection = URL(OpenMeteoForecast.requestUrl(coordinates)).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MILLISECONDS
            connection.readTimeout = TIMEOUT_MILLISECONDS
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            val bodyStream = if (status in HTTP_SUCCESS) connection.inputStream else connection.errorStream
            if (bodyStream == null) return failed(WeatherFetchFailure.HTTP_STATUS, "status=$status body_length=0")
            val body = bodyStream.use { stream -> readAtMost(stream, OpenMeteoForecast.MAXIMUM_BODY_BYTES + 1) }
            when (val parsed = OpenMeteoForecast.parse(body)) {
                is ForecastParseResult.Parsed -> {
                    if (status in HTTP_SUCCESS) {
                        Log.i(
                            LOG_TAG,
                            "event=weather_fetched status=$status body_length=${body.size} unknown_fields=${parsed.unknownFieldCount}",
                        )
                        WeatherFetchResult.Fetched(parsed.currentWeather)
                    } else {
                        failed(WeatherFetchFailure.HTTP_STATUS, "status=$status body_length=${body.size}")
                    }
                }

                ForecastParseResult.ProviderError -> {
                    failed(WeatherFetchFailure.PROVIDER_ERROR, "status=$status body_length=${body.size}")
                }

                is ForecastParseResult.Rejected -> {
                    failed(WeatherFetchFailure.REJECTED_RESPONSE, "status=$status body_length=${body.size} rejection=${parsed.rejection}")
                }
            }
        } catch (networkFailure: IOException) {
            failed(WeatherFetchFailure.NETWORK, "exception=${networkFailure::class.simpleName}")
        } finally {
            connection.disconnect()
        }
    }

    private fun readAtMost(
        stream: InputStream,
        limit: Int,
    ): ByteArray {
        val buffer = ByteArray(limit)
        var filled = 0
        while (filled < limit) {
            val readCount = stream.read(buffer, filled, limit - filled)
            if (readCount < 0) break
            filled += readCount
        }
        return buffer.copyOf(filled)
    }

    private fun failed(
        failure: WeatherFetchFailure,
        detail: String,
    ): WeatherFetchResult {
        Log.w(LOG_TAG, "event=weather_fetch_failed failure=$failure $detail")
        return WeatherFetchResult.Failed(failure)
    }

    private companion object {
        const val LOG_TAG = "Thymian"
        const val TIMEOUT_MILLISECONDS = 15_000
        val HTTP_SUCCESS = 200..299
    }
}
