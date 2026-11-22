package fyi.punkdev.thymian.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.settings.WidgetSettingsStore
import fyi.punkdev.thymian.widget.WidgetRenderer

/**
 * The settings screen for one widget instance, opened by the launcher when the
 * widget is first added and again to reconfigure it (specification 1.3). The
 * preview applies the renderer's own output, so it shows exactly what the
 * widget will (specification 2.2).
 */
class WidgetSettingsActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var settingsStore: WidgetSettingsStore
    private lateinit var currentSettings: WidgetSettings
    private lateinit var previewContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Backing out of the first add must not leave a widget behind.
        setResult(RESULT_CANCELED)
        appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.w(LOG_TAG, "event=settings_opened_without_widget outcome=finished")
            finish()
            return
        }

        setContentView(R.layout.activity_widget_settings)
        keepClearOfSystemBars(findViewById(R.id.settings_scroll))
        settingsStore = CompositionRoot.widgetSettingsStore(this)
        currentSettings = settingsStore.load(appWidgetId)
        previewContainer = findViewById(R.id.settings_preview)

        val controlsContainer = findViewById<LinearLayout>(R.id.settings_controls)
        SettingControls(this) { updatedSettings ->
            currentSettings = updatedSettings
            refreshPreview()
        }.build(currentSettings).forEach { control -> controlsContainer.addView(control) }
        findViewById<View>(R.id.settings_save).setOnClickListener { saveAndFinish() }
        refreshPreview()
    }

    private fun refreshPreview() {
        previewContainer.removeAllViews()
        previewContainer.addView(renderedWidget().apply(this, previewContainer))
    }

    private fun saveAndFinish() {
        settingsStore.save(appWidgetId, currentSettings)
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId, renderedWidget())
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        Log.i(LOG_TAG, "event=widget_settings_saved")
        finish()
    }

    private fun renderedWidget() = WidgetRenderer.render(this, WidgetState(currentSettings), resources.configuration.locales[0])

    private fun keepClearOfSystemBars(scrollingContent: View) {
        // targetSdk 37 draws edge to edge, so the screen pads itself clear of the bars.
        scrollingContent.setOnApplyWindowInsetsListener { insetView, windowInsets ->
            val barInsets = windowInsets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
            insetView.setPadding(barInsets.left, barInsets.top, barInsets.right, barInsets.bottom)
            windowInsets
        }
    }

    private companion object {
        const val LOG_TAG = "Thymian"
    }
}
