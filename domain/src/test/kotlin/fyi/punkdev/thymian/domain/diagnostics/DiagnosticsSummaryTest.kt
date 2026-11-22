package fyi.punkdev.thymian.domain.diagnostics

import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.weather.CurrentWeather
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** The diagnostics section's summary lines (specification 1.3): last refresh, last error, location source. */
class DiagnosticsSummaryTest {
    private val start = Instant.parse("2026-11-22T12:00:00Z")
    private val snapshot =
        WeatherSnapshot(CurrentWeather(4.5, WeatherCondition.RAIN, isDay = true), "Berlin", LocationSourceUsed.FIXED_CHOSEN, start)

    @Test
    fun `nothing yet means no refresh and no error`() {
        assertEquals(
            DiagnosticsSummary(lastRefresh = null, locationSource = null, lastError = null),
            DiagnosticsSummary.from(null, emptyList()),
        )
    }

    @Test
    fun `the last refresh and location source come from the kept snapshot`() {
        val summary = DiagnosticsSummary.from(snapshot, emptyList())

        assertEquals(start, summary.lastRefresh)
        assertEquals(LocationSourceUsed.FIXED_CHOSEN, summary.locationSource)
    }

    @Test
    fun `the last error is the newest failure, and throttled attempts are not errors`() {
        val events =
            listOf(
                DiagnosticEvent(start, "location_unavailable", "reason=NO_FIX"),
                DiagnosticEvent(start.plusSeconds(60), "weather_unavailable", "failure=NETWORK location_source=FIXED_CHOSEN"),
                DiagnosticEvent(start.plusSeconds(120), "throttled", "trigger=AUTOMATIC"),
            )

        assertEquals(events[1], DiagnosticsSummary.from(snapshot, events).lastError)
    }

    @Test
    fun `a failure older than the last success is still reported, as history`() {
        val events = listOf(DiagnosticEvent(start.minusSeconds(60), "weather_unavailable", "failure=NETWORK location_source=FIXED_CHOSEN"))

        assertEquals(events.single(), DiagnosticsSummary.from(snapshot, events).lastError)
    }
}
