package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.location.Coordinates
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import java.math.BigDecimal
import java.math.RoundingMode

/** What the widget shows from one forecast response. */
data class CurrentWeather(
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
)

/** Why a response was not used, by category only: the payload itself is never kept or logged (datum 0.15). */
enum class ForecastRejection {
    TOO_LARGE,
    MALFORMED,
    MISSING_FIELD,
    WRONG_TYPE,
    OUT_OF_RANGE,
    UNEXPECTED_UNIT,
}

sealed interface ForecastParseResult {
    data class Parsed(
        val currentWeather: CurrentWeather,
        val unknownFieldCount: Int,
    ) : ForecastParseResult

    /** Open-Meteo's documented error shape, `{"error": true, "reason": ...}`. */
    data object ProviderError : ForecastParseResult

    data class Rejected(
        val rejection: ForecastRejection,
    ) : ForecastParseResult
}

/**
 * The Open-Meteo forecast contract as read on 22 November 2026 (Plan record
 * 8.1). Responses are untrusted input (datum 0.10): the size bound is checked
 * before any parsing, the three fields used are required, typed and range
 * checked, and the temperature unit is confirmed. Unknown fields are tolerated
 * and counted rather than rejected, because the provider already sends fields
 * this app does not use and may add more (CWD19, deviation D2).
 */
object OpenMeteoForecast {
    const val MAXIMUM_BODY_BYTES = 64 * 1024

    private const val ENDPOINT = "https://api.open-meteo.com/v1/forecast"

    // Two decimal places is about a kilometre: enough for the weather, and no more precise than needed.
    private const val COORDINATE_DECIMAL_PLACES = 2

    private val PLAUSIBLE_CELSIUS = -100.0..70.0
    private val WMO_CODE_RANGE = 0..99
    private const val CELSIUS_UNIT = "°C"

    private val KNOWN_TOP_LEVEL_FIELDS =
        setOf(
            "latitude",
            "longitude",
            "generationtime_ms",
            "utc_offset_seconds",
            "timezone",
            "timezone_abbreviation",
            "elevation",
            "current_units",
            "current",
        )
    private val KNOWN_CURRENT_FIELDS = setOf("time", "interval", "temperature_2m", "weather_code", "is_day")

    fun requestUrl(coordinates: Coordinates): String =
        "$ENDPOINT?latitude=${rounded(coordinates.latitudeDegrees)}&longitude=${rounded(coordinates.longitudeDegrees)}" +
            "&current=temperature_2m,weather_code,is_day&temperature_unit=celsius&forecast_days=1"

    fun parse(body: ByteArray): ForecastParseResult {
        if (body.size > MAXIMUM_BODY_BYTES) return ForecastParseResult.Rejected(ForecastRejection.TOO_LARGE)
        val root =
            try {
                Json.parseToJsonElement(body.decodeToString()) as? JsonObject
            } catch (malformedJson: SerializationException) {
                null
            } ?: return ForecastParseResult.Rejected(ForecastRejection.MALFORMED)

        if ((root["error"] as? JsonPrimitive)?.booleanOrNull == true) return ForecastParseResult.ProviderError

        val units = root["current_units"] as? JsonObject ?: return rejected(ForecastRejection.MISSING_FIELD)
        val current = root["current"] as? JsonObject ?: return rejected(ForecastRejection.MISSING_FIELD)
        val temperatureField = current["temperature_2m"] ?: return rejected(ForecastRejection.MISSING_FIELD)
        val codeField = current["weather_code"] ?: return rejected(ForecastRejection.MISSING_FIELD)
        val dayField = current["is_day"] ?: return rejected(ForecastRejection.MISSING_FIELD)

        if ((units["temperature_2m"] as? JsonPrimitive)?.takeIf { unit -> unit.isString }?.content != CELSIUS_UNIT) {
            return rejected(ForecastRejection.UNEXPECTED_UNIT)
        }
        val temperatureCelsius = temperatureField.numberOrNull()?.doubleOrNull ?: return rejected(ForecastRejection.WRONG_TYPE)
        val code = codeField.numberOrNull()?.intOrNull ?: return rejected(ForecastRejection.WRONG_TYPE)
        val dayFlag = dayField.numberOrNull()?.intOrNull ?: return rejected(ForecastRejection.WRONG_TYPE)

        if (temperatureCelsius !in PLAUSIBLE_CELSIUS || code !in WMO_CODE_RANGE || dayFlag !in 0..1) {
            return rejected(ForecastRejection.OUT_OF_RANGE)
        }
        val unknownFieldCount =
            root.keys.count { field -> field !in KNOWN_TOP_LEVEL_FIELDS } +
                current.keys.count { field -> field !in KNOWN_CURRENT_FIELDS } +
                units.keys.count { field -> field !in KNOWN_CURRENT_FIELDS }
        return ForecastParseResult.Parsed(
            CurrentWeather(temperatureCelsius, WeatherCondition.forWmoCode(code), isDay = dayFlag == 1),
            unknownFieldCount,
        )
    }

    /** A JSON number, not a quoted string that happens to hold digits. */
    private fun JsonElement.numberOrNull(): JsonPrimitive? = (this as? JsonPrimitive)?.takeIf { primitive -> !primitive.isString }

    private fun rejected(rejection: ForecastRejection) = ForecastParseResult.Rejected(rejection)

    private fun rounded(degrees: Double): String =
        BigDecimal(degrees.toString()).setScale(COORDINATE_DECIMAL_PLACES, RoundingMode.HALF_UP).toPlainString()
}
