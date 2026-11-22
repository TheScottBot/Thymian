package fyi.punkdev.thymian.widget

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * Runs on a device: only Android's own locale data can answer these (specification
 * 1.3, CWD13). That data differs by Android version: on 22 November 2026 en-GB gave
 * "EEE, d MMMM" on the API 34 emulator and "EEE d MMMM" on the Pixel 10 Pro (API 37).
 * The tests therefore pin the field order and month width, with the comma optional.
 */
@RunWith(AndroidJUnit4::class)
class LocalizedDatePatternTest {
    @Test
    fun `british english gives weekday then day then full month`() {
        val pattern = LocalizedDatePattern.forLocale(Locale.UK)

        assertTrue("was $pattern", Regex("""^EEE,? d MMMM$""").matches(pattern))
    }

    @Test
    fun `american english puts the full month before the day`() {
        val pattern = LocalizedDatePattern.forLocale(Locale.US)

        assertTrue("was $pattern", Regex("""^EEE, MMMM d$""").matches(pattern))
    }
}
