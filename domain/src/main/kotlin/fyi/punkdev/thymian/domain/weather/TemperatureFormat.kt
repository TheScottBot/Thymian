package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.TemperatureUnit
import kotlin.math.roundToInt

/**
 * Weather is fetched in Celsius once and converted per widget, so two widgets
 * with different units share one refresh. Shown as a whole number with a degree
 * sign and no unit letter (specification Appendix B); spoken with the unit.
 */
object TemperatureFormat {
    private const val FAHRENHEIT_PER_CELSIUS = 9.0 / 5.0
    private const val FAHRENHEIT_AT_ZERO_CELSIUS = 32.0

    fun format(
        celsius: Double,
        unit: TemperatureUnit,
    ): String = "${wholeDegrees(celsius, unit)}°"

    fun spoken(
        celsius: Double,
        unit: TemperatureUnit,
    ): String {
        val unitName =
            when (unit) {
                TemperatureUnit.CELSIUS -> "Celsius"
                TemperatureUnit.FAHRENHEIT -> "Fahrenheit"
            }
        return "${wholeDegrees(celsius, unit)} degrees $unitName"
    }

    private fun wholeDegrees(
        celsius: Double,
        unit: TemperatureUnit,
    ): Int {
        val inUnit =
            when (unit) {
                TemperatureUnit.CELSIUS -> celsius
                TemperatureUnit.FAHRENHEIT -> celsius * FAHRENHEIT_PER_CELSIUS + FAHRENHEIT_AT_ZERO_CELSIUS
            }
        // roundToInt gives an Int, so a value such as -0.4 becomes 0, never "-0".
        return inUnit.roundToInt()
    }
}
