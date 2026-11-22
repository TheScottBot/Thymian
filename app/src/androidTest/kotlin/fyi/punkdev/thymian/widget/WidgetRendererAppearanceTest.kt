package fyi.punkdev.thymian.widget

import android.content.Context
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** The renderer draws a widget's appearance settings (specification 1.3). */
@RunWith(AndroidJUnit4::class)
class WidgetRendererAppearanceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun renderAndApply(settings: WidgetSettings): View {
        val widgetView =
            WidgetRenderer
                .render(context, WidgetState(settings), Locale.UK)
                .apply(context, FrameLayout(context))
        // Lay out at a fixed size so the outline has bounds to read.
        widgetView.measure(
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
        )
        widgetView.layout(0, 0, 800, 300)
        return widgetView
    }

    @Test
    fun `background colour and opacity become the root background`() {
        val settings =
            WidgetSettings
                .defaults()
                .with(SettingDeclarations.BACKGROUND_COLOUR, 0x203040)
                .with(SettingDeclarations.BACKGROUND_OPACITY_PERCENT, 100)

        val widgetRoot = renderAndApply(settings)

        assertEquals(0xFF203040.toInt(), (widgetRoot.background as ColorDrawable).color)
    }

    @Test
    fun `corner radius clips the root to a rounded outline`() {
        val settings = WidgetSettings.defaults().with(SettingDeclarations.CORNER_RADIUS_DP, 20)
        val widgetRoot = renderAndApply(settings)
        val outline = Outline()

        widgetRoot.outlineProvider.getOutline(widgetRoot, outline)

        assertTrue(widgetRoot.clipToOutline)
        assertEquals(20 * context.resources.displayMetrics.density, outline.radius, 0.5f)
    }

    @Test
    fun `text colour reaches every clock view`() {
        val widgetRoot = renderAndApply(WidgetSettings.defaults().with(SettingDeclarations.TEXT_COLOUR, 0x00FF00))

        for (clockViewId in listOf(R.id.clock_date, R.id.clock_digits, R.id.clock_suffix)) {
            assertEquals(0xFF00FF00.toInt(), widgetRoot.findViewById<TextClock>(clockViewId).currentTextColor)
        }
    }

    @Test
    fun `hiding the date removes it from the layout`() {
        val widgetRoot = renderAndApply(WidgetSettings.defaults().with(SettingDeclarations.SHOW_DATE, false))

        assertEquals(View.GONE, widgetRoot.findViewById<TextClock>(R.id.clock_date).visibility)
    }

    @Test
    fun `the date shows by default`() {
        val widgetRoot = renderAndApply(WidgetSettings.defaults())

        assertEquals(View.VISIBLE, widgetRoot.findViewById<TextClock>(R.id.clock_date).visibility)
    }
}
