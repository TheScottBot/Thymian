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

data class SizeLayout(
    val bucket: SizeBucket,
    val clock: ClockBlockSizing,
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
        return SizeLayout(bucket, clockSizingFor(clockWidth, clockHeight))
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
