package fyi.punkdev.thymian.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class RefreshPolicyTest {
    @Test
    fun `weather refreshes every thirty minutes`() {
        assertEquals(Duration.ofMinutes(30), RefreshPolicy.REFRESH_INTERVAL)
    }

    @Test
    fun `data counts as stale after twice the refresh interval`() {
        assertEquals(Duration.ofMinutes(60), RefreshPolicy.STALENESS_THRESHOLD)
    }
}
