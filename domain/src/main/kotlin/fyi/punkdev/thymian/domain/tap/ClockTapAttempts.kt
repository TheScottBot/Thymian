package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import java.time.Instant

/**
 * Tries each clock tap target in turn until one opens, recording every step
 * before acting on it, so a report shows what was tried and what refused.
 */
object ClockTapAttempts {
    /**
     * [tryOpen] returns null when the target opened, or a one word reason, such as
     * an exception's class name, when it did not.
     */
    fun attempt(
        tappedAt: Instant,
        candidates: List<ClockTapTarget>,
        record: (DiagnosticEvent) -> Unit,
        tryOpen: (ClockTapTarget) -> String?,
    ) {
        record(TapEvents.clockTapped(tappedAt, candidates.firstOrNull() ?: ClockTapTarget.NoTarget))
        if (candidates.isEmpty()) {
            record(TapEvents.tapFailed(tappedAt, TapSide.CLOCK, "no_clock_app"))
            return
        }
        candidates.forEachIndexed { candidateIndex, target ->
            if (candidateIndex > 0) record(TapEvents.clockFellBack(tappedAt, target))
            val failureReason = tryOpen(target) ?: return
            record(TapEvents.tapFailed(tappedAt, TapSide.CLOCK, failureReason))
        }
    }
}
