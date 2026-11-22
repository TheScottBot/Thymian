package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

/**
 * A guard so no trigger can call Open-Meteo in a tight loop: on 22 November
 * 2026 a widget update loop refreshed once a second. Automatic triggers are
 * refused within the minimum gap of the last attempt, successful or not; a
 * refresh the user asked for always runs.
 */
class RefreshThrottleTest {
    private var now = Instant.parse("2026-11-22T12:00:00Z")
    private val throttle = RefreshThrottle(KeyValueRefreshAttemptStore(InMemoryKeyValueStore())) { now }

    @Test
    fun `the first automatic refresh runs`() {
        assertEquals(true, throttle.tryStart(RefreshTrigger.AUTOMATIC))
    }

    @Test
    fun `a second automatic refresh within the gap is refused`() {
        throttle.tryStart(RefreshTrigger.AUTOMATIC)
        now = now.plus(RefreshThrottle.MINIMUM_AUTOMATIC_GAP).minusSeconds(1)

        assertEquals(false, throttle.tryStart(RefreshTrigger.AUTOMATIC))
    }

    @Test
    fun `an automatic refresh after the gap runs`() {
        throttle.tryStart(RefreshTrigger.AUTOMATIC)
        now = now.plus(RefreshThrottle.MINIMUM_AUTOMATIC_GAP)

        assertEquals(true, throttle.tryStart(RefreshTrigger.AUTOMATIC))
    }

    @Test
    fun `a refresh the user asked for always runs and counts as an attempt`() {
        throttle.tryStart(RefreshTrigger.AUTOMATIC)

        assertEquals(true, throttle.tryStart(RefreshTrigger.USER_REQUESTED))
        now = now.plusSeconds(1)
        assertEquals(false, throttle.tryStart(RefreshTrigger.AUTOMATIC))
    }

    @Test
    fun `a stored attempt in the future, from a clock change, does not block refreshing`() {
        throttle.tryStart(RefreshTrigger.AUTOMATIC)
        now = now.minus(Duration.ofHours(5))

        assertEquals(true, throttle.tryStart(RefreshTrigger.AUTOMATIC))
    }

    @Test
    fun `the gap is well inside the free tier and the 30 minute schedule`() {
        // A loop at this gap makes 144 calls a day, against Open-Meteo's 10,000 (Plan record 8.3).
        assertEquals(Duration.ofMinutes(10), RefreshThrottle.MINIMUM_AUTOMATIC_GAP)
    }
}
