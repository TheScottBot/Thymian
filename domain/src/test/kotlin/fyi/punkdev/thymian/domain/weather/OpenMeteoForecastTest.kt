package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.location.Coordinates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The Open-Meteo forecast contract as read on 22 November 2026 (Plan record 8.1),
 * parsed defensively per datum 0.10 and CWD19: size bound first, used fields
 * required and range checked, unknown fields tolerated and counted.
 */
class OpenMeteoForecastTest {
    private fun fixture(name: String): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/open-meteo/$name")) { "missing fixture $name" }.readBytes()

    private fun parse(body: ByteArray) = OpenMeteoForecast.parse(body)

    private fun parse(text: String) = OpenMeteoForecast.parse(text.toByteArray())

    private val validCurrentUnits =
        """"current_units":{"time":"iso8601","interval":"seconds","temperature_2m":"°C","weather_code":"wmo code","is_day":""}"""

    private fun bodyWithCurrent(current: String): String = """{$validCurrentUnits,"current":{$current}}"""

    @Test
    fun `the request asks for current temperature, code and daylight in celsius at two decimal places`() {
        assertEquals(
            "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41" +
                "&current=temperature_2m,weather_code,is_day&temperature_unit=celsius&forecast_days=1",
            OpenMeteoForecast.requestUrl(Coordinates(52.5200, 13.4050)),
        )
    }

    @Test
    fun `the request rounds coordinates and handles the southern and western hemispheres`() {
        assertEquals(
            "https://api.open-meteo.com/v1/forecast?latitude=-33.87&longitude=-151.21" +
                "&current=temperature_2m,weather_code,is_day&temperature_unit=celsius&forecast_days=1",
            OpenMeteoForecast.requestUrl(Coordinates(-33.8688, -151.2093)),
        )
    }

    @Test
    fun `a recorded Berlin response parses with no unknown fields`() {
        val result = parse(fixture("forecast-berlin.json"))

        assertEquals(
            ForecastParseResult.Parsed(
                CurrentWeather(temperatureCelsius = 4.5, condition = WeatherCondition.PARTLY_CLOUDY, isDay = true),
                unknownFieldCount = 0,
            ),
            result,
        )
    }

    @Test
    fun `unknown fields are tolerated and counted`() {
        val result = parse(fixture("forecast-with-unknown-fields.json"))

        assertEquals(
            ForecastParseResult.Parsed(
                CurrentWeather(temperatureCelsius = -2.6, condition = WeatherCondition.SNOW, isDay = false),
                unknownFieldCount = 2,
            ),
            result,
        )
    }

    @Test
    fun `the provider error shape is recognised without keeping its text`() {
        assertEquals(ForecastParseResult.ProviderError, parse(fixture("error-latitude.json")))
    }

    @Test
    fun `a body over 64 KiB is refused before parsing`() {
        val oversized = ByteArray(64 * 1024 + 1) { ' '.code.toByte() }

        assertEquals(ForecastParseResult.Rejected(ForecastRejection.TOO_LARGE), parse(oversized))
    }

    @Test
    fun `text that is not a JSON object is malformed`() {
        for (body in listOf("", "not json", "[1,2]", "\"text\"", "{\"current\":")) {
            assertEquals(ForecastParseResult.Rejected(ForecastRejection.MALFORMED), parse(body), body)
        }
    }

    @Test
    fun `a missing required field is reported`() {
        val result = parse(bodyWithCurrent(""""weather_code":2,"is_day":1"""))

        assertEquals(ForecastParseResult.Rejected(ForecastRejection.MISSING_FIELD), result)
    }

    @Test
    fun `a field of the wrong type is reported`() {
        assertEquals(
            ForecastParseResult.Rejected(ForecastRejection.WRONG_TYPE),
            parse(bodyWithCurrent(""""temperature_2m":"warm","weather_code":2,"is_day":1""")),
        )
        assertEquals(
            ForecastParseResult.Rejected(ForecastRejection.WRONG_TYPE),
            parse(bodyWithCurrent(""""temperature_2m":4.5,"weather_code":2.5,"is_day":1""")),
        )
    }

    @Test
    fun `values outside their ranges are reported`() {
        assertEquals(
            ForecastParseResult.Rejected(ForecastRejection.OUT_OF_RANGE),
            parse(bodyWithCurrent(""""temperature_2m":999,"weather_code":2,"is_day":1""")),
        )
        assertEquals(
            ForecastParseResult.Rejected(ForecastRejection.OUT_OF_RANGE),
            parse(bodyWithCurrent(""""temperature_2m":4.5,"weather_code":2,"is_day":2""")),
        )
        assertEquals(
            ForecastParseResult.Rejected(ForecastRejection.OUT_OF_RANGE),
            parse(bodyWithCurrent(""""temperature_2m":4.5,"weather_code":-1,"is_day":1""")),
        )
    }

    @Test
    fun `a temperature unit other than celsius is refused, since the request asked for celsius`() {
        val fahrenheitUnits = validCurrentUnits.replace("°C", "°F")
        val body = """{$fahrenheitUnits,"current":{"temperature_2m":40.1,"weather_code":2,"is_day":1}}"""

        assertEquals(ForecastParseResult.Rejected(ForecastRejection.UNEXPECTED_UNIT), parse(body))
    }

    @Test
    fun `an undocumented weather code parses as unknown weather`() {
        val result = parse(bodyWithCurrent(""""temperature_2m":4.5,"weather_code":42,"is_day":1"""))

        assertEquals(WeatherCondition.UNKNOWN, (result as ForecastParseResult.Parsed).currentWeather.condition)
    }
}
