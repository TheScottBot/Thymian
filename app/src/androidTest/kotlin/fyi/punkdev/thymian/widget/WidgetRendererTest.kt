package fyi.punkdev.thymian.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.ClockMode
import fyi.punkdev.thymian.domain.WidgetState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * Applies the renderer's RemoteViews inside the app, the same way a launcher
 * does, and reads back what each clock view was given.
 */
@RunWith(AndroidJUnit4::class)
class WidgetRendererTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun renderAndApply(clockMode: ClockMode): View =
        WidgetRenderer
            .render(context, WidgetState(clockMode = clockMode), Locale.UK)
            .apply(context, FrameLayout(context))

    private fun View.formatsOf(clockViewId: Int): Pair<String, String> {
        val clockView = findViewById<TextClock>(clockViewId)
        return clockView.format12Hour.toString() to clockView.format24Hour.toString()
    }

    @Test
    fun `follow device mode gives the clock views their two slot formats`() {
        val widgetView = renderAndApply(ClockMode.FOLLOW_DEVICE)

        assertEquals("h:mm" to "HH:mm", widgetView.formatsOf(R.id.clock_digits))
        assertEquals("a" to "", widgetView.formatsOf(R.id.clock_suffix))
        val britishDatePattern = LocalizedDatePattern.forLocale(Locale.UK)
        assertEquals(britishDatePattern to britishDatePattern, widgetView.formatsOf(R.id.clock_date))
    }

    @Test
    fun `twelve hour mode forces the suffix into both slots`() {
        val widgetView = renderAndApply(ClockMode.TWELVE_HOUR)

        assertEquals("h:mm" to "h:mm", widgetView.formatsOf(R.id.clock_digits))
        assertEquals("a" to "a", widgetView.formatsOf(R.id.clock_suffix))
    }

    @Test
    fun `twenty four hour mode leaves the suffix empty in both slots`() {
        val widgetView = renderAndApply(ClockMode.TWENTY_FOUR_HOUR)

        assertEquals("HH:mm" to "HH:mm", widgetView.formatsOf(R.id.clock_digits))
        assertEquals("" to "", widgetView.formatsOf(R.id.clock_suffix))
    }

    @Test
    fun `the widget background is fully transparent by default`() {
        val widgetView = renderAndApply(ClockMode.DEFAULT)

        assertEquals(0, (widgetView.background as ColorDrawable).alpha)
    }
}
