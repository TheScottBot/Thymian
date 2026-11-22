package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.TemperatureUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** A whole number with a degree sign and no unit letter, as in the reference (Appendix B). */
class TemperatureFormatTest {
    @Test
    fun `celsius rounds to the nearest whole degree`() {
        assertEquals("4°", TemperatureFormat.format(celsius = 4.4, unit = TemperatureUnit.CELSIUS))
        assertEquals("5°", TemperatureFormat.format(celsius = 4.5, unit = TemperatureUnit.CELSIUS))
        assertEquals("-3°", TemperatureFormat.format(celsius = -2.6, unit = TemperatureUnit.CELSIUS))
    }

    @Test
    fun `fahrenheit converts before rounding`() {
        assertEquals("32°", TemperatureFormat.format(celsius = 0.0, unit = TemperatureUnit.FAHRENHEIT))
        assertEquals("40°", TemperatureFormat.format(celsius = 4.5, unit = TemperatureUnit.FAHRENHEIT))
        assertEquals("-40°", TemperatureFormat.format(celsius = -40.0, unit = TemperatureUnit.FAHRENHEIT))
    }

    @Test
    fun `a value just below zero never shows as minus zero`() {
        assertEquals("0°", TemperatureFormat.format(celsius = -0.4, unit = TemperatureUnit.CELSIUS))
    }

    @Test
    fun `the spoken form names the unit`() {
        assertEquals("4 degrees Celsius", TemperatureFormat.spoken(celsius = 4.4, unit = TemperatureUnit.CELSIUS))
        assertEquals("40 degrees Fahrenheit", TemperatureFormat.spoken(celsius = 4.5, unit = TemperatureUnit.FAHRENHEIT))
    }
}
