package fyi.punkdev.thymian.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fyi.punkdev.thymian.domain.weather.RefreshTrigger

/**
 * Receives the weather tap (CWD12). Not exported: a user requested refresh
 * skips the throttle, so only this app's own immutable pending intent may send it.
 */
class WeatherTapReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.i(LOG_TAG, "event=weather_tapped")
        WeatherRefreshWorker.refreshNow(context, RefreshTrigger.USER_REQUESTED)
    }

    private companion object {
        const val LOG_TAG = "Thymian"
    }
}
