package fyi.punkdev.thymian.domain.sizing

import kotlin.math.floor
import kotlin.math.min

/** A size the launcher may show the widget at, in density independent pixels. */
data class WidgetSize(
    val widthDp: Float,
    val heightDp: Float,
)

/** Specification 2.4: clock only; clock and weather side by side; clock above weather. */
enum class SizeBucket {
    SMALL,
    WIDE,
    TALL,
}

/** Text sizes for the clock block, and whether the date line has room. */
data class ClockBlockSizing(
    val digitsDp: Int,
    val dateDp: Int,
    val suffixDp: Int,
    val dateFits: Boolean,
)

/** Text and icon sizes for the weather block: temperature like the digits, place name like the date. */
data class WeatherBlockSizing(
    val temperatureDp: Int,
    val placeDp: Int,
    val iconDp: Int,
)

/** [weather] is null for the Small bucket, which has no weather block (specification 2.4). */
data class SizeLayout(
    val bucket: SizeBucket,
    val clock: ClockBlockSizing,
    val weather: WeatherBlockSizing?,
)

/**
 * Chooses the size bucket and scales the clock text to the space it gets
 * (specification 2.4 and CWE4). The thresholds are the Plan record's provisional
 * values (section 6.1), kept here as the one place to tune them. When the height
 * cannot fit the date above readable digits, the date is hidden rather than the
 * digits shrinking further, because an element that does not fit is hidden, never
 * clipped (specification 1.3).
 */
object SizeLayoutPolicy {
    const val TALL_MINIMUM_HEIGHT_DP = 150f
    const val TALL_MAXIMUM_WIDTH_DP = 300f
    const val WIDE_MINIMUM_WIDTH_DP = 240f

    // Matches the widget root's padding: 16 dp either side, a little above and below.
    private const val HORIZONTAL_PADDING_DP = 32f
    private const val VERTICAL_PADDING_DP = 8f

    // Share of the widget the clock block gets in the two layouts that also hold weather.
    private const val WIDE_CLOCK_WIDTH_SHARE = 0.5f
    private const val TALL_CLOCK_HEIGHT_SHARE = 0.55f

    // Estimates for the light sans serif, to be confirmed by eye on the device in CWE4: "12:59" with
    // its suffix is about three digit sizes wide, and the date line adds about 0.3 of a digit size.
    private const val CLOCK_WIDTH_PER_DIGIT_SIZE = 3.0f
    private const val CLOCK_HEIGHT_PER_DIGIT_SIZE_WITH_DATE = 1.45f
    private const val CLOCK_HEIGHT_PER_DIGIT_SIZE_WITHOUT_DATE = 1.15f
    private const val DATE_TO_DIGITS = 0.27f
    private const val SUFFIX_TO_DIGITS = 0.31f

    private const val MINIMUM_DIGITS_DP = 24
    private const val MAXIMUM_DIGITS_DP = 72
    private const val MINIMUM_SMALL_TEXT_DP = 11

    fun layoutFor(size: WidgetSize): SizeLayout {
        val bucket = bucketFor(size)
        val usableWidth = (size.widthDp - HORIZONTAL_PADDING_DP).coerceAtLeast(0f)
        val clockWidth = if (bucket == SizeBucket.WIDE) usableWidth * WIDE_CLOCK_WIDTH_SHARE else usableWidth
        val clockHeightShare = if (bucket == SizeBucket.TALL) TALL_CLOCK_HEIGHT_SHARE else 1f
        val clockHeight = (size.heightDp * clockHeightShare - VERTICAL_PADDING_DP).coerceAtLeast(0f)
        val clock = clockSizingFor(clockWidth, clockHeight)
        val weather =
            when (bucket) {
                SizeBucket.SMALL -> {
                    null
                }

                // Side by side, the weather mirrors the clock so the two halves match, as in the reference.
                SizeBucket.WIDE -> {
                    WeatherBlockSizing(clock.digitsDp, clock.dateDp, clock.digitsDp)
                }

                SizeBucket.TALL -> {
                    val weatherHeight = (size.heightDp * (1f - TALL_CLOCK_HEIGHT_SHARE) - VERTICAL_PADDING_DP).coerceAtLeast(0f)
                    tallWeatherSizingFor(usableWidth, weatherHeight)
                }
            }
        return SizeLayout(bucket, clock, weather)
    }

    private fun tallWeatherSizingFor(
        weatherWidth: Float,
        weatherHeight: Float,
    ): WeatherBlockSizing {
        val temperature = min(weatherWidth / CLOCK_WIDTH_PER_DIGIT_SIZE, weatherHeight / CLOCK_HEIGHT_PER_DIGIT_SIZE_WITH_DATE)
        val temperatureDp = floor(temperature).toInt().coerceIn(MINIMUM_DIGITS_DP, MAXIMUM_DIGITS_DP)
        return WeatherBlockSizing(temperatureDp, smallTextFor(temperatureDp, DATE_TO_DIGITS), temperatureDp)
    }

    private fun bucketFor(size: WidgetSize): SizeBucket =
        when {
            size.heightDp >= TALL_MINIMUM_HEIGHT_DP && size.widthDp < TALL_MAXIMUM_WIDTH_DP -> SizeBucket.TALL
            size.widthDp >= WIDE_MINIMUM_WIDTH_DP -> SizeBucket.WIDE
            else -> SizeBucket.SMALL
        }

    private fun clockSizingFor(
        clockWidth: Float,
        clockHeight: Float,
    ): ClockBlockSizing {
        val digitsFromWidth = clockWidth / CLOCK_WIDTH_PER_DIGIT_SIZE
        val digitsWithDate = min(digitsFromWidth, clockHeight / CLOCK_HEIGHT_PER_DIGIT_SIZE_WITH_DATE)
        val dateFits = digitsWithDate >= MINIMUM_DIGITS_DP
        val digits =
            if (dateFits) digitsWithDate else min(digitsFromWidth, clockHeight / CLOCK_HEIGHT_PER_DIGIT_SIZE_WITHOUT_DATE)
        val digitsDp = floor(digits).toInt().coerceIn(MINIMUM_DIGITS_DP, MAXIMUM_DIGITS_DP)
        return ClockBlockSizing(
            digitsDp = digitsDp,
            dateDp = smallTextFor(digitsDp, DATE_TO_DIGITS),
            suffixDp = smallTextFor(digitsDp, SUFFIX_TO_DIGITS),
            dateFits = dateFits,
        )
    }

    private fun smallTextFor(
        digitsDp: Int,
        ratio: Float,
    ): Int = floor(digitsDp * ratio).toInt().coerceAtLeast(MINIMUM_SMALL_TEXT_DP)
}
