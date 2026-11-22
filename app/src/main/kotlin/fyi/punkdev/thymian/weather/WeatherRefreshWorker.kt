package fyi.punkdev.thymian.weather

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import fyi.punkdev.thymian.composition.CompositionRoot
import fyi.punkdev.thymian.domain.RefreshPolicy
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.weather.RefreshOutcome
import fyi.punkdev.thymian.domain.weather.RefreshTrigger
import fyi.punkdev.thymian.widget.WidgetUpdater
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Runs one weather refresh off the main thread, records the outcome in the
 * diagnostics history and re-renders every widget, unless the refresh throttle
 * refuses it. Scheduled every 30 minutes (CWD7) from the settings screen and
 * after a reboot or update, never from widget callbacks (deviation D10).
 */
class WeatherRefreshWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : Worker(appContext, workerParameters) {
    override fun doWork(): Result {
        val trigger =
            RefreshTrigger.entries.firstOrNull { entry -> entry.name == inputData.getString(TRIGGER_KEY) } ?: RefreshTrigger.AUTOMATIC
        val history = CompositionRoot.diagnosticsHistory(applicationContext)
        if (!CompositionRoot.refreshThrottle(applicationContext).tryStart(trigger)) {
            Log.i(LOG_TAG, "event=weather_refresh outcome=throttled trigger=$trigger")
            history.recordEvent(DiagnosticEvent(Instant.now(), "throttled", "trigger=$trigger"))
            return Result.success()
        }
        val outcome = CompositionRoot.refreshWeather(applicationContext).refresh(CompositionRoot.locationPreference(applicationContext))
        Log.i(LOG_TAG, "event=weather_refresh trigger=$trigger ${describe(outcome)}")
        history.record(Instant.now(), outcome)
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
        private const val PERIODIC_WORK_NAME = "weather_refresh_periodic"
        private const val TRIGGER_KEY = "trigger"

        /**
         * Schedules the 30 minute refresh (CWD7) with no network constraint: offline, the
         * attempt fails fast, is recorded and re-renders, so the stale symbol and diagnostics
         * stay truthful (CWD39, deviation D11). `KEEP` leaves
         * an existing schedule untouched, so calling this again never shifts its timing.
         */
        fun ensurePeriodic(context: Context) {
            val request =
                PeriodicWorkRequest
                    .Builder(WeatherRefreshWorker::class.java, RefreshPolicy.REFRESH_INTERVAL.toMinutes(), TimeUnit.MINUTES)
                    .setInputData(Data.Builder().putString(TRIGGER_KEY, RefreshTrigger.AUTOMATIC.name).build())
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** Queues one refresh; a request already queued is kept, so repeated triggers do not pile up. */
        fun refreshNow(
            context: Context,
            trigger: RefreshTrigger,
        ) {
            val request =
                OneTimeWorkRequest
                    .Builder(WeatherRefreshWorker::class.java)
                    .setInputData(Data.Builder().putString(TRIGGER_KEY, trigger.name).build())
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(REFRESH_NOW_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
