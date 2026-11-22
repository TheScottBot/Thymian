package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** The clock tap tries each target in turn, logging every step, until one opens. */
class ClockTapAttemptsTest {
    private val tappedAt = Instant.parse("2026-11-22T17:19:06Z")
    private val deskClock = ClockTapTarget.ClockApp("com.android.deskclock")

    private fun attempted(
        candidates: List<ClockTapTarget>,
        failureReasons: Map<ClockTapTarget, String>,
    ): Pair<List<ClockTapTarget>, List<DiagnosticEvent>> {
        val opened = mutableListOf<ClockTapTarget>()
        val recorded = mutableListOf<DiagnosticEvent>()
        ClockTapAttempts.attempt(tappedAt, candidates, recorded::add) { target ->
            opened += target
            failureReasons[target]
        }
        return opened to recorded
    }

    @Test
    fun `a refused alarm list falls back to the clock app`() {
        // The Xiaomi report of 22 November 2026: show alarms resolved, then starting it threw a SecurityException.
        val (opened, recorded) =
            attempted(listOf(ClockTapTarget.ShowAlarms, deskClock), mapOf(ClockTapTarget.ShowAlarms to "SecurityException"))

        assertEquals(listOf(ClockTapTarget.ShowAlarms, deskClock), opened)
        assertEquals(
            listOf(
                "tapped side=clock target=show_alarms",
                "tap_failed side=clock reason=SecurityException",
                "tap_fallback side=clock target=clock_app package=com.android.deskclock",
            ),
            recorded.map { event -> "${event.kind} ${event.detail}" },
        )
    }

    @Test
    fun `a chosen app that refuses falls back to the phone's own targets`() {
        val chosen = ClockTapTarget.ChosenApp("com.example.clock")
        val (opened, recorded) = attempted(listOf(chosen, ClockTapTarget.ShowAlarms), mapOf(chosen to "app_not_found"))

        assertEquals(listOf(chosen, ClockTapTarget.ShowAlarms), opened)
        assertEquals(
            listOf(
                "tapped side=clock target=chosen_app",
                "tap_failed side=clock reason=app_not_found",
                "tap_fallback side=clock target=show_alarms",
            ),
            recorded.map { event -> "${event.kind} ${event.detail}" },
        )
    }

    @Test
    fun `an alarm list that opens stops the attempts`() {
        val (opened, recorded) = attempted(listOf(ClockTapTarget.ShowAlarms, deskClock), emptyMap())

        assertEquals(listOf(ClockTapTarget.ShowAlarms), opened)
        assertEquals(listOf("tapped side=clock target=show_alarms"), recorded.map { event -> "${event.kind} ${event.detail}" })
    }

    @Test
    fun `when every target refuses each failure is logged`() {
        val (_, recorded) =
            attempted(
                listOf(ClockTapTarget.ShowAlarms, deskClock),
                mapOf(ClockTapTarget.ShowAlarms to "SecurityException", deskClock to "ActivityNotFoundException"),
            )

        assertEquals(
            listOf("tapped", "tap_failed", "tap_fallback", "tap_failed"),
            recorded.map { event -> event.kind },
        )
        assertEquals("side=clock reason=ActivityNotFoundException", recorded.last().detail)
    }

    @Test
    fun `with no candidates the tap is logged as having no clock app`() {
        val (opened, recorded) = attempted(emptyList(), emptyMap())

        assertEquals(emptyList<ClockTapTarget>(), opened)
        assertEquals(
            listOf("tapped side=clock target=none", "tap_failed side=clock reason=no_clock_app"),
            recorded.map { event -> "${event.kind} ${event.detail}" },
        )
    }
}
