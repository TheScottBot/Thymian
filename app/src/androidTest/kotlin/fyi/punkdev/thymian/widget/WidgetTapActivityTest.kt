package fyi.punkdev.thymian.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.tap.ClockTapTarget
import fyi.punkdev.thymian.domain.tap.WeatherTapAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** Both sides of the widget go through this screen, which logs each touch before acting on it. */
@RunWith(AndroidJUnit4::class)
class WidgetTapActivityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    // The tap starts a real refresh, whose own event may land after the tap's, so only tap events are read.
    private fun lastEventAfterTap(action: WeatherTapAction): String =
        tapEventsAfter(WidgetTapActivity.weatherTapIntent(context, action)).last()

    private fun tapEventsAfter(tapIntent: Intent): List<String> {
        // By time, not by count: once the history is full, its length stops changing.
        val tappedFrom = Instant.now()
        ActivityScenario.launch<WidgetTapActivity>(tapIntent).close()
        return CompositionRoot
            .diagnosticsHistory(context)
            .events()
            .filter { event -> !event.occurredAt.isBefore(tappedFrom) }
            .filter { event -> event.kind in setOf("tapped", "tap_failed", "tap_fallback") }
            .map { event -> "${event.kind} ${event.detail}" }
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

    @Test
    fun `a clock tap whose chosen app has gone is logged and falls back`() {
        val tapEvents =
            tapEventsAfter(WidgetTapActivity.clockTapIntent(context, ClockTapTarget.ChosenApp("fyi.punkdev.thymian.no.such.app")))

        assertEquals(
            listOf("tapped side=clock target=chosen_app", "tap_failed side=clock reason=app_not_found"),
            tapEvents.take(2),
        )
        assertTrue(tapEvents[2].startsWith("tap_fallback side=clock "))
    }

    @Test
    fun `clock taps with and without a chosen app are told apart by the system`() {
        val withoutChoice = WidgetTapActivity.clockTapIntent(context, chosenApp = null)
        val withChoice = WidgetTapActivity.clockTapIntent(context, ClockTapTarget.ChosenApp("com.example.clock"))

        assertFalse(withoutChoice.filterEquals(withChoice))
    }
}
