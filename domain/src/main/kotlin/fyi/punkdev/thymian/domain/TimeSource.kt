package fyi.punkdev.thymian.domain

import java.time.Instant

/** Port: the current time, so rules that depend on it are testable (specification 2.1). */
fun interface TimeSource {
    fun now(): Instant
}
