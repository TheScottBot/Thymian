package fyi.punkdev.thymian.domain

import java.time.Duration

/** How often the weather is fetched and when shown data counts as stale. */
object RefreshPolicy {
    /** Specification 1.3 and CWD7. */
    val REFRESH_INTERVAL: Duration = Duration.ofMinutes(30)

    /** Specification 1.3: twice the interval, so one missed run never shows the stale indicator. */
    val STALENESS_THRESHOLD: Duration = REFRESH_INTERVAL.multipliedBy(2)
}
