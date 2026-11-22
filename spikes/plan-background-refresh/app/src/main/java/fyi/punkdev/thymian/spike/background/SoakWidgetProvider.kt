package fyi.punkdev.thymian.spike.background

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

/** Spike only: shows the latest soak run, so the home screen itself shows whether runs keep happening. */
class SoakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val latestLine = SoakLog.readLines(context).lastOrNull() ?: "Soak: waiting for first run"
        pushStatus(context, latestLine.take(60))
    }

    companion object {
        fun pushStatus(context: Context, statusText: String) {
            val remoteViews = RemoteViews(context.packageName, R.layout.soak_widget)
            remoteViews.setTextViewText(R.id.soak_status, statusText)
            AppWidgetManager.getInstance(context)
                .updateAppWidget(ComponentName(context, SoakWidgetProvider::class.java), remoteViews)
        }
    }
}
