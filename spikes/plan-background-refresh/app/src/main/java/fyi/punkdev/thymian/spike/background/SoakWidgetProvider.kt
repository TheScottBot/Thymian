package fyi.punkdev.thymian.spike.background

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews

/** Spike only: shows the latest soak run, so the home screen itself shows whether runs keep happening. */
class SoakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val latestLine = SoakLog.readLines(context).lastOrNull() ?: "Soak: waiting for first run"
        pushStatus(context, latestLine.take(60))
        for (appWidgetId in appWidgetIds) {
            logReportedSizes(appWidgetManager.getAppWidgetOptions(appWidgetId))
        }
    }

    // Added for CWP4: logs the sizes the launcher reports, to ground the size bucket thresholds.
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        logReportedSizes(newOptions)
    }

    companion object {
        fun logReportedSizes(options: Bundle) {
            val minimumWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minimumHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            val maximumWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val maximumHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val exactSizes = options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java)
            Log.i(
                SoakWorker.LOG_TAG,
                "event=widget_sizes min_dp=${minimumWidth}x$minimumHeight max_dp=${maximumWidth}x$maximumHeight " +
                    "exact_sizes_dp=${exactSizes?.joinToString { reportedSize -> "${reportedSize.width}x${reportedSize.height}" }}",
            )
        }

        fun pushStatus(context: Context, statusText: String) {
            val remoteViews = RemoteViews(context.packageName, R.layout.soak_widget)
            remoteViews.setTextViewText(R.id.soak_status, statusText)
            AppWidgetManager.getInstance(context)
                .updateAppWidget(ComponentName(context, SoakWidgetProvider::class.java), remoteViews)
        }
    }
}
