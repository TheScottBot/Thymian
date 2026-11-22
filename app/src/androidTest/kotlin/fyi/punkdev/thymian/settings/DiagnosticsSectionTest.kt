package fyi.punkdev.thymian.settings

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.weather.CurrentWeather
import fyi.punkdev.thymian.domain.weather.WeatherCondition
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/** The diagnostics section on the settings screen and its Copy diagnostics button (specification 1.3, CWD31). */
@RunWith(AndroidJUnit4::class)
class DiagnosticsSectionTest {
    private val themedContext = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), android.R.style.Theme_DeviceDefault)
    private val fetchedAt = Instant.parse("2026-11-22T12:00:00Z")
    private val snapshot =
        WeatherSnapshot(CurrentWeather(4.5, WeatherCondition.RAIN, isDay = true), "Berlin", LocationSourceUsed.FIXED_CHOSEN, fetchedAt)
    private val events =
        listOf(
            DiagnosticEvent(fetchedAt, "refreshed", "location_source=FIXED_CHOSEN place_named=true"),
            DiagnosticEvent(fetchedAt.plusSeconds(60), "weather_unavailable", "failure=NETWORK location_source=FIXED_CHOSEN"),
        )

    private fun onMainThread(testBody: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(testBody)

    private fun View.texts(): List<String> =
        if (this is ViewGroup) {
            (0 until childCount).flatMap { childIndex -> getChildAt(childIndex).texts() }
        } else {
            listOfNotNull((this as? TextView)?.text?.toString())
        }

    private fun View.copyButton(): Button =
        if (this is Button) {
            this
        } else {
            (this as ViewGroup).let { group ->
                (0 until group.childCount)
                    .mapNotNull { childIndex ->
                        runCatching { group.getChildAt(childIndex).copyButton() }.getOrNull()
                    }.first()
            }
        }

    @Test
    fun `the section names the location source with the last error and the next refresh`() =
        onMainThread {
            val section =
                DiagnosticsSection(themedContext, appVersion = "0.1.0", snapshot, events, nextRefresh = fetchedAt.plusSeconds(1_800)) { }
            val texts = section.view.texts()

            assertTrue(texts.toString(), texts.any { text -> text.startsWith("Location source: Fixed place") })
            assertTrue(texts.toString(), texts.any { text -> text.startsWith("Last error: ") && text.contains("failure=NETWORK") })
            assertTrue(texts.toString(), texts.any { text -> text.startsWith("Next refresh: ") })
            assertTrue(texts.toString(), texts.any { text -> text.startsWith("Last refresh: ") })
        }

    @Test
    fun `nothing yet says so plainly`() =
        onMainThread {
            val texts =
                DiagnosticsSection(
                    themedContext,
                    "0.1.0",
                    weather = null,
                    events = emptyList(),
                    nextRefresh = null,
                ) { }.view.texts()

            assertTrue(texts.contains("Last refresh: not yet"))
            assertTrue(texts.contains("Last error: none"))
            assertTrue(texts.contains("Next refresh: not scheduled"))
        }

    @Test
    fun `copy diagnostics hands over the report text without the place name`() =
        onMainThread {
            var copied: String? = null
            val section =
                DiagnosticsSection(themedContext, "0.1.0", snapshot, events, nextRefresh = null) { reportText ->
                    copied =
                        reportText
                }

            section.view.copyButton().performClick()

            val reportText = checkNotNull(copied) { "nothing was copied" }
            assertTrue(reportText.startsWith("Thymian diagnostics, version 0.1.0\n"))
            assertTrue(reportText.contains("weather_unavailable failure=NETWORK"))
            assertFalse(reportText.contains("Berlin"))
        }

    @Test
    fun `copy diagnostics includes what the phone offers the clock tap`() =
        onMainThread {
            var copied: String? = null
            val section =
                DiagnosticsSection(
                    themedContext,
                    "0.1.0",
                    snapshot,
                    events,
                    nextRefresh = null,
                    deviceFacts = listOf("clock_tap target=clock_app package=com.android.deskclock"),
                ) { reportText -> copied = reportText }

            section.view.copyButton().performClick()

            assertTrue(checkNotNull(copied).contains("clock_tap target=clock_app package=com.android.deskclock"))
        }

    @Test
    fun `the details start closed and open from the heading`() =
        onMainThread {
            val section = DiagnosticsSection(themedContext, "0.1.0", snapshot, events, nextRefresh = null) { }
            val heading = section.view.findViewWithTag<TextView>(DiagnosticsSection.HEADING_TAG)
            val details = section.view.findViewWithTag<View>(DiagnosticsSection.DETAILS_TAG)

            assertTrue(details.visibility == View.GONE)
            assertTrue(heading.text.toString() == "Show diagnostics")

            heading.performClick()

            assertTrue(details.visibility == View.VISIBLE)
            assertTrue(heading.text.toString() == "Hide diagnostics")
        }
}
