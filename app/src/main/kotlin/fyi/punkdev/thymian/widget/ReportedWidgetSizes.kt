package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.util.SizeF
import fyi.punkdev.thymian.domain.sizing.WidgetSize

/** Turns a widget's options into the sizes the launcher may show it at. */
object ReportedWidgetSizes {
    fun from(options: Bundle): List<WidgetSize> {
        val exactSizes = options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java)
        if (!exactSizes.isNullOrEmpty()) return exactSizes.map { reportedSize -> WidgetSize(reportedSize.width, reportedSize.height) }
        // A launcher may not report exact sizes; the bounds then give portrait (narrowest, tallest)
        // and landscape (widest, shortest).
        val minimumWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minimumHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maximumWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val maximumHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        if (minimumWidth == 0 && maximumWidth == 0) return emptyList()
        return listOf(
            WidgetSize(minimumWidth.toFloat(), maximumHeight.toFloat()),
            WidgetSize(maximumWidth.toFloat(), minimumHeight.toFloat()),
        )
    }
}
