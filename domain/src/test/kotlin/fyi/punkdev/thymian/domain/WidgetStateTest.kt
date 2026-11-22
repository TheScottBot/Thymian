package fyi.punkdev.thymian.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WidgetStateTest {
    @Test
    fun `a new widget follows the device clock setting`() {
        assertEquals(ClockMode.FOLLOW_DEVICE, WidgetState().clockMode)
    }
}
