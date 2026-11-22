package fyi.punkdev.thymian.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.settings.WidgetSettingsStore
import fyi.punkdev.thymian.domain.weather.RefreshTrigger
import fyi.punkdev.thymian.weather.WeatherRefreshWorker
import fyi.punkdev.thymian.widget.WidgetRenderer
import fyi.punkdev.thymian.widget.WidgetUpdater

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
    private lateinit var locationSection: LocationSettingsSection
    private val searchExecutor =
        java.util.concurrent.Executors
            .newSingleThreadExecutor()

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

        findViewById<TextView>(R.id.settings_attribution).apply {
            text = Html.fromHtml(getString(R.string.settings_attribution), Html.FROM_HTML_MODE_COMPACT)
            movementMethod = LinkMovementMethod.getInstance()
        }
        locationSection =
            LocationSettingsSection(
                context = this,
                initialPreference = CompositionRoot.locationPreference(this),
                placeSearch = CompositionRoot.placeSearch(this),
                backgroundExecutor = searchExecutor,
                mainExecutor = mainExecutor,
            ) { permission -> requestPermissions(arrayOf(permission), LOCATION_PERMISSION_REQUEST) }
        findViewById<LinearLayout>(R.id.settings_location).addView(locationSection.view)

        val controlsContainer = findViewById<LinearLayout>(R.id.settings_controls)
        SettingControls(this) { updatedSettings ->
            currentSettings = updatedSettings
            refreshPreview()
        }.build(currentSettings).forEach { control -> controlsContainer.addView(control) }
        findViewById<View>(R.id.settings_save).setOnClickListener { saveAndFinish() }
        refreshPreview()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) locationSection.refreshPermissionState()
    }

    override fun onDestroy() {
        searchExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun refreshPreview() {
        // The preview shows the widget at its first reported size, so its layout and text sizes match.
        val previewState = currentWidgetState()
        val previewSize = previewState.sizes.firstOrNull() ?: WidgetRenderer.DEFAULT_SIZE
        val previewViews = WidgetRenderer.renderForSize(this, previewState, currentLocale(), previewSize)
        previewContainer.removeAllViews()
        previewContainer.addView(previewViews.apply(this, previewContainer))
    }

    private fun saveAndFinish() {
        settingsStore.save(appWidgetId, currentSettings)
        // Location is one choice for the whole app (CWD36), saved from whichever widget's screen is open.
        CompositionRoot.locationPreferenceStore(this).save(locationSection.preference())
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId, renderedWidget())
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        Log.i(LOG_TAG, "event=widget_settings_saved")
        WeatherRefreshWorker.ensurePeriodic(this)
        WeatherRefreshWorker.refreshNow(this, RefreshTrigger.USER_REQUESTED)
        finish()
    }

    private fun renderedWidget() = WidgetRenderer.render(this, currentWidgetState(), currentLocale())

    // The saved state for this widget, with the settings being edited in place of its stored ones.
    private fun currentWidgetState() = WidgetUpdater.widgetStateFor(this, appWidgetId).copy(settings = currentSettings)

    private fun currentLocale() = resources.configuration.locales[0]

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
        const val LOCATION_PERMISSION_REQUEST = 1
    }
}
