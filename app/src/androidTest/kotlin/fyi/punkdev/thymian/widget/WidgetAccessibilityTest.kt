package fyi.punkdev.thymian.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Locale

/**
 * CWE7: every view in every widget layout is described or marked decorative, so
 * TalkBack reads the time, date, temperature, condition and place (specification
 * 1.4). A text view describes itself through its text; a clock must not be given
 * a fixed description, which would hide the time it shows. Containers group
 * their children and need no description of their own.
 */
@RunWith(AndroidJUnit4::class)
class WidgetAccessibilityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val widgetLayouts =
        mapOf(
            "thymian_widget_small" to R.layout.thymian_widget_small,
            "thymian_widget_wide" to R.layout.thymian_widget_wide,
            "thymian_widget_tall" to R.layout.thymian_widget_tall,
        )

    private fun View.allViews(): List<View> =
        if (this is ViewGroup) {
            listOf(this) +
                (0 until childCount).flatMap { childIndex ->
                    getChildAt(childIndex).allViews()
                }
        } else {
            listOf(this)
        }

    /** Returns why [view] would be silent or misleading to TalkBack, or null when it is fine. */
    private fun problemWith(view: View): String? =
        when {
            view is ViewGroup -> null

            view.importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_NO -> null

            view is TextView -> null

            !view.contentDescription.isNullOrBlank() -> null

            else -> "${context.resources.getResourceEntryName(
                view.id,
            )} (${view::class.simpleName}) has no description and is not marked decorative"
        }

    @Test
    fun `every view in every widget layout is described or decorative`() {
        val problems =
            widgetLayouts.flatMap { (layoutName, layoutId) ->
                LayoutInflater
                    .from(context)
                    .inflate(layoutId, FrameLayout(context), false)
                    .allViews()
                    .mapNotNull { view -> problemWith(view)?.let { problem -> "$layoutName: $problem" } }
            }

        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `the rendered widget reads the condition and temperature with its unit`() {
        val size = WidgetSize(360f, 80f)
        val weather =
            WeatherSnapshot(
                CurrentWeather(4.5, WeatherCondition.RAIN, isDay = true),
                "Berlin",
                LocationSourceUsed.DEVICE_RECENT,
                Instant.now(),
            )
        val widgetView =
            WidgetRenderer
                .renderForSize(context, WidgetState(WidgetSettings.defaults(), listOf(size), weather), Locale.UK, size)
                .apply(context, FrameLayout(context))

        assertEquals("Rain, 5 degrees Celsius", widgetView.findViewById<TextView>(R.id.weather_temperature).contentDescription.toString())
        assertEquals("Berlin", widgetView.findViewById<TextView>(R.id.weather_place).text.toString())
        // TextClock writes its own description from the time it shows on every tick (AOSP TextClock.onTimeChanged),
        // so the spoken time always matches the screen; Thymian must not set a fixed one over it.
        val digits = widgetView.findViewById<TextView>(R.id.clock_digits)
        assertEquals(digits.text.toString(), digits.contentDescription.toString())
    }
}
