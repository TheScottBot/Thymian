package fyi.punkdev.thymian.domain.background

import fyi.punkdev.thymian.domain.diagnostics.KeyValueDiagnosticsHistory
import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * On 22 and 23 November 2026 the Pixel ran neither the soak spike nor Thymian's
 * refresh for about 20 hours, with no reboot and the phone in use. Each periodic
 * run now records what the phone reports, so the next gap explains itself.
 */
class BackgroundWatchTest {
    private val firstRun = Instant.parse("2026-11-22T20:02:00Z")
    private val usualState =
        BackgroundState(
            standbyBucket = "active",
            powerSave = false,
            deviceIdle = false,
            backgroundRestricted = false,
            batteryOptimised = true,
        )

    private class FakeObservations(
        var state: BackgroundState,
        var bucketsSince: List<String> = emptyList(),
        var exitsSince: List<ProcessExit> = emptyList(),
    ) : BackgroundObservations {
        val askedSince = mutableListOf<Instant>()

        override fun currentState(): BackgroundState = state

        override fun standbyBucketsSince(since: Instant): List<String> {
            askedSince += since
            return bucketsSince
        }

        override fun processExitsSince(since: Instant): List<ProcessExit> {
            askedSince += since
            return exitsSince
        }
    }

    private val history = KeyValueDiagnosticsHistory(InMemoryKeyValueStore())
    private val watch = BackgroundWatch(InMemoryKeyValueStore(), history)

    private fun recorded(): List<String> = history.events().map { event -> "${event.kind} ${event.detail}" }

    @Test
    fun `the first run records the background state`() {
        watch.periodicRunStarted(firstRun, FakeObservations(usualState))

        assertEquals(
            listOf("background_state bucket=active power_save=false device_idle=false background_restricted=false battery_optimised=true"),
            recorded(),
        )
    }

    @Test
    fun `an unchanged state is not recorded again`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        watch.periodicRunStarted(firstRun.plusSeconds(30 * 60), observations)

        assertEquals(1, recorded().size)
    }

    @Test
    fun `a changed state is recorded`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        observations.state = usualState.copy(standbyBucket = "rare", powerSave = true)
        watch.periodicRunStarted(firstRun.plusSeconds(30 * 60), observations)

        assertEquals(
            "background_state bucket=rare power_save=true device_idle=false background_restricted=false battery_optimised=true",
            recorded().last(),
        )
    }

    @Test
    fun `a run long after the previous one records the gap and the buckets in between`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        observations.bucketsSince = listOf("rare", "restricted", "active")
        watch.periodicRunStarted(Instant.parse("2026-11-23T15:50:00Z"), observations)

        assertEquals("refresh_gap minutes=1188 buckets=rare,restricted,active", recorded().last())
        assertEquals(firstRun, observations.askedSince.last())
    }

    @Test
    fun `a gap with no bucket changes says none`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        watch.periodicRunStarted(firstRun.plusSeconds(3 * 60 * 60), observations)

        assertEquals("refresh_gap minutes=180 buckets=none", recorded().last())
    }

    @Test
    fun `a run within three intervals records no gap`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        watch.periodicRunStarted(firstRun.plusSeconds(90 * 60), observations)

        assertEquals(listOf("background_state"), history.events().map { event -> event.kind })
    }

    @Test
    fun `the app being stopped since the previous run is recorded at the time it happened`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        val stoppedAt = firstRun.plusSeconds(10 * 60)
        observations.exitsSince = listOf(ProcessExit(stoppedAt, "user_requested"))
        watch.periodicRunStarted(firstRun.plusSeconds(30 * 60), observations)

        val stop = history.events().single { event -> event.kind == "process_exit" }
        assertEquals("reason=user_requested", stop.detail)
        assertEquals(stoppedAt, stop.occurredAt)
    }

    @Test
    fun `a reason that is not one word is kept as unknown`() {
        val observations = FakeObservations(usualState)
        watch.periodicRunStarted(firstRun, observations)
        observations.exitsSince = listOf(ProcessExit(firstRun.plusSeconds(60), "not one word"))
        watch.periodicRunStarted(firstRun.plusSeconds(30 * 60), observations)

        assertEquals("process_exit reason=unknown", recorded().last())
    }
}
