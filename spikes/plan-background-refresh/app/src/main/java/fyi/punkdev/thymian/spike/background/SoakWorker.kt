package fyi.punkdev.thymian.spike.background

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Spike only, for CWP7. Records when each periodic run actually happens and the
 * power state around it, which is what decides whether a 30 minute refresh holds
 * up unattended. It does no network work: the question is scheduling, not fetching.
 */
class SoakWorker(appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {

    override fun doWork(): Result {
        val now = Instant.now()
        val preferences = applicationContext.getSharedPreferences(STATE_STORE_NAME, Context.MODE_PRIVATE)
        val previousRunEpochMillis = preferences.getLong(PREVIOUS_RUN_KEY, 0L)
        val minutesSincePrevious = if (previousRunEpochMillis == 0L) {
            "first"
        } else {
            Duration.between(Instant.ofEpochMilli(previousRunEpochMillis), now).toMinutes().toString()
        }
        preferences.edit().putLong(PREVIOUS_RUN_KEY, now.toEpochMilli()).apply()

        val powerManager = applicationContext.getSystemService(PowerManager::class.java)
        val usageStatsManager = applicationContext.getSystemService(UsageStatsManager::class.java)
        val localTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault()).format(now)
        val line = "$localTime gap_minutes=$minutesSincePrevious " +
            "standby_bucket=${describeBucket(usageStatsManager.appStandbyBucket)} " +
            "power_save=${powerManager.isPowerSaveMode} device_idle=${powerManager.isDeviceIdleMode} " +
            "attempt=${runAttemptCount}"
        SoakLog.append(applicationContext, line)
        Log.i(LOG_TAG, "event=soak_run $line")
        SoakWidgetProvider.pushStatus(applicationContext, "Soak run $localTime, gap $minutesSincePrevious min")
        return Result.success()
    }

    private fun describeBucket(bucket: Int): String = when (bucket) {
        UsageStatsManager.STANDBY_BUCKET_ACTIVE -> "active"
        UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> "working_set"
        UsageStatsManager.STANDBY_BUCKET_FREQUENT -> "frequent"
        UsageStatsManager.STANDBY_BUCKET_RARE -> "rare"
        UsageStatsManager.STANDBY_BUCKET_RESTRICTED -> "restricted"
        else -> "other_$bucket"
    }

    companion object {
        const val LOG_TAG = "ThymianSoakSpike"
        const val UNIQUE_WORK_NAME = "thymian_soak"
        private const val STATE_STORE_NAME = "soak_state"
        private const val PREVIOUS_RUN_KEY = "previous_run_epoch_millis"
    }
}
