package fyi.punkdev.thymian.domain.sizing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Size buckets and text scaling (specification 2.4, CWE4), with the thresholds
 * from the Plan record (section 6.1): Tall when at least 150 dp high and under
 * 300 dp wide; otherwise Wide from 240 dp wide; otherwise Small.
 */
class SizeLayoutPolicyTest {
    private fun bucketOf(
        widthDp: Float,
        heightDp: Float,
    ): SizeBucket = SizeLayoutPolicy.layoutFor(WidgetSize(widthDp, heightDp)).bucket

    @Test
    fun `bucket boundaries`() {
        val expectations =
            listOf(
                Triple(110f, 40f, SizeBucket.SMALL),
                Triple(239.9f, 94f, SizeBucket.SMALL),
                Triple(240f, 94f, SizeBucket.WIDE),
                Triple(276.6f, 94.1f, SizeBucket.WIDE),
                Triple(587.8f, 48.4f, SizeBucket.WIDE),
                Triple(299.9f, 150f, SizeBucket.TALL),
                Triple(299.9f, 149.9f, SizeBucket.WIDE),
                Triple(300f, 150f, SizeBucket.WIDE),
                Triple(184f, 200f, SizeBucket.TALL),
                Triple(130f, 149.9f, SizeBucket.SMALL),
                Triple(130f, 150f, SizeBucket.TALL),
            )
        for ((widthDp, heightDp, expectedBucket) in expectations) {
            assertEquals(expectedBucket, bucketOf(widthDp, heightDp), "${widthDp}x$heightDp")
        }
    }

    @Test
    fun `the Pixel 3 by 1 widget gets digits sized to fit half its width`() {
        val clock = SizeLayoutPolicy.layoutFor(WidgetSize(276.6f, 94.1f)).clock

        assertEquals(40, clock.digitsDp)
        assertEquals(11, clock.dateDp)
        assertEquals(12, clock.suffixDp)
        assertTrue(clock.dateFits)
    }

    @Test
    fun `a likely Xiaomi 5 by 1 widget is limited by its height`() {
        val clock = SizeLayoutPolicy.layoutFor(WidgetSize(360f, 80f)).clock

        assertEquals(49, clock.digitsDp)
        assertEquals(13, clock.dateDp)
        assertTrue(clock.dateFits)
    }

    @Test
    fun `too little height hides the date before shrinking the digits below their minimum`() {
        val clock = SizeLayoutPolicy.layoutFor(WidgetSize(276.6f, 40f)).clock

        assertFalse(clock.dateFits)
        assertEquals(27, clock.digitsDp)
    }

    @Test
    fun `digits never leave their readable range`() {
        for (widthDp in 40..800 step 20) {
            for (heightDp in 30..600 step 20) {
                val clock = SizeLayoutPolicy.layoutFor(WidgetSize(widthDp.toFloat(), heightDp.toFloat())).clock

                assertTrue(clock.digitsDp in 24..72, "${widthDp}x$heightDp gave ${clock.digitsDp}")
                assertTrue(clock.dateDp >= 11 && clock.suffixDp >= 11, "${widthDp}x$heightDp")
            }
        }
    }

    @Test
    fun `a larger widget in the same bucket never gets smaller digits`() {
        var previousDigits = 0
        for (heightDp in 60..140 step 5) {
            val digits = SizeLayoutPolicy.layoutFor(WidgetSize(400f, heightDp.toFloat())).clock.digitsDp

            assertTrue(digits >= previousDigits, "height $heightDp gave $digits after $previousDigits")
            previousDigits = digits
        }
    }

    @Test
    fun `the tall layout sizes the clock from the upper part of the height`() {
        val layout = SizeLayoutPolicy.layoutFor(WidgetSize(200f, 250f))

        assertEquals(SizeBucket.TALL, layout.bucket)
        assertEquals(56, layout.clock.digitsDp)
    }
}
