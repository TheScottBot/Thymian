package fyi.punkdev.thymian.widget

import android.content.Context
import android.provider.AlarmClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.domain.tap.ClockTapTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** The clock tap falls back to the clock app where nothing shows the alarm list (reported on the Xiaomi). */
@RunWith(AndroidJUnit4::class)
class ClockTapResolverTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `a phone with a clock app finds a target for the clock tap`() {
        // The test devices carry the Google clock, which answers show alarms; the manifest must make it visible.
        assertNotEquals(ClockTapTarget.NoTarget, ClockTapResolver.target(context))
    }

    @Test
    fun `the alarm list target asks for the alarm list`() {
        val showAlarms = ClockTapResolver.intentFor(context, ClockTapTarget.ShowAlarms)

        assertEquals(AlarmClock.ACTION_SHOW_ALARMS, showAlarms?.action)
    }

    @Test
    fun `the clock app target opens that app`() {
        val clockPackage = ClockTapTarget.KNOWN_CLOCK_PACKAGES.first { packageName -> isLaunchable(packageName) }

        assertEquals(clockPackage, ClockTapResolver.intentFor(context, ClockTapTarget.ClockApp(clockPackage))?.`package`)
    }

    @Test
    fun `no target gives no intent`() {
        assertNull(ClockTapResolver.intentFor(context, ClockTapTarget.NoTarget))
    }

    private fun isLaunchable(packageName: String): Boolean = context.packageManager.getLaunchIntentForPackage(packageName) != null
}
