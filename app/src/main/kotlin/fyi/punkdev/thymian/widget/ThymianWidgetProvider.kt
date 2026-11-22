package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.weather.WeatherRefreshWorker

/**
 * Receives the system's widget callbacks and delegates; it holds no rendering,
 * networking or settings logic (specification 2.2). A locale change re-renders
 * because the date pattern is chosen per locale when rendering, while time, time
 * zone and the device's 12 or 24 hour setting are followed by the clock views
 * themselves.
 */
class ThymianWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetUpdater.renderAll(context, reason = "update", onlyAppWidgetIds = appWidgetIds)
        WeatherRefreshWorker.refreshNow(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        WidgetUpdater.renderAll(context, reason = "resized", onlyAppWidgetIds = intArrayOf(appWidgetId))
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        val settingsStore = CompositionRoot.widgetSettingsStore(context)
        appWidgetIds.forEach { appWidgetId -> settingsStore.delete(appWidgetId) }
        Log.i(LOG_TAG, "event=widget_settings_deleted instance_count=${appWidgetIds.size}")
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_LOCALE_CHANGED) WidgetUpdater.renderAll(context, reason = "locale_changed")
    }

    private companion object {
        const val LOG_TAG = "Thymian"
    }
}
