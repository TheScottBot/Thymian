package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.RefreshPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** Specification 1.3: stale when the last successful refresh is older than twice the interval. */
class StalenessTest {
    private val fetchedAt = Instant.parse("2026-11-22T12:00:00Z")

    @Test
    fun `fresh up to and including the threshold`() {
        assertEquals(false, Staleness.isStale(fetchedAt, now = fetchedAt))
        assertEquals(false, Staleness.isStale(fetchedAt, now = fetchedAt.plus(RefreshPolicy.STALENESS_THRESHOLD)))
    }

    @Test
    fun `stale once older than the threshold`() {
        assertEquals(true, Staleness.isStale(fetchedAt, now = fetchedAt.plus(RefreshPolicy.STALENESS_THRESHOLD).plusSeconds(1)))
    }

    @Test
    fun `a fetch time in the future, from a clock change, is not stale`() {
        assertEquals(false, Staleness.isStale(fetchedAt, now = fetchedAt.minusSeconds(3_600)))
    }
}
