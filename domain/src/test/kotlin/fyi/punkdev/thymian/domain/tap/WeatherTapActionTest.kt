package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WeatherTapActionTest {
    private val withAppChosen =
        WidgetSettings
            .defaults()
            .with(SettingDeclarations.WEATHER_TAP_OPENS_APP, true)
            .with(SettingDeclarations.WEATHER_TAP_APP_PACKAGE, "com.example.weather")

    @Test
    fun `by default touching the weather only refreshes it`() {
        assertEquals(WeatherTapAction.Refresh, WeatherTapAction.choose(WidgetSettings.defaults()) { true })
    }

    @Test
    fun `with the option on and an app chosen the weather refreshes and opens that app`() {
        assertEquals(WeatherTapAction.RefreshAndOpen("com.example.weather"), WeatherTapAction.choose(withAppChosen) { true })
    }

    @Test
    fun `with the option on but no app chosen the weather only refreshes`() {
        val settings = WidgetSettings.defaults().with(SettingDeclarations.WEATHER_TAP_OPENS_APP, true)

        assertEquals(WeatherTapAction.Refresh, WeatherTapAction.choose(settings) { true })
    }

    @Test
    fun `a chosen app that can no longer be opened falls back to refreshing`() {
        assertEquals(WeatherTapAction.Refresh, WeatherTapAction.choose(withAppChosen) { false })
    }

    @Test
    fun `an app chosen while the option is off is kept but not opened`() {
        val settings = withAppChosen.with(SettingDeclarations.WEATHER_TAP_OPENS_APP, false)

        assertEquals(WeatherTapAction.Refresh, WeatherTapAction.choose(settings) { true })
        assertEquals("com.example.weather", settings[SettingDeclarations.WEATHER_TAP_APP_PACKAGE])
    }
}
