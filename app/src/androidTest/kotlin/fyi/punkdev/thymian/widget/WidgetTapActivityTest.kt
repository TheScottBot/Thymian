package fyi.punkdev.thymian.widget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.tap.WeatherTapAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/** Both sides of the widget go through this screen, which logs each touch before acting on it. */
@RunWith(AndroidJUnit4::class)
class WidgetTapActivityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    // The tap starts a real refresh, whose own event may land after the tap's, so only tap events are read.
    private fun lastEventAfterTap(action: WeatherTapAction): String {
        ActivityScenario.launch<WidgetTapActivity>(WidgetTapActivity.weatherTapIntent(context, action)).close()
        return CompositionRoot
            .diagnosticsHistory(context)
            .events()
            .last { event -> event.kind in setOf("tapped", "tap_failed") }
            .let { event -> "${event.kind} ${event.detail}" }
    }

    @Test
    fun `the tap screen cannot be reached by other apps`() {
        // A refresh the user asks for skips the throttle, so only this app's own tap may start it.
        val activityInfo =
            context.packageManager.getActivityInfo(
                ComponentName(context, WidgetTapActivity::class.java),
                PackageManager.ComponentInfoFlags.of(0),
            )

        assertFalse(activityInfo.exported)
    }

    @Test
    fun `a weather tap is logged with its action`() {
        assertEquals("tapped side=weather action=refresh", lastEventAfterTap(WeatherTapAction.Refresh))
    }

    @Test
    fun `a weather tap whose app has gone is logged as a failure`() {
        assertEquals(
            "tap_failed side=weather reason=app_not_found",
            lastEventAfterTap(WeatherTapAction.RefreshAndOpen("fyi.punkdev.thymian.no.such.app")),
        )
    }

    @Test
    fun `weather taps for different apps are told apart by the system`() {
        // Pending intents that differ only in extras are merged, so each app must change the intent itself.
        val first = WidgetTapActivity.weatherTapIntent(context, WeatherTapAction.RefreshAndOpen("com.example.first"))
        val second = WidgetTapActivity.weatherTapIntent(context, WeatherTapAction.RefreshAndOpen("com.example.second"))

        assertFalse(first.filterEquals(second))
    }
}
