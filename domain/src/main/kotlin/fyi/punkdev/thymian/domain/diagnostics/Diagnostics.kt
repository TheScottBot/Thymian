package fyi.punkdev.thymian.domain.diagnostics

import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.settings.KeyValueStore
import fyi.punkdev.thymian.domain.weather.RefreshOutcome
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import java.time.Instant

/** One thing that happened, by category only (datum 0.15): no coordinates, place names or weather values. */
data class DiagnosticEvent(
    val occurredAt: Instant,
    val kind: String,
    val detail: String,
)

/** Port: the bounded event history behind diagnostics and Copy diagnostics (CWD31). */
interface DiagnosticsHistory {
    fun record(
        occurredAt: Instant,
        outcome: RefreshOutcome,
    ) = recordEvent(eventFor(occurredAt, outcome))

    fun recordEvent(event: DiagnosticEvent)

    fun events(): List<DiagnosticEvent>

    companion object {
        /** About a day of refreshes every 30 minutes, with room for retries and taps. */
        const val MAXIMUM_EVENTS = 60

        fun eventFor(
            occurredAt: Instant,
            outcome: RefreshOutcome,
        ): DiagnosticEvent =
            when (outcome) {
                is RefreshOutcome.Refreshed -> {
                    DiagnosticEvent(
                        occurredAt,
                        "refreshed",
                        "location_source=${outcome.snapshot.locationSource} place_named=${outcome.snapshot.placeName != null}",
                    )
                }

                is RefreshOutcome.LocationUnavailable -> {
                    DiagnosticEvent(occurredAt, "location_unavailable", "reason=${outcome.reason}")
                }

                is RefreshOutcome.WeatherUnavailable -> {
                    DiagnosticEvent(
                        occurredAt,
                        "weather_unavailable",
                        "failure=${outcome.failure} location_source=${outcome.locationSource}",
                    )
                }
            }
    }
}

/**
 * Keeps the newest [DiagnosticsHistory.MAXIMUM_EVENTS] events as lines of text,
 * one key. Lines that do not parse or are too long are skipped on reading, so a
 * damaged entry never hides the rest (datum 0.10).
 */
class KeyValueDiagnosticsHistory(
    private val keyValueStore: KeyValueStore,
) : DiagnosticsHistory {
    @Synchronized
    override fun recordEvent(event: DiagnosticEvent) {
        val kept = (events() + event).takeLast(DiagnosticsHistory.MAXIMUM_EVENTS)
        keyValueStore.writeAll(mapOf(EVENTS_KEY to kept.joinToString(separator = "\n") { stored -> encode(stored) }))
    }

    override fun events(): List<DiagnosticEvent> =
        keyValueStore
            .readAll()[EVENTS_KEY]
            .orEmpty()
            .lineSequence()
            .mapNotNull { line -> decode(line) }
            .toList()
            .takeLast(DiagnosticsHistory.MAXIMUM_EVENTS)

    private fun encode(event: DiagnosticEvent): String =
        "${event.occurredAt.toEpochMilli()}$FIELD_SEPARATOR${event.kind}$FIELD_SEPARATOR${event.detail}"

    private fun decode(line: String): DiagnosticEvent? {
        if (line.length > MAXIMUM_LINE_LENGTH) return null
        val fields = line.split(FIELD_SEPARATOR)
        if (fields.size != FIELD_COUNT) return null
        val occurredAt = fields[0].toLongOrNull()?.let { epochMillis -> Instant.ofEpochMilli(epochMillis) } ?: return null
        return DiagnosticEvent(occurredAt, fields[1], fields[2])
    }

    private companion object {
        const val EVENTS_KEY = "diagnostics.events"
        const val FIELD_SEPARATOR = "\t"
        const val FIELD_COUNT = 3
        const val MAXIMUM_LINE_LENGTH = 200
    }
}

/** The text Copy diagnostics puts on the clipboard, for the user to paste into a message (CWD31). */
object DiagnosticsReport {
    fun asText(
        appVersion: String,
        events: List<DiagnosticEvent>,
        deviceFacts: List<String> = emptyList(),
    ): String =
        buildString {
            appendLine("Thymian diagnostics, version $appVersion")
            // What this phone offers, such as the clock tap target; category words only, as for events.
            deviceFacts.forEach { fact -> appendLine(fact) }
            events.forEach { event -> appendLine("${event.occurredAt} ${event.kind} ${event.detail}") }
        }
}

/** Specification 1.3: the HyperOS battery guidance screen is for Xiaomi phones only. */
object BatteryGuidance {
    private val xiaomiNames = setOf("xiaomi", "redmi", "poco")

    fun appliesTo(
        manufacturer: String,
        brand: String,
    ): Boolean = listOf(manufacturer, brand).any { name -> name.trim().lowercase() in xiaomiNames }
}

/** The diagnostics section's summary (specification 1.3); the next refresh comes from the scheduler. */
data class DiagnosticsSummary(
    val lastRefresh: Instant?,
    val locationSource: LocationSourceUsed?,
    val lastError: DiagnosticEvent?,
) {
    companion object {
        private val ERROR_KINDS = setOf("location_unavailable", "weather_unavailable", "tap_failed")

        fun from(
            weather: WeatherSnapshot?,
            events: List<DiagnosticEvent>,
        ): DiagnosticsSummary =
            DiagnosticsSummary(
                lastRefresh = weather?.fetchedAt,
                locationSource = weather?.locationSource,
                // A throttled attempt is the guard working, not a failure.
                lastError = events.lastOrNull { event -> event.kind in ERROR_KINDS },
            )
    }
}
