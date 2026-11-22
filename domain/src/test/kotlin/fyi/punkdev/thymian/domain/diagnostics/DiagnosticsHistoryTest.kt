package fyi.punkdev.thymian.domain.diagnostics

import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.location.LocationUnavailableReason
import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import fyi.punkdev.thymian.domain.weather.CurrentWeather
import fyi.punkdev.thymian.domain.weather.RefreshOutcome
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import fyi.punkdev.thymian.domain.weather.WeatherFetchFailure
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * The bounded event history behind diagnostics and Copy diagnostics (CWD31).
 * It holds outcome categories and times only: never coordinates, place names or
 * weather values (datum 0.15).
 */
class DiagnosticsHistoryTest {
    private val start = Instant.parse("2026-11-22T12:00:00Z")
    private val keyValueStore = InMemoryKeyValueStore()
    private val history = KeyValueDiagnosticsHistory(keyValueStore)

    private val refreshed =
        RefreshOutcome.Refreshed(
            WeatherSnapshot(
                CurrentWeather(4.5, WeatherCondition.RAIN, isDay = true),
                "Somewhere Private",
                LocationSourceUsed.DEVICE_RECENT,
                start,
            ),
        )

    @Test
    fun `outcomes become events holding only categories`() {
        history.record(start, refreshed)
        history.record(start.plusSeconds(60), RefreshOutcome.LocationUnavailable(LocationUnavailableReason.PERMISSION_DENIED))
        history.record(
            start.plusSeconds(120),
            RefreshOutcome.WeatherUnavailable(WeatherFetchFailure.NETWORK, LocationSourceUsed.FIXED_CHOSEN),
        )

        assertEquals(
            listOf(
                DiagnosticEvent(start, "refreshed", "location_source=DEVICE_RECENT place_named=true"),
                DiagnosticEvent(start.plusSeconds(60), "location_unavailable", "reason=PERMISSION_DENIED"),
                DiagnosticEvent(start.plusSeconds(120), "weather_unavailable", "failure=NETWORK location_source=FIXED_CHOSEN"),
            ),
            history.events(),
        )
    }

    @Test
    fun `no personal detail is ever stored`() {
        history.record(start, refreshed)

        val everythingStored = keyValueStore.readAll().values.joinToString(separator = " ")
        assertFalse(everythingStored.contains("Somewhere Private"))
        assertFalse(everythingStored.contains("4.5"))
    }

    @Test
    fun `only the most recent events are kept`() {
        repeat(DiagnosticsHistory.MAXIMUM_EVENTS + 7) { index -> history.record(start.plusSeconds(index.toLong()), refreshed) }

        val events = history.events()
        assertEquals(DiagnosticsHistory.MAXIMUM_EVENTS, events.size)
        assertEquals(start.plusSeconds(7), events.first().occurredAt)
    }

    @Test
    fun `throttled refreshes and other app events can be recorded`() {
        history.recordEvent(DiagnosticEvent(start, "throttled", "trigger=AUTOMATIC"))

        assertEquals(listOf(DiagnosticEvent(start, "throttled", "trigger=AUTOMATIC")), history.events())
    }

    @Test
    fun `damaged stored events are skipped rather than failing the history`() {
        history.record(start, refreshed)
        keyValueStore.writeAll(
            mapOf("diagnostics.events" to keyValueStore.readAll().getValue("diagnostics.events") + "\nnot an event\n" + "x".repeat(500)),
        )

        assertEquals(1, history.events().size)
    }

    @Test
    fun `the copied text lists events oldest first with a heading and no personal detail`() {
        history.record(start, refreshed)
        history.record(start.plusSeconds(60), RefreshOutcome.LocationUnavailable(LocationUnavailableReason.NO_FIX))

        assertEquals(
            "Thymian diagnostics, version 0.1.0\n" +
                "2026-11-22T12:00:00Z refreshed location_source=DEVICE_RECENT place_named=true\n" +
                "2026-11-22T12:01:00Z location_unavailable reason=NO_FIX\n",
            DiagnosticsReport.asText(appVersion = "0.1.0", events = history.events()),
        )
    }
}
