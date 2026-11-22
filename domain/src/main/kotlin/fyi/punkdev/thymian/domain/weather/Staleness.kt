package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.RefreshPolicy
import java.time.Duration
import java.time.Instant

/** Specification 1.3: stale once the last successful refresh is older than twice the interval. */
object Staleness {
    fun isStale(
        fetchedAt: Instant,
        now: Instant,
    ): Boolean = Duration.between(fetchedAt, now) > RefreshPolicy.STALENESS_THRESHOLD
}
