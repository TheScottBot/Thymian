package fyi.punkdev.thymian.background

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.PowerManager
import fyi.punkdev.thymian.domain.background.BackgroundNames
import fyi.punkdev.thymian.domain.background.BackgroundObservations
import fyi.punkdev.thymian.domain.background.BackgroundState
import fyi.punkdev.thymian.domain.background.ProcessExit
import java.time.Instant

/**
 * Reads the app's own background standing. Every call here is about this app
 * only and needs no permission: the standby bucket and its history through
 * `queryEventsForSelf`, and the process exit history.
 */
class PlatformBackgroundObservations(
    context: Context,
) : BackgroundObservations {
    private val applicationContext = context.applicationContext
    private val usageStatsManager = applicationContext.getSystemService(UsageStatsManager::class.java)
    private val powerManager = applicationContext.getSystemService(PowerManager::class.java)
    private val activityManager = applicationContext.getSystemService(ActivityManager::class.java)
    private val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java)

    override fun currentState(): BackgroundState =
        BackgroundState(
            standbyBucket = BackgroundNames.standbyBucket(usageStatsManager.appStandbyBucket),
            powerSave = powerManager.isPowerSaveMode,
            deviceIdle = powerManager.isDeviceIdleMode,
            backgroundRestricted = activityManager.isBackgroundRestricted,
            batteryOptimised = !powerManager.isIgnoringBatteryOptimizations(applicationContext.packageName),
            networkBlocked = isNetworkBlocked(),
        )

    // The only synchronous view of whether this app is blocked from the active network, which
    // is how background data turned off for the app on mobile data shows (24 November 2026).
    // On screen the limit does not apply, so the answer there says nothing and is left unknown.
    @Suppress("DEPRECATION")
    private fun isNetworkBlocked(): Boolean? {
        val processState = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(processState)
        if (processState.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE) return null
        return connectivityManager.activeNetworkInfo?.detailedState == NetworkInfo.DetailedState.BLOCKED
    }

    override fun standbyBucketsSince(since: Instant): List<String> {
        val events = usageStatsManager.queryEventsForSelf(since.toEpochMilli(), System.currentTimeMillis()) ?: return emptyList()
        val buckets = mutableListOf<String>()
        val event = UsageEvents.Event()
        while (events.getNextEvent(event)) {
            if (event.eventType == UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                val bucket = BackgroundNames.standbyBucket(event.appStandbyBucket)
                if (buckets.lastOrNull() != bucket) buckets += bucket
            }
        }
        return buckets
    }

    override fun processExitsSince(since: Instant): List<ProcessExit> =
        activityManager
            .getHistoricalProcessExitReasons(null, 0, MAXIMUM_EXITS_READ)
            .filter { exitInfo -> exitInfo.timestamp > since.toEpochMilli() }
            .sortedBy { exitInfo -> exitInfo.timestamp }
            .map { exitInfo -> ProcessExit(Instant.ofEpochMilli(exitInfo.timestamp), BackgroundNames.exitReason(exitInfo.reason)) }

    private companion object {
        const val MAXIMUM_EXITS_READ = 16
    }
}
