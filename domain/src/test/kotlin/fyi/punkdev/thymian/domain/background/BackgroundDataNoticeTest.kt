package fyi.punkdev.thymian.domain.background

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * The settings screen offers to allow background data when it is off, because
 * nobody reads the documentation (24 November 2026). The block can only be seen
 * from the background, so the last scheduled run that could tell decides.
 */
class BackgroundDataNoticeTest {
    private val at = Instant.parse("2026-11-24T10:52:51Z")

    private fun state(networkBlocked: String) =
        DiagnosticEvent(
            at,
            "background_state",
            "bucket=active power_save=false device_idle=false background_restricted=false battery_optimised=true " +
                "network_blocked=$networkBlocked",
        )

    @Test
    fun `shown when the last run that could tell found the network blocked`() {
        assertEquals(true, BackgroundDataNotice.shouldShow(listOf(state("true"))))
    }

    @Test
    fun `a run while Thymian was on screen cannot tell, so the earlier reading stands`() {
        // 24 November 2026: a run with the app on screen recorded the network as allowed and hid the notice.
        assertEquals(true, BackgroundDataNotice.shouldShow(listOf(state("true"), state("unknown"))))
    }

    @Test
    fun `hidden once a later run found the network allowed`() {
        assertEquals(false, BackgroundDataNotice.shouldShow(listOf(state("true"), state("false"))))
    }

    @Test
    fun `hidden with nothing to go on`() {
        assertEquals(false, BackgroundDataNotice.shouldShow(emptyList()))
        assertEquals(false, BackgroundDataNotice.shouldShow(listOf(state("unknown"))))
    }
}
