package fyi.punkdev.thymian.widget

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.TemperatureUnit
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.sizing.WidgetSize
import fyi.punkdev.thymian.domain.weather.CurrentWeather
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Locale

/** The weather block shows the last good snapshot per each widget's own settings (specification 1.3). */
@RunWith(AndroidJUnit4::class)
class WidgetRendererWeatherTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val wide = WidgetSize(360f, 80f)
    private val snapshot =
        WeatherSnapshot(
            CurrentWeather(temperatureCelsius = 4.5, condition = WeatherCondition.PARTLY_CLOUDY, isDay = true),
            placeName = "Berlin",
            locationSource = LocationSourceUsed.DEVICE_RECENT,
            // CWD38: fresh weather, so the CWE6 stale rule does not apply; the assertions are unchanged.
            fetchedAt = Instant.now(),
        )

    private fun applyWith(
        settings: WidgetSettings = WidgetSettings.defaults(),
        weather: WeatherSnapshot? = snapshot,
        size: WidgetSize = wide,
    ): View =
        WidgetRenderer
            .renderForSize(context, WidgetState(settings, listOf(size), weather), Locale.UK, size)
            .apply(context, FrameLayout(context))

    @Test
    fun `the weather block shows the place name with icon and temperature`() {
        val widgetView = applyWith()

        assertEquals(View.VISIBLE, widgetView.findViewById<View>(R.id.weather_block).visibility)
        assertEquals("Berlin", widgetView.findViewById<TextView>(R.id.weather_place).text.toString())
        assertEquals("5°", widgetView.findViewById<TextView>(R.id.weather_temperature).text.toString())
        assertEquals(View.VISIBLE, widgetView.findViewById<View>(R.id.weather_icon).visibility)
    }

    @Test
    fun `each widget converts to its own unit`() {
        val widgetView =
            applyWith(settings = WidgetSettings.defaults().with(SettingDeclarations.TEMPERATURE_UNIT, TemperatureUnit.FAHRENHEIT))

        assertEquals("40°", widgetView.findViewById<TextView>(R.id.weather_temperature).text.toString())
    }

    @Test
    fun `talkback hears the condition and the temperature with its unit`() {
        val widgetView = applyWith()

        assertEquals(
            "Partly cloudy, 5 degrees Celsius",
            widgetView.findViewById<TextView>(R.id.weather_temperature).contentDescription.toString(),
        )
    }

    @Test
    fun `each optional element can be hidden on its own`() {
        val settings =
            WidgetSettings
                .defaults()
                .with(SettingDeclarations.SHOW_PLACE_NAME, false)
                .with(SettingDeclarations.SHOW_CONDITION_ICON, false)
        val widgetView = applyWith(settings = settings)

        assertEquals(View.GONE, widgetView.findViewById<View>(R.id.weather_place).visibility)
        assertEquals(View.GONE, widgetView.findViewById<View>(R.id.weather_icon).visibility)
        assertEquals(View.VISIBLE, widgetView.findViewById<View>(R.id.weather_temperature).visibility)
    }

    @Test
    fun `a missing place name hides the place line`() {
        val widgetView = applyWith(weather = snapshot.copy(placeName = null))

        assertEquals(View.GONE, widgetView.findViewById<View>(R.id.weather_place).visibility)
    }

    @Test
    fun `with no weather yet the block stays hidden`() {
        assertEquals(View.GONE, applyWith(weather = null).findViewById<View>(R.id.weather_block).visibility)
    }

    @Test
    fun `the small layout shows no weather even when there is some`() {
        assertNull(applyWith(size = WidgetSize(130f, 94f)).findViewById(R.id.weather_block))
    }
}
