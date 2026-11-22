package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.SizeF
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.domain.sizing.WidgetSize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Reads the sizes a launcher reports for a widget (specification 2.4). */
@RunWith(AndroidJUnit4::class)
class ReportedWidgetSizesTest {
    @Test
    fun `exact sizes are used when the launcher reports them`() {
        val options =
            Bundle().apply {
                putParcelableArrayList(
                    AppWidgetManager.OPTION_APPWIDGET_SIZES,
                    arrayListOf(SizeF(276.6f, 94.1f), SizeF(587.8f, 48.4f)),
                )
            }

        assertEquals(listOf(WidgetSize(276.6f, 94.1f), WidgetSize(587.8f, 48.4f)), ReportedWidgetSizes.from(options))
    }

    @Test
    fun `without exact sizes the portrait and landscape bounds stand in`() {
        // The platform warns a launcher may not report exact sizes; minimum width with maximum
        // height is the portrait size, and the reverse is landscape.
        val options =
            Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 276)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 48)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 587)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 94)
            }

        assertEquals(listOf(WidgetSize(276f, 94f), WidgetSize(587f, 48f)), ReportedWidgetSizes.from(options))
    }

    @Test
    fun `no size information gives no sizes`() {
        assertEquals(emptyList<WidgetSize>(), ReportedWidgetSizes.from(Bundle()))
    }
}
