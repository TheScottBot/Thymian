package fyi.punkdev.thymian.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.tap.ClockTapAttempts
import fyi.punkdev.thymian.domain.tap.ClockTapTarget
import fyi.punkdev.thymian.domain.tap.TapEvents
import fyi.punkdev.thymian.domain.tap.TapSide
import fyi.punkdev.thymian.domain.tap.WeatherTapAction
import fyi.punkdev.thymian.domain.weather.RefreshTrigger
import fyi.punkdev.thymian.weather.WeatherRefreshWorker
import java.time.Instant

/**
 * Both sides of the widget open this screen, which shows nothing. It logs the
 * touch to diagnostics, acts on it, and logs any failure, so a report shows
 * whether a tap reached the app and what came of it. Being an activity started
 * by the home screen, it also avoids HyperOS dropping a broadcast to an app
 * that is not running. Not exported: a refresh the user asks for skips the
 * throttle, so only this app's own pending intents may start it.
 */
class WidgetTapActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent?.action) {
            CLOCK_TAP_ACTION -> handleClockTap()
            WEATHER_TAP_ACTION -> handleWeatherTap()
            else -> Log.w(LOG_TAG, "event=tap_unrecognised")
        }
        // Theme.NoDisplay requires finishing before the screen would be shown.
        finish()
    }

    private fun handleClockTap() {
        // Resolved at the moment of the tap, so a clock app installed or removed since the last render is honoured.
        // On the Xiaomi the alarm list resolved but refused with a SecurityException, so each refusal moves on.
        // An app chosen in settings goes first; if it cannot open, the phone's own targets follow.
        val chosenApp = intent.getStringExtra(APP_PACKAGE_EXTRA)?.let { packageName -> ClockTapTarget.ChosenApp(packageName) }
        val candidates = listOfNotNull(chosenApp) + ClockTapResolver.candidates(this)
        ClockTapAttempts.attempt(Instant.now(), candidates, ::record) { target ->
            val openClock = ClockTapResolver.intentFor(this, target)
            if (openClock == null) "app_not_found" else startFailureReason(openClock)
        }
    }

    private fun handleWeatherTap() {
        val chosenPackage = intent.getStringExtra(APP_PACKAGE_EXTRA)
        val action = chosenPackage?.let { packageName -> WeatherTapAction.RefreshAndOpen(packageName) } ?: WeatherTapAction.Refresh
        record(TapEvents.weatherTapped(Instant.now(), action))
        WeatherRefreshWorker.refreshNow(this, RefreshTrigger.USER_REQUESTED)
        if (action !is WeatherTapAction.RefreshAndOpen) return
        val openApp = packageManager.getLaunchIntentForPackage(action.packageName)
        if (openApp == null) {
            record(TapEvents.tapFailed(Instant.now(), TapSide.WEATHER, "app_not_found"))
            return
        }
        startOrRecordFailure(openApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), TapSide.WEATHER)
    }

    private fun startOrRecordFailure(
        openIntent: Intent,
        side: TapSide,
    ) {
        startFailureReason(openIntent)?.let { reason -> record(TapEvents.tapFailed(Instant.now(), side, reason)) }
    }

    /** Null when the screen started; otherwise the exception's class name, which is all that is kept. */
    private fun startFailureReason(openIntent: Intent): String? =
        try {
            startActivity(openIntent)
            null
        } catch (startFailure: RuntimeException) {
            // ActivityNotFoundException or SecurityException.
            startFailure::class.java.simpleName
        }

    private fun record(event: DiagnosticEvent) {
        Log.i(LOG_TAG, "event=${event.kind} ${event.detail}")
        CompositionRoot.diagnosticsHistory(this).recordEvent(event)
    }

    companion object {
        private const val LOG_TAG = "Thymian"
        private const val CLOCK_TAP_ACTION = "fyi.punkdev.thymian.action.CLOCK_TAP"
        private const val WEATHER_TAP_ACTION = "fyi.punkdev.thymian.action.WEATHER_TAP"
        private const val APP_PACKAGE_EXTRA = "fyi.punkdev.thymian.extra.APP_PACKAGE"

        /** As for the weather, a chosen app is also written into the intent's data so the system keeps it apart. */
        fun clockTapIntent(
            context: Context,
            chosenApp: ClockTapTarget.ChosenApp?,
        ): Intent {
            val tapIntent = Intent(context, WidgetTapActivity::class.java).setAction(CLOCK_TAP_ACTION)
            chosenApp?.let { app ->
                tapIntent
                    .setData(Uri.fromParts("thymian-tap", app.packageName, null))
                    .putExtra(APP_PACKAGE_EXTRA, app.packageName)
            }
            return tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        /**
         * The chosen app is also written into the intent's data, because the system
         * merges pending intents that differ only in extras.
         */
        fun weatherTapIntent(
            context: Context,
            action: WeatherTapAction,
        ): Intent {
            val tapIntent = Intent(context, WidgetTapActivity::class.java).setAction(WEATHER_TAP_ACTION)
            return when (action) {
                WeatherTapAction.Refresh -> {
                    tapIntent
                }

                is WeatherTapAction.RefreshAndOpen -> {
                    tapIntent
                        .setData(Uri.fromParts("thymian-tap", action.packageName, null))
                        .putExtra(APP_PACKAGE_EXTRA, action.packageName)
                }
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
