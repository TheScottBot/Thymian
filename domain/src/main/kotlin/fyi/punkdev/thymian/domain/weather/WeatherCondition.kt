package fyi.punkdev.thymian.domain.weather

/**
 * The closed set of conditions, in one table with the WMO codes Open-Meteo
 * documents and the Meteocons icons for day and night (specification 2.5).
 * Precipitation has no plain day or night icon in Meteocons, so it uses the
 * partly cloudy compounds (CWD23). An undocumented code is [UNKNOWN], never a
 * guess. Descriptions are what TalkBack reads, in UK English.
 */
enum class WeatherCondition(
    val description: String,
    private val wmoCodes: Set<Int>,
    private val dayIconName: String,
    private val nightIconName: String,
) {
    CLEAR("Clear", setOf(0), "clear-day", "clear-night"),
    MAINLY_CLEAR("Mainly clear", setOf(1), "mostly-clear-day", "mostly-clear-night"),
    PARTLY_CLOUDY("Partly cloudy", setOf(2), "partly-cloudy-day", "partly-cloudy-night"),
    OVERCAST("Overcast", setOf(3), "overcast-day", "overcast-night"),
    FOG("Fog", setOf(45, 48), "fog-day", "fog-night"),
    DRIZZLE("Drizzle", setOf(51, 53, 55), "partly-cloudy-day-drizzle", "partly-cloudy-night-drizzle"),
    FREEZING_DRIZZLE("Freezing drizzle", setOf(56, 57), "partly-cloudy-day-sleet", "partly-cloudy-night-sleet"),
    RAIN("Rain", setOf(61, 63, 65), "partly-cloudy-day-rain", "partly-cloudy-night-rain"),
    FREEZING_RAIN("Freezing rain", setOf(66, 67), "partly-cloudy-day-sleet", "partly-cloudy-night-sleet"),
    SNOW("Snow", setOf(71, 73, 75), "partly-cloudy-day-snow", "partly-cloudy-night-snow"),
    SNOW_GRAINS("Snow grains", setOf(77), "partly-cloudy-day-snow", "partly-cloudy-night-snow"),
    RAIN_SHOWERS("Rain showers", setOf(80, 81, 82), "partly-cloudy-day-rain", "partly-cloudy-night-rain"),
    SNOW_SHOWERS("Snow showers", setOf(85, 86), "partly-cloudy-day-snow", "partly-cloudy-night-snow"),
    THUNDERSTORM("Thunderstorm", setOf(95, 97), "thunderstorms-day", "thunderstorms-night"),
    THUNDERSTORM_WITH_HAIL("Thunderstorm with hail", setOf(96, 99), "thunderstorms-day-hail", "thunderstorms-night-hail"),
    UNKNOWN("Weather unknown", emptySet(), "not-available", "not-available"),
    ;

    fun iconName(isDay: Boolean): String = if (isDay) dayIconName else nightIconName

    companion object {
        fun forWmoCode(code: Int): WeatherCondition = entries.firstOrNull { condition -> code in condition.wmoCodes } ?: UNKNOWN

        /** Every icon the app must ship; checked against the vendored drawables (CWD24). */
        fun allIconNames(): Set<String> = entries.flatMap { condition -> listOf(condition.dayIconName, condition.nightIconName) }.toSet()
    }
}
