package fyi.punkdev.thymian.domain.weather

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Every WMO code Open-Meteo documents (read 22 November 2026, Plan record 8.1)
 * maps to one condition, and each condition names its Meteocons day and night
 * icons in the same table (specification 2.5, CWD23).
 */
class WeatherConditionTest {
    private val documentedCodes =
        mapOf(
            0 to WeatherCondition.CLEAR,
            1 to WeatherCondition.MAINLY_CLEAR,
            2 to WeatherCondition.PARTLY_CLOUDY,
            3 to WeatherCondition.OVERCAST,
            45 to WeatherCondition.FOG,
            48 to WeatherCondition.FOG,
            51 to WeatherCondition.DRIZZLE,
            53 to WeatherCondition.DRIZZLE,
            55 to WeatherCondition.DRIZZLE,
            56 to WeatherCondition.FREEZING_DRIZZLE,
            57 to WeatherCondition.FREEZING_DRIZZLE,
            61 to WeatherCondition.RAIN,
            63 to WeatherCondition.RAIN,
            65 to WeatherCondition.RAIN,
            66 to WeatherCondition.FREEZING_RAIN,
            67 to WeatherCondition.FREEZING_RAIN,
            71 to WeatherCondition.SNOW,
            73 to WeatherCondition.SNOW,
            75 to WeatherCondition.SNOW,
            77 to WeatherCondition.SNOW_GRAINS,
            80 to WeatherCondition.RAIN_SHOWERS,
            81 to WeatherCondition.RAIN_SHOWERS,
            82 to WeatherCondition.RAIN_SHOWERS,
            85 to WeatherCondition.SNOW_SHOWERS,
            86 to WeatherCondition.SNOW_SHOWERS,
            95 to WeatherCondition.THUNDERSTORM,
            96 to WeatherCondition.THUNDERSTORM_WITH_HAIL,
            97 to WeatherCondition.THUNDERSTORM,
            99 to WeatherCondition.THUNDERSTORM_WITH_HAIL,
        )

    @Test
    fun `every documented code maps to its condition`() {
        for ((code, expectedCondition) in documentedCodes) {
            assertEquals(expectedCondition, WeatherCondition.forWmoCode(code), "code $code")
        }
    }

    @Test
    fun `an undocumented code is unknown rather than guessed`() {
        for (undocumentedCode in listOf(-1, 4, 50, 98, 100, Int.MAX_VALUE)) {
            assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.forWmoCode(undocumentedCode), "code $undocumentedCode")
        }
    }

    @Test
    fun `day and night icons for every documented code`() {
        val expectedIcons =
            mapOf(
                WeatherCondition.CLEAR to ("clear-day" to "clear-night"),
                WeatherCondition.MAINLY_CLEAR to ("mostly-clear-day" to "mostly-clear-night"),
                WeatherCondition.PARTLY_CLOUDY to ("partly-cloudy-day" to "partly-cloudy-night"),
                WeatherCondition.OVERCAST to ("overcast-day" to "overcast-night"),
                WeatherCondition.FOG to ("fog-day" to "fog-night"),
                WeatherCondition.DRIZZLE to ("partly-cloudy-day-drizzle" to "partly-cloudy-night-drizzle"),
                WeatherCondition.FREEZING_DRIZZLE to ("partly-cloudy-day-sleet" to "partly-cloudy-night-sleet"),
                WeatherCondition.RAIN to ("partly-cloudy-day-rain" to "partly-cloudy-night-rain"),
                WeatherCondition.FREEZING_RAIN to ("partly-cloudy-day-sleet" to "partly-cloudy-night-sleet"),
                WeatherCondition.SNOW to ("partly-cloudy-day-snow" to "partly-cloudy-night-snow"),
                WeatherCondition.SNOW_GRAINS to ("partly-cloudy-day-snow" to "partly-cloudy-night-snow"),
                WeatherCondition.RAIN_SHOWERS to ("partly-cloudy-day-rain" to "partly-cloudy-night-rain"),
                WeatherCondition.SNOW_SHOWERS to ("partly-cloudy-day-snow" to "partly-cloudy-night-snow"),
                WeatherCondition.THUNDERSTORM to ("thunderstorms-day" to "thunderstorms-night"),
                WeatherCondition.THUNDERSTORM_WITH_HAIL to ("thunderstorms-day-hail" to "thunderstorms-night-hail"),
                WeatherCondition.UNKNOWN to ("not-available" to "not-available"),
            )
        assertEquals(WeatherCondition.entries.toSet(), expectedIcons.keys)
        for ((condition, icons) in expectedIcons) {
            assertEquals(icons.first, condition.iconName(isDay = true), "$condition by day")
            assertEquals(icons.second, condition.iconName(isDay = false), "$condition by night")
        }
    }

    @Test
    fun `every condition has a spoken description in UK English`() {
        assertEquals("Partly cloudy", WeatherCondition.PARTLY_CLOUDY.description)
        assertEquals("Thunderstorm with hail", WeatherCondition.THUNDERSTORM_WITH_HAIL.description)
        for (condition in WeatherCondition.entries) {
            assertEquals(true, condition.description.isNotBlank(), "$condition")
        }
    }

    @Test
    fun `the distinct icon names are the set the app must ship`() {
        assertEquals(23, WeatherCondition.allIconNames().size)
    }
}
