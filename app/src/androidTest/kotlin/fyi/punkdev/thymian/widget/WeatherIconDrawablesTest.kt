package fyi.punkdev.thymian.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Vector drawables are parsed only at run time, so every converted weather icon
 * is loaded on a device here (CWD24).
 */
@RunWith(AndroidJUnit4::class)
class WeatherIconDrawablesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `every condition icon loads as a drawable`() {
        for (iconName in WeatherCondition.allIconNames()) {
            val drawable = checkNotNull(context.getDrawable(WeatherIconDrawables.forIconName(iconName))) { iconName }

            assertTrue(iconName, drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
        }
    }

    @Test
    fun `each known icon name has its own drawable and anything else falls back to not available`() {
        assertNotEquals(R.drawable.weather_not_available, WeatherIconDrawables.forIconName("clear-day"))
        assertEquals(R.drawable.weather_not_available, WeatherIconDrawables.forIconName("no-such-icon"))
    }
}
