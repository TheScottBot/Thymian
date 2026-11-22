package fyi.punkdev.thymian.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * A TextClock holds two formats and shows the 12 hour one when the device uses
 * 12 hour time and the 24 hour one otherwise. Each clock mode is a choice of
 * what goes in those two slots for the digits, the am or pm suffix and the date.
 */
class ClockFormatPolicyTest {
    private val britishDatePattern = "EEE d MMMM"
    private val americanDatePattern = "EEE, MMMM d"

    @Test
    fun `follow device puts the 12 hour digits and suffix in the 12 hour slot and 24 hour digits in the other`() {
        val formats = ClockFormatPolicy.formatsFor(ClockMode.FOLLOW_DEVICE, britishDatePattern)

        assertEquals(TextClockFormats(whenDeviceUses12Hour = "h:mm", whenDeviceUses24Hour = "HH:mm"), formats.digits)
        assertEquals(TextClockFormats(whenDeviceUses12Hour = "a", whenDeviceUses24Hour = ""), formats.suffix)
    }

    @Test
    fun `twelve hour mode shows 12 hour digits and the suffix whatever the device setting`() {
        val formats = ClockFormatPolicy.formatsFor(ClockMode.TWELVE_HOUR, britishDatePattern)

        assertEquals(TextClockFormats(whenDeviceUses12Hour = "h:mm", whenDeviceUses24Hour = "h:mm"), formats.digits)
        assertEquals(TextClockFormats(whenDeviceUses12Hour = "a", whenDeviceUses24Hour = "a"), formats.suffix)
    }

    @Test
    fun `twenty four hour mode shows 24 hour digits and no suffix whatever the device setting`() {
        val formats = ClockFormatPolicy.formatsFor(ClockMode.TWENTY_FOUR_HOUR, britishDatePattern)

        assertEquals(TextClockFormats(whenDeviceUses12Hour = "HH:mm", whenDeviceUses24Hour = "HH:mm"), formats.digits)
        assertEquals(TextClockFormats(whenDeviceUses12Hour = "", whenDeviceUses24Hour = ""), formats.suffix)
    }

    @Test
    fun `the en-GB date pattern is used unchanged in both slots in every mode`() {
        for (clockMode in ClockMode.entries) {
            val formats = ClockFormatPolicy.formatsFor(clockMode, britishDatePattern)

            assertEquals(TextClockFormats(britishDatePattern, britishDatePattern), formats.date, "mode $clockMode")
        }
    }

    @Test
    fun `the en-US date pattern is used unchanged in both slots`() {
        val formats = ClockFormatPolicy.formatsFor(ClockMode.FOLLOW_DEVICE, americanDatePattern)

        assertEquals(TextClockFormats(americanDatePattern, americanDatePattern), formats.date)
    }

    @Test
    fun `the date skeleton asks for abbreviated weekday, day of month and full month name`() {
        // Specification 1.3 and CWD13: the locale's best pattern for these fields, giving Tue 11 July on en-GB.
        assertEquals("EEEdMMMM", ClockFormatPolicy.DATE_SKELETON)
    }

    @Test
    fun `follow device is the default clock mode`() {
        assertEquals(ClockMode.FOLLOW_DEVICE, ClockMode.DEFAULT)
    }
}
