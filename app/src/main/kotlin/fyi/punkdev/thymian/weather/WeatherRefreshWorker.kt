package fyi.punkdev.thymian.weather

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.weather.RefreshOutcome
import fyi.punkdev.thymian.widget.WidgetUpdater

/**
 * Runs one weather refresh off the main thread and re-renders every widget.
 * The periodic 30 minute schedule arrives in CWE6; for now a refresh runs when
 * a widget is placed or its settings are saved.
 */
class WeatherRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        val outcome = CompositionRoot.refreshWeather(applicationContext).refresh(CompositionRoot.locationPreference(applicationContext))
        Log.i(LOG_TAG, "event=weather_refresh ${describe(outcome)}")
        WidgetUpdater.renderAll(applicationContext, reason = "weather_refresh")
        return Result.success()
    }

    private fun describe(outcome: RefreshOutcome): String =
        when (outcome) {
            is RefreshOutcome.Refreshed -> {
                "outcome=refreshed location_source=${outcome.snapshot.locationSource} " +
                    "condition=${outcome.snapshot.currentWeather.condition} place_named=${outcome.snapshot.placeName != null}"
            }

            is RefreshOutcome.LocationUnavailable -> {
                "outcome=location_unavailable reason=${outcome.reason}"
            }

            is RefreshOutcome.WeatherUnavailable -> {
                "outcome=weather_unavailable failure=${outcome.failure} location_source=${outcome.locationSource}"
            }
        }

    companion object {
        private const val LOG_TAG = "Thymian"
        private const val REFRESH_NOW_WORK_NAME = "weather_refresh_now"

        /** Queues one refresh; a request already queued is kept, so repeated triggers do not pile up. */
        fun refreshNow(context: Context) {
            val request =
                OneTimeWorkRequest
                    .Builder(WeatherRefreshWorker::class.java)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(REFRESH_NOW_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
