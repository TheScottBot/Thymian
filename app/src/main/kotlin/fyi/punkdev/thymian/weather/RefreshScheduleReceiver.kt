package fyi.punkdev.thymian.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fyi.punkdev.thymian.domain.weather.RefreshTrigger

/**
 * After a reboot or an app update (specification 1.4, CWE6): makes sure the
 * periodic refresh is scheduled and refreshes once. These broadcasts come only
 * from the system and never as a result of scheduling work, so they cannot form
 * the loop that `onUpdate` did (deviation D10).
 */
class RefreshScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in HANDLED_ACTIONS) return
        Log.i(LOG_TAG, "event=refresh_schedule_ensured reason=${intent.action?.substringAfterLast('.')}")
        WeatherRefreshWorker.ensurePeriodic(context)
        WeatherRefreshWorker.refreshNow(context, RefreshTrigger.AUTOMATIC)
    }

    private companion object {
        const val LOG_TAG = "Thymian"
        val HANDLED_ACTIONS = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)
    }
}
