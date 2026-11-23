package fyi.punkdev.thymian.domain.background

import fyi.punkdev.thymian.domain.RefreshPolicy
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticsHistory
import fyi.punkdev.thymian.domain.settings.KeyValueStore
import java.time.Duration
import java.time.Instant

/** What the phone reports about the app's standing for background work, at one moment. */
data class BackgroundState(
    val standbyBucket: String,
    val powerSave: Boolean,
    val deviceIdle: Boolean,
    val backgroundRestricted: Boolean,
    val batteryOptimised: Boolean,
) {
    val detail: String
        get() =
            "bucket=$standbyBucket power_save=$powerSave device_idle=$deviceIdle " +
                "background_restricted=$backgroundRestricted battery_optimised=$batteryOptimised"
}

/** The phone stopped the app's process, with the phone's reason in one word. */
data class ProcessExit(
    val exitedAt: Instant,
    val reason: String,
)

/** Port: what the phone can say about background work. */
interface BackgroundObservations {
    fun currentState(): BackgroundState

    /** The standby buckets the app moved through since [since], oldest first. */
    fun standbyBucketsSince(since: Instant): List<String>

    /** The times the app's process was stopped since [since], oldest first. */
    fun processExitsSince(since: Instant): List<ProcessExit>
}

/**
 * Records, at the start of each periodic refresh, what could explain a refresh
 * that did not come: a long gap since the previous run, with the standby buckets
 * in between; each stop of the app's process; and the background state when it
 * changes. On 22 and 23 November 2026 the Pixel ran no refresh for about 20
 * hours with no reboot, and nothing recorded why. Category words only (datum 0.15).
 */
class BackgroundWatch(
    private val keyValueStore: KeyValueStore,
    private val history: DiagnosticsHistory,
) {
    fun periodicRunStarted(
        startedAt: Instant,
        observations: BackgroundObservations,
    ) {
        val previousRunAt = keyValueStore.readAll()[LAST_PERIODIC_RUN_KEY]?.toLongOrNull()?.let { millis -> Instant.ofEpochMilli(millis) }
        if (previousRunAt != null) {
            observations.processExitsSince(previousRunAt).forEach { exit ->
                history.recordEvent(DiagnosticEvent(exit.exitedAt, PROCESS_EXIT_KIND, "reason=${oneWord(exit.reason)}"))
            }
            val gap = Duration.between(previousRunAt, startedAt)
            if (gap > GAP_THRESHOLD) {
                val buckets = observations.standbyBucketsSince(previousRunAt).map(::oneWord).ifEmpty { listOf("none") }
                history.recordEvent(
                    DiagnosticEvent(startedAt, REFRESH_GAP_KIND, "minutes=${gap.toMinutes()} buckets=${buckets.joinToString(",")}"),
                )
            }
        }
        val stateDetail = observations.currentState().detail
        val lastRecordedState = history.events().lastOrNull { event -> event.kind == BACKGROUND_STATE_KIND }?.detail
        if (stateDetail != lastRecordedState) history.recordEvent(DiagnosticEvent(startedAt, BACKGROUND_STATE_KIND, stateDetail))
        keyValueStore.writeAll(mapOf(LAST_PERIODIC_RUN_KEY to startedAt.toEpochMilli().toString()))
    }

    private fun oneWord(word: String): String = if (ONE_WORD.matches(word)) word else "unknown"

    companion object {
        /** Longer than this between periodic runs is worth explaining; normal runs drift up to about 55 minutes. */
        val GAP_THRESHOLD: Duration = RefreshPolicy.REFRESH_INTERVAL.multipliedBy(3)

        private const val LAST_PERIODIC_RUN_KEY = "background_watch.last_periodic_run"
        private const val PROCESS_EXIT_KIND = "process_exit"
        private const val REFRESH_GAP_KIND = "refresh_gap"
        private const val BACKGROUND_STATE_KIND = "background_state"
        private val ONE_WORD = Regex("[a-z0-9_]{1,40}")
    }
}

/**
 * Words for the numbers Android reports. The domain has no Android, so the
 * numbers are written out; a device test checks them against the platform's.
 */
object BackgroundNames {
    private val STANDBY_BUCKETS =
        mapOf(5 to "exempted", 10 to "active", 20 to "working_set", 30 to "frequent", 40 to "rare", 45 to "restricted", 50 to "never")

    private val EXIT_REASONS =
        mapOf(
            0 to "unknown",
            1 to "exit_self",
            2 to "signaled",
            3 to "low_memory",
            4 to "crash",
            5 to "crash_native",
            6 to "anr",
            7 to "initialization_failure",
            8 to "permission_change",
            9 to "excessive_resource_usage",
            10 to "user_requested",
            11 to "user_stopped",
            12 to "dependency_died",
            13 to "other",
            14 to "freezer",
            15 to "package_state_change",
            16 to "package_updated",
        )

    fun standbyBucket(bucket: Int): String = STANDBY_BUCKETS[bucket] ?: "bucket_$bucket"

    fun exitReason(reason: Int): String = EXIT_REASONS[reason] ?: "reason_$reason"
}
