package fyi.punkdev.thymian.widget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.FrameLayout
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
import fyi.punkdev.thymian.weather.WeatherTapReceiver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Locale

/** Specification 1.3 and CWD12: the clock opens alarms, the weather refreshes. */
@RunWith(AndroidJUnit4::class)
class WidgetTapTargetsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val wide = WidgetSize(360f, 80f)
    private val snapshot =
        WeatherSnapshot(
            CurrentWeather(4.5, WeatherCondition.CLEAR, isDay = true),
            "Berlin",
            LocationSourceUsed.DEVICE_RECENT,
            Instant.now(),
        )

    private fun applied(): View =
        WidgetRenderer
            .renderForSize(context, WidgetState(WidgetSettings.defaults(), listOf(wide), snapshot), Locale.UK, wide)
            .apply(context, FrameLayout(context))

    @Test
    fun `the clock and the weather each have a tap action`() {
        val widgetView = applied()

        assertTrue(widgetView.findViewById<View>(R.id.clock_block).hasOnClickListeners())
        assertTrue(widgetView.findViewById<View>(R.id.weather_block).hasOnClickListeners())
    }

    @Test
    fun `the tap refresh receiver cannot be reached by other apps`() {
        // A refresh the user asks for skips the throttle, so only this app's own tap may send it.
        val receiverInfo =
            context.packageManager.getReceiverInfo(
                ComponentName(context, WeatherTapReceiver::class.java),
                PackageManager.ComponentInfoFlags.of(0),
            )

        assertFalse(receiverInfo.exported)
    }
}
