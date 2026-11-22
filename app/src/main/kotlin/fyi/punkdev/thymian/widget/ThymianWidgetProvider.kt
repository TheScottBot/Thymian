package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.WidgetState

/**
 * Receives the system's widget callbacks and delegates to the renderer; it holds
 * no rendering, networking or settings logic (specification 2.2). A locale change
 * re-renders because the date pattern is chosen per locale when rendering, while
 * time, time zone and the device's 12 or 24 hour setting are followed by the
 * clock views themselves.
 */
class ThymianWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        renderAll(context, appWidgetManager, appWidgetIds, reason = "update")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        renderAll(context, appWidgetManager, intArrayOf(appWidgetId), reason = "resized")
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
        if (intent.action == Intent.ACTION_LOCALE_CHANGED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, ThymianWidgetProvider::class.java))
            renderAll(context, appWidgetManager, appWidgetIds, reason = "locale_changed")
        }
    }

    private fun renderAll(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        reason: String,
    ) {
        val locale = context.resources.configuration.locales[0]
        val settingsStore = CompositionRoot.widgetSettingsStore(context)
        for (appWidgetId in appWidgetIds) {
            val reportedSizes = ReportedWidgetSizes.from(appWidgetManager.getAppWidgetOptions(appWidgetId))
            val widgetState = WidgetState(settingsStore.load(appWidgetId), reportedSizes)
            appWidgetManager.updateAppWidget(appWidgetId, WidgetRenderer.render(context, widgetState, locale))
        }
        Log.i(LOG_TAG, "event=widgets_rendered reason=$reason instance_count=${appWidgetIds.size}")
    }

    private companion object {
        const val LOG_TAG = "Thymian"
    }
}
