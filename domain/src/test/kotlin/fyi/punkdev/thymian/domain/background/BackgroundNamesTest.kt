package fyi.punkdev.thymian.domain.background

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The phone reports numbers; diagnostics show words. The device test checks these numbers against Android's. */
class BackgroundNamesTest {
    @Test
    fun `standby buckets are named`() {
        assertEquals("active", BackgroundNames.standbyBucket(10))
        assertEquals("working_set", BackgroundNames.standbyBucket(20))
        assertEquals("frequent", BackgroundNames.standbyBucket(30))
        assertEquals("rare", BackgroundNames.standbyBucket(40))
        assertEquals("restricted", BackgroundNames.standbyBucket(45))
        assertEquals("bucket_99", BackgroundNames.standbyBucket(99))
    }

    @Test
    fun `process exit reasons are named`() {
        assertEquals("low_memory", BackgroundNames.exitReason(3))
        assertEquals("user_requested", BackgroundNames.exitReason(10))
        assertEquals("freezer", BackgroundNames.exitReason(14))
        assertEquals("package_updated", BackgroundNames.exitReason(16))
        assertEquals("reason_99", BackgroundNames.exitReason(99))
    }
}
