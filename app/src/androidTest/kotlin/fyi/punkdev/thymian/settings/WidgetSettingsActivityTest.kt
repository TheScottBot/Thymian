package fyi.punkdev.thymian.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.SeekBar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The settings screen as a launcher opens it. Uses a widget id no launcher will
 * hand out, and deletes its settings afterwards, so placed widgets are untouched.
 */
@RunWith(AndroidJUnit4::class)
class WidgetSettingsActivityTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testWidgetId = 987_654

    @After
    fun removeTestSettings() {
        CompositionRoot.widgetSettingsStore(context).delete(testWidgetId)
    }

    private fun intentFor(appWidgetId: Int?): Intent =
        Intent(context, WidgetSettingsActivity::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
            if (appWidgetId != null) putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

    @Test
    fun `saving stores the changed settings and confirms the widget`() {
        val scenario = ActivityScenario.launchActivityForResult<WidgetSettingsActivity>(intentFor(testWidgetId))

        scenario.onActivity { activity ->
            activity
                .findViewById<android.view.View>(R.id.settings_controls)
                .findViewWithTag<android.view.View>(SettingDeclarations.BACKGROUND_OPACITY_PERCENT.key)
                .let { control -> (control as android.view.ViewGroup).getChildAt(1) as SeekBar }
                .progress = 30
            activity.findViewById<android.view.View>(R.id.settings_save).performClick()
        }

        assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        assertEquals(testWidgetId, scenario.result.resultData.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        assertEquals(30, CompositionRoot.widgetSettingsStore(context).load(testWidgetId)[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
    }

    @Test
    fun `opening without a widget id finishes without confirming anything`() {
        val scenario = ActivityScenario.launchActivityForResult<WidgetSettingsActivity>(intentFor(appWidgetId = null))

        assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
    }
}
