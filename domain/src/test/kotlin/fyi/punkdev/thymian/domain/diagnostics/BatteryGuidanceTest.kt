package fyi.punkdev.thymian.domain.diagnostics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** The HyperOS battery guidance shows on Xiaomi phones only (specification 1.3). */
class BatteryGuidanceTest {
    @Test
    fun `Xiaomi, Redmi and POCO phones get the guidance`() {
        for (manufacturer in listOf("Xiaomi", "xiaomi", "XIAOMI", " Xiaomi ")) {
            assertEquals(true, BatteryGuidance.appliesTo(manufacturer = manufacturer, brand = "Redmi"), manufacturer)
        }
        assertEquals(true, BatteryGuidance.appliesTo(manufacturer = "Unknown", brand = "POCO"))
    }

    @Test
    fun `other phones do not`() {
        assertEquals(false, BatteryGuidance.appliesTo(manufacturer = "Google", brand = "google"))
        assertEquals(false, BatteryGuidance.appliesTo(manufacturer = "samsung", brand = "samsung"))
        assertEquals(false, BatteryGuidance.appliesTo(manufacturer = "", brand = ""))
    }
}
