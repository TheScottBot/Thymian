package fyi.punkdev.thymian.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.WidgetState

/**
 * Re-renders placed widgets from their settings, reported sizes and the shared
 * weather snapshot. Every caller (provider callbacks, the settings screen, the
 * refresh worker) comes through here, so each builds the widget state the same way.
 */
object WidgetUpdater {
    private const val LOG_TAG = "Thymian"

    fun widgetStateFor(
        context: Context,
        appWidgetId: Int,
    ): WidgetState {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        return WidgetState(
            settings = CompositionRoot.widgetSettingsStore(context).load(appWidgetId),
            sizes = ReportedWidgetSizes.from(appWidgetManager.getAppWidgetOptions(appWidgetId)),
            weather = CompositionRoot.weatherSnapshotStore(context).load(),
        )
    }

    fun renderAll(
        context: Context,
        reason: String,
        onlyAppWidgetIds: IntArray? = null,
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = onlyAppWidgetIds ?: appWidgetManager.getAppWidgetIds(ComponentName(context, ThymianWidgetProvider::class.java))
        val locale = context.resources.configuration.locales[0]
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, WidgetRenderer.render(context, widgetStateFor(context, appWidgetId), locale))
        }
        Log.i(LOG_TAG, "event=widgets_rendered reason=$reason instance_count=${appWidgetIds.size}")
    }
}
