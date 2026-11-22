package fyi.punkdev.thymian.frontdoor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.frontdoor.FrontDoorDestination
import fyi.punkdev.thymian.settings.WidgetSettingsActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The app's front door: a home screen icon, so the app can be opened once
 * without a widget. HyperOS hides widgets of apps that have never been opened,
 * and with no icon there was no way to open Thymian.
 */
@RunWith(AndroidJUnit4::class)
class FrontDoorActivityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `the home screen icon opens the front door`() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        assertEquals(FrontDoorActivity::class.java.name, launchIntent?.component?.className)
    }

    @Test
    fun `opening settings for a widget names that widget`() {
        val settingsIntent = FrontDoorActivity.intentFor(context, FrontDoorDestination.WidgetSettings(55))

        assertEquals(WidgetSettingsActivity::class.java.name, settingsIntent.component?.className)
        assertEquals(55, settingsIntent.getIntExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        assertTrue(settingsIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK == 0)
    }

    @Test
    fun `the add widget screen explains itself and offers the add button`() {
        val addWidgetView = LayoutInflater.from(context).inflate(R.layout.activity_front_door, FrameLayout(context), false)

        assertFalse(addWidgetView.findViewById<TextView>(R.id.front_door_explanation).text.isNullOrBlank())
        assertEquals(
            context.getString(R.string.front_door_add_widget),
            addWidgetView.findViewById<Button>(R.id.front_door_add_widget).text.toString(),
        )
    }
}
