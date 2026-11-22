package fyi.punkdev.thymian.domain.settings

import fyi.punkdev.thymian.domain.ClockMode
import fyi.punkdev.thymian.domain.TemperatureUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Defaults and bounds of every declared setting (specification 1.3 and 2.2). */
class SettingDeclarationsTest {
    @Test
    fun `every setting is declared once with a unique key`() {
        val keys = SettingDeclarations.all.map { declaration -> declaration.key }

        assertEquals(keys.toSet().size, keys.size, "duplicate keys in $keys")
    }

    @Test
    fun `a new widget is fully transparent with white text`() {
        val defaults = WidgetSettings.defaults()

        assertEquals(0, defaults[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
        assertEquals(0xFFFFFF, defaults[SettingDeclarations.TEXT_COLOUR])
        assertEquals(0x000000, defaults[SettingDeclarations.BACKGROUND_COLOUR])
    }

    @Test
    fun `a new widget follows the device clock and uses celsius`() {
        val defaults = WidgetSettings.defaults()

        assertEquals(ClockMode.FOLLOW_DEVICE, defaults[SettingDeclarations.CLOCK_MODE])
        assertEquals(TemperatureUnit.CELSIUS, defaults[SettingDeclarations.TEMPERATURE_UNIT])
    }

    @Test
    fun `on a new widget the weather tap only refreshes and no app is chosen`() {
        val defaults = WidgetSettings.defaults()

        assertEquals(false, defaults[SettingDeclarations.WEATHER_TAP_OPENS_APP])
        assertEquals("", defaults[SettingDeclarations.WEATHER_TAP_APP_PACKAGE])
    }

    @Test
    fun `a chosen app is kept only when it is a package name`() {
        val appPackage = SettingDeclarations.WEATHER_TAP_APP_PACKAGE

        assertEquals("com.example.weather", appPackage.decode("com.example.weather"))
        assertEquals(null, appPackage.decode("not a package"))
        assertEquals(null, appPackage.decode("nodots"))
        assertEquals(null, appPackage.decode("a.".repeat(200) + "b"))
        assertEquals("", WidgetSettings.defaults().with(appPackage, "com.example/../weather")[appPackage])
    }

    @Test
    fun `every optional element is shown on a new widget`() {
        val defaults = WidgetSettings.defaults()

        for (elementSetting in SettingDeclarations.elementVisibility) {
            assertEquals(true, defaults[elementSetting], elementSetting.key)
        }
        assertEquals(4, SettingDeclarations.elementVisibility.size)
    }

    @Test
    fun `opacity runs from 0 to 100 percent and corner radius from 0 to 32 dp`() {
        assertEquals(0..100, SettingDeclarations.BACKGROUND_OPACITY_PERCENT.bounds)
        assertEquals(0..32, SettingDeclarations.CORNER_RADIUS_DP.bounds)
        assertEquals(16, WidgetSettings.defaults()[SettingDeclarations.CORNER_RADIUS_DP])
    }

    @Test
    fun `out of range numbers are clamped to their bounds`() {
        val settings =
            WidgetSettings
                .defaults()
                .with(SettingDeclarations.BACKGROUND_OPACITY_PERCENT, 140)
                .with(SettingDeclarations.CORNER_RADIUS_DP, -3)

        assertEquals(100, settings[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
        assertEquals(0, settings[SettingDeclarations.CORNER_RADIUS_DP])
    }

    @Test
    fun `colours keep only their red green and blue channels`() {
        val settings = WidgetSettings.defaults().with(SettingDeclarations.TEXT_COLOUR, 0x7F112233)

        assertEquals(0x112233, settings[SettingDeclarations.TEXT_COLOUR])
    }

    @Test
    fun `changing one setting leaves the others alone`() {
        val defaults = WidgetSettings.defaults()
        val changed = defaults.with(SettingDeclarations.SHOW_DATE, false)

        assertEquals(false, changed[SettingDeclarations.SHOW_DATE])
        for (declaration in SettingDeclarations.all.filterNot { other -> other == SettingDeclarations.SHOW_DATE }) {
            assertEquals(defaults.valueOf(declaration), changed.valueOf(declaration), declaration.key)
        }
    }

    @Test
    fun `a choice setting lists its option labels and selects by position`() {
        val clockMode = SettingDeclarations.CLOCK_MODE

        assertEquals(listOf("Follow device", "12 hour", "24 hour"), clockMode.optionLabels)
        assertEquals(0, clockMode.selectedIndexIn(WidgetSettings.defaults()))
        val chosen = clockMode.withOptionAt(WidgetSettings.defaults(), optionIndex = 2)
        assertEquals(ClockMode.TWENTY_FOUR_HOUR, chosen[clockMode])
        assertEquals(2, clockMode.selectedIndexIn(chosen))
    }

    @Test
    fun `the corner radius setting notes that the home screen may round corners further`() {
        // CWD35: the Pixel launcher clips widgets to its own radius, so small values may not show there.
        assertEquals("The home screen may round corners further.", SettingDeclarations.CORNER_RADIUS_DP.note)
        assertEquals(null, SettingDeclarations.BACKGROUND_OPACITY_PERCENT.note)
    }
}
