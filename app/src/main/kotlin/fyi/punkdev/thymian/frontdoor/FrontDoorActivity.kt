package fyi.punkdev.thymian.frontdoor

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.frontdoor.FrontDoorDestination
import fyi.punkdev.thymian.settings.WidgetSettingsActivity
import fyi.punkdev.thymian.widget.ThymianWidgetProvider

/**
 * The home screen icon. HyperOS hides the widgets of an app that has never been
 * opened, and a widget only app had no way to be opened, so this gives it one.
 * With a widget placed it opens that widget's settings; with none it explains
 * how to add one and, where the launcher allows, adds it directly.
 */
class FrontDoorActivity : Activity() {
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var widgetProvider: ComponentName

    // The widgets placed when the add request was made, so the new one can be found on return.
    private var placedBeforeAddRequest: List<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetManager = AppWidgetManager.getInstance(this)
        widgetProvider = ComponentName(this, ThymianWidgetProvider::class.java)
        placedBeforeAddRequest = savedInstanceState?.getIntArray(PLACED_BEFORE_KEY)?.toList()

        when (val destination = FrontDoorDestination.choose(placedWidgetIds())) {
            is FrontDoorDestination.WidgetSettings -> openAndFinish(destination)
            FrontDoorDestination.AddWidget -> showAddWidget()
        }
    }

    override fun onResume() {
        super.onResume()
        // The launcher's add dialog sits over this screen, so a widget added from it shows up here.
        val placedBefore = placedBeforeAddRequest ?: return
        FrontDoorDestination.newlyPlacedWidget(placedBefore, placedWidgetIds())?.let { newWidgetId ->
            Log.i(LOG_TAG, "event=front_door_widget_added")
            openAndFinish(FrontDoorDestination.WidgetSettings(newWidgetId))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        placedBeforeAddRequest?.let { placedBefore -> outState.putIntArray(PLACED_BEFORE_KEY, placedBefore.toIntArray()) }
    }

    private fun placedWidgetIds(): List<Int> = appWidgetManager.getAppWidgetIds(widgetProvider).toList()

    private fun openAndFinish(destination: FrontDoorDestination) {
        Log.i(LOG_TAG, "event=front_door_opened destination=settings")
        startActivity(intentFor(this, destination))
        finish()
    }

    private fun showAddWidget() {
        Log.i(LOG_TAG, "event=front_door_opened destination=add_widget")
        setContentView(R.layout.activity_front_door)
        keepClearOfSystemBars(findViewById(R.id.front_door_scroll))
        val addWidgetButton = findViewById<View>(R.id.front_door_add_widget)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            addWidgetButton.setOnClickListener {
                placedBeforeAddRequest = placedWidgetIds()
                val requestAccepted = appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
                Log.i(LOG_TAG, "event=front_door_add_requested accepted=$requestAccepted")
            }
        } else {
            // Some launchers cannot add a widget for an app; the explanation still says how to do it by hand.
            addWidgetButton.visibility = View.GONE
        }
    }

    private fun keepClearOfSystemBars(scrollingContent: View) {
        scrollingContent.setOnApplyWindowInsetsListener { insetView, windowInsets ->
            val barInsets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            insetView.setPadding(barInsets.left, barInsets.top, barInsets.right, barInsets.bottom)
            windowInsets
        }
    }

    companion object {
        private const val LOG_TAG = "Thymian"
        private const val PLACED_BEFORE_KEY = "placed_before_add_request"

        /** The screen [destination] leads to, started within the front door's own task. */
        fun intentFor(
            context: Context,
            destination: FrontDoorDestination,
        ): Intent =
            when (destination) {
                is FrontDoorDestination.WidgetSettings -> {
                    Intent(context, WidgetSettingsActivity::class.java)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, destination.appWidgetId)
                }

                FrontDoorDestination.AddWidget -> {
                    Intent(context, FrontDoorActivity::class.java)
                }
            }
    }
}
