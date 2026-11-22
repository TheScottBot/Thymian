package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticsSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** Every touch on either side is logged, so a report shows whether a tap reached the app and what came of it. */
class TapEventsTest {
    private val tappedAt = Instant.parse("2026-11-22T17:00:00Z")

    @Test
    fun `a clock tap records the target it was sent to`() {
        assertEquals(
            DiagnosticEvent(tappedAt, "tapped", "side=clock target=clock_app package=com.android.deskclock"),
            TapEvents.clockTapped(tappedAt, ClockTapTarget.ClockApp("com.android.deskclock")),
        )
        assertEquals("side=clock target=none", TapEvents.clockTapped(tappedAt, ClockTapTarget.NoTarget).detail)
    }

    @Test
    fun `a weather tap records its action without naming the chosen app`() {
        assertEquals(
            DiagnosticEvent(tappedAt, "tapped", "side=weather action=refresh"),
            TapEvents.weatherTapped(tappedAt, WeatherTapAction.Refresh),
        )
        assertEquals(
            "side=weather action=refresh_and_open",
            TapEvents.weatherTapped(tappedAt, WeatherTapAction.RefreshAndOpen("com.example.weather")).detail,
        )
    }

    @Test
    fun `a failed tap records the side and a reason of one word`() {
        assertEquals(
            DiagnosticEvent(tappedAt, "tap_failed", "side=clock reason=ActivityNotFoundException"),
            TapEvents.tapFailed(tappedAt, TapSide.CLOCK, "ActivityNotFoundException"),
        )
        assertEquals("side=weather reason=unknown", TapEvents.tapFailed(tappedAt, TapSide.WEATHER, "bad reason\twith tab").detail)
    }

    @Test
    fun `a failed tap counts as the last error in diagnostics`() {
        val failure = TapEvents.tapFailed(tappedAt, TapSide.CLOCK, "SecurityException")

        assertEquals(failure, DiagnosticsSummary.from(weather = null, events = listOf(failure)).lastError)
    }
}
