package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import java.time.Instant

enum class TapSide { CLOCK, WEATHER }

/**
 * The diagnostics events for touches on the widget, so a report shows whether a
 * tap reached the app and what came of it. Category words only (datum 0.15).
 */
object TapEvents {
    private val ONE_WORD = Regex("[A-Za-z_]{1,64}")

    fun clockTapped(
        tappedAt: Instant,
        target: ClockTapTarget,
    ): DiagnosticEvent = DiagnosticEvent(tappedAt, "tapped", "side=clock ${target.detail}")

    fun weatherTapped(
        tappedAt: Instant,
        action: WeatherTapAction,
    ): DiagnosticEvent = DiagnosticEvent(tappedAt, "tapped", "side=weather action=${action.diagnosticName}")

    /** The clock tap moved on to [target] after the previous target refused to open. */
    fun clockFellBack(
        tappedAt: Instant,
        target: ClockTapTarget,
    ): DiagnosticEvent = DiagnosticEvent(tappedAt, "tap_fallback", "side=clock ${target.detail}")

    /** [reason] is kept only when it is one word, such as an exception's class name. */
    fun tapFailed(
        tappedAt: Instant,
        side: TapSide,
        reason: String,
    ): DiagnosticEvent {
        val keptReason = if (ONE_WORD.matches(reason)) reason else "unknown"
        return DiagnosticEvent(tappedAt, "tap_failed", "side=${side.name.lowercase()} reason=$keptReason")
    }
}
