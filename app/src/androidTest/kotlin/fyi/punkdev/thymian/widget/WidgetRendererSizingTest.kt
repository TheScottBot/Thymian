package fyi.punkdev.thymian.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.sizing.SizeLayoutPolicy
import fyi.punkdev.thymian.domain.sizing.WidgetSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** One layout per reported size, with text scaled for that size (specification 2.4). */
@RunWith(AndroidJUnit4::class)
class WidgetRendererSizingTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun applyForSize(size: WidgetSize): View =
        WidgetRenderer
            .renderForSize(context, WidgetState(WidgetSettings.defaults(), listOf(size)), Locale.UK, size)
            .apply(context, FrameLayout(context))

    private fun dpToPixels(dp: Int): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)

    @Test
    fun `the small layout holds the clock only`() {
        val widgetView = applyForSize(WidgetSize(130f, 94f))

        assertNotNull(widgetView.findViewById(R.id.clock_block))
        assertNull(widgetView.findViewById(R.id.weather_block))
    }

    @Test
    fun `the wide and tall layouts include the weather block`() {
        assertNotNull(applyForSize(WidgetSize(360f, 80f)).findViewById(R.id.weather_block))
        assertNotNull(applyForSize(WidgetSize(200f, 250f)).findViewById(R.id.weather_block))
    }

    @Test
    fun `clock text is sized by the size layout policy`() {
        val size = WidgetSize(276.6f, 94.1f)
        val expected = SizeLayoutPolicy.layoutFor(size).clock
        val widgetView = applyForSize(size)

        assertEquals(dpToPixels(expected.digitsDp), widgetView.findViewById<TextClock>(R.id.clock_digits).textSize, 0.5f)
        assertEquals(dpToPixels(expected.dateDp), widgetView.findViewById<TextClock>(R.id.clock_date).textSize, 0.5f)
        assertEquals(dpToPixels(expected.suffixDp), widgetView.findViewById<TextClock>(R.id.clock_suffix).textSize, 0.5f)
    }

    @Test
    fun `the date is hidden when it does not fit even though the setting shows it`() {
        val widgetView = applyForSize(WidgetSize(276.6f, 40f))

        assertEquals(View.GONE, widgetView.findViewById<TextClock>(R.id.clock_date).visibility)
    }

    @Test
    fun `several reported sizes render into one responsive tree that applies`() {
        val sizes = listOf(WidgetSize(276.6f, 94.1f), WidgetSize(587.8f, 48.4f))

        val widgetView =
            WidgetRenderer
                .render(context, WidgetState(WidgetSettings.defaults(), sizes), Locale.UK)
                .apply(context, FrameLayout(context))

        assertNotNull(widgetView.findViewById(R.id.clock_digits))
    }
}
