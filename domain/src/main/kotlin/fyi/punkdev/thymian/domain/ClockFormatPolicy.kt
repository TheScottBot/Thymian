package fyi.punkdev.thymian.domain

/**
 * The two formats one self updating clock view holds: Android's TextClock shows
 * [whenDeviceUses12Hour] while the device is set to 12 hour time and
 * [whenDeviceUses24Hour] otherwise, switching by itself when the setting changes.
 */
data class TextClockFormats(
    val whenDeviceUses12Hour: String,
    val whenDeviceUses24Hour: String,
)

/** Formats for the three clock views in the clock block. */
data class ClockBlockFormats(
    val digits: TextClockFormats,
    val suffix: TextClockFormats,
    val date: TextClockFormats,
)

/**
 * Chooses clock formats per mode. Forcing a mode means writing the same format
 * into both slots, so the view never depends on the device setting. The am or pm
 * suffix is its own view, so it can be smaller than the digits (specification
 * 1.3); an empty format hides it in 24 hour time.
 */
object ClockFormatPolicy {
    /** Abbreviated weekday, day of month and full month name, ordered by the locale (CWD13). */
    const val DATE_SKELETON = "EEEdMMMM"

    private const val TWELVE_HOUR_DIGITS = "h:mm"
    private const val TWENTY_FOUR_HOUR_DIGITS = "HH:mm"
    private const val DAY_PERIOD = "a"
    private const val HIDDEN = ""

    /** [localizedDatePattern] is the device locale's best pattern for [DATE_SKELETON]. */
    fun formatsFor(
        clockMode: ClockMode,
        localizedDatePattern: String,
    ): ClockBlockFormats {
        val digits =
            when (clockMode) {
                ClockMode.TWELVE_HOUR -> TextClockFormats(TWELVE_HOUR_DIGITS, TWELVE_HOUR_DIGITS)
                ClockMode.TWENTY_FOUR_HOUR -> TextClockFormats(TWENTY_FOUR_HOUR_DIGITS, TWENTY_FOUR_HOUR_DIGITS)
                ClockMode.FOLLOW_DEVICE -> TextClockFormats(TWELVE_HOUR_DIGITS, TWENTY_FOUR_HOUR_DIGITS)
            }
        val suffix =
            when (clockMode) {
                ClockMode.TWELVE_HOUR -> TextClockFormats(DAY_PERIOD, DAY_PERIOD)
                ClockMode.TWENTY_FOUR_HOUR -> TextClockFormats(HIDDEN, HIDDEN)
                ClockMode.FOLLOW_DEVICE -> TextClockFormats(DAY_PERIOD, HIDDEN)
            }
        return ClockBlockFormats(
            digits = digits,
            suffix = suffix,
            date = TextClockFormats(localizedDatePattern, localizedDatePattern),
        )
    }
}
