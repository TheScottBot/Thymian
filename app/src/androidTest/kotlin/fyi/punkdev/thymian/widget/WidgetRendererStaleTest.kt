package fyi.punkdev.thymian.widget

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.sizing.WidgetSize
import fyi.punkdev.thymian.domain.weather.CurrentWeather
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant
import java.util.Locale

/**
 * Specification 1.3: a subtle stale indicator once the weather is older than
 * twice the refresh interval, keeping the last good data. A symbol, not a colour
 * change (datum 0.16), and read out by TalkBack.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRendererStaleTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val wide = WidgetSize(360f, 80f)

    private fun appliedWithWeatherAged(age: Duration): View {
        val snapshot =
            WeatherSnapshot(
                CurrentWeather(4.5, WeatherCondition.CLEAR, isDay = true),
                "Berlin",
                LocationSourceUsed.DEVICE_RECENT,
                Instant.now().minus(age),
            )
        return WidgetRenderer
            .renderForSize(context, WidgetState(WidgetSettings.defaults(), listOf(wide), snapshot), Locale.UK, wide)
            .apply(context, FrameLayout(context))
    }

    @Test
    fun `recent weather shows no stale indicator`() {
        assertEquals(View.GONE, appliedWithWeatherAged(Duration.ofMinutes(20)).findViewById<View>(R.id.weather_stale).visibility)
    }

    @Test
    fun `weather older than an hour shows the indicator and keeps the temperature`() {
        val widgetView = appliedWithWeatherAged(Duration.ofMinutes(75))

        assertEquals(View.VISIBLE, widgetView.findViewById<View>(R.id.weather_stale).visibility)
        assertEquals("5°", widgetView.findViewById<TextView>(R.id.weather_temperature).text.toString())
    }

    @Test
    fun `talkback hears that stale weather is out of date`() {
        val description =
            appliedWithWeatherAged(
                Duration.ofMinutes(75),
            ).findViewById<TextView>(R.id.weather_temperature).contentDescription.toString()

        assertTrue(description, description.endsWith("last updated over an hour ago"))
    }
}
