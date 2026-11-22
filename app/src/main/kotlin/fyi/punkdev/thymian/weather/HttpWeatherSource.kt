package fyi.punkdev.thymian.weather

import android.util.Log
import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.weather.ForecastParseResult
import fyi.punkdev.thymian.domain.weather.OpenMeteoForecast
import fyi.punkdev.thymian.domain.weather.WeatherFetchFailure
import fyi.punkdev.thymian.domain.weather.WeatherFetchResult
import fyi.punkdev.thymian.domain.weather.WeatherSource

/**
 * Adapter: the Open-Meteo forecast over HTTPS. Logs status, length and category,
 * never the body or the coordinates (datum 0.15).
 */
class HttpWeatherSource(
    private val httpGet: BoundedHttpGet = BoundedHttpGet(OpenMeteoForecast.MAXIMUM_BODY_BYTES + 1),
) : WeatherSource {
    override fun fetch(coordinates: Coordinates): WeatherFetchResult =
        when (val reply = httpGet.get(OpenMeteoForecast.requestUrl(coordinates))) {
            is HttpReply.NetworkFailure -> failed(WeatherFetchFailure.NETWORK, "exception=${reply.exceptionName}")
            is HttpReply.Body -> fromBody(reply)
        }

    private fun fromBody(reply: HttpReply.Body): WeatherFetchResult {
        val detail = "status=${reply.status} body_length=${reply.bytes.size}"
        return when (val parsed = OpenMeteoForecast.parse(reply.bytes)) {
            is ForecastParseResult.Parsed -> {
                if (reply.status in BoundedHttpGet.HTTP_SUCCESS) {
                    Log.i(LOG_TAG, "event=weather_fetched $detail unknown_fields=${parsed.unknownFieldCount}")
                    WeatherFetchResult.Fetched(parsed.currentWeather)
                } else {
                    failed(WeatherFetchFailure.HTTP_STATUS, detail)
                }
            }

            ForecastParseResult.ProviderError -> {
                failed(WeatherFetchFailure.PROVIDER_ERROR, detail)
            }

            is ForecastParseResult.Rejected -> {
                failed(WeatherFetchFailure.REJECTED_RESPONSE, "$detail rejection=${parsed.rejection}")
            }
        }
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
    }
}
