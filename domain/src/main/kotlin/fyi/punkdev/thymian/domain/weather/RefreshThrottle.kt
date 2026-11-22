package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.TimeSource
import fyi.punkdev.thymian.domain.settings.KeyValueStore
import java.time.Duration
import java.time.Instant

/** Who asked for a refresh: the schedule or a widget event, or the user directly. */
enum class RefreshTrigger {
    AUTOMATIC,
    USER_REQUESTED,
}

/** Port: when a refresh was last attempted, kept across process restarts. */
interface RefreshAttemptStore {
    fun lastAttempt(): Instant?

    fun recordAttempt(attemptedAt: Instant)
}

class KeyValueRefreshAttemptStore(
    private val keyValueStore: KeyValueStore,
) : RefreshAttemptStore {
    override fun lastAttempt(): Instant? =
        keyValueStore
            .readAll()[LAST_ATTEMPT_KEY]
            ?.takeIf { storedText -> storedText.length <= MAXIMUM_STORED_LENGTH }
            ?.toLongOrNull()
            ?.let { epochMillis -> Instant.ofEpochMilli(epochMillis) }

    override fun recordAttempt(attemptedAt: Instant) {
        keyValueStore.writeAll(mapOf(LAST_ATTEMPT_KEY to attemptedAt.toEpochMilli().toString()))
    }

    private companion object {
        const val LAST_ATTEMPT_KEY = "refresh.last_attempt_epoch_millis"
        const val MAXIMUM_STORED_LENGTH = 20
    }
}

/**
 * Stops any trigger from calling Open-Meteo in a tight loop. On 22 November
 * 2026 a widget update loop refreshed once a second, about 3,600 calls an hour
 * against a free tier of 5,000. Automatic refreshes are refused within
 * [MINIMUM_AUTOMATIC_GAP] of the last attempt, successful or not, so a failing
 * loop is held back too; a refresh the user asked for always runs.
 */
class RefreshThrottle(
    private val attemptStore: RefreshAttemptStore,
    private val timeSource: TimeSource,
) {
    /** Records the attempt and returns true when the refresh may run. */
    fun tryStart(trigger: RefreshTrigger): Boolean {
        val now = timeSource.now()
        val lastAttempt = attemptStore.lastAttempt()
        val withinGap = lastAttempt != null && !lastAttempt.isAfter(now) && Duration.between(lastAttempt, now) < MINIMUM_AUTOMATIC_GAP
        if (trigger == RefreshTrigger.AUTOMATIC && withinGap) return false
        attemptStore.recordAttempt(now)
        return true
    }

    companion object {
        val MINIMUM_AUTOMATIC_GAP: Duration = Duration.ofMinutes(10)
    }
}
