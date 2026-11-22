package fyi.punkdev.thymian.settings

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The HyperOS battery guidance, shown on Xiaomi phones only and closed by default (specification 1.3). */
@RunWith(AndroidJUnit4::class)
class BatteryGuidanceSectionTest {
    private val themedContext = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), android.R.style.Theme_DeviceDefault)

    private fun onMainThread(testBody: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(testBody)

    private fun View.buttons(): List<Button> =
        if (this is Button) {
            listOf(this)
        } else if (this is ViewGroup) {
            (0 until childCount).flatMap { childIndex -> getChildAt(childIndex).buttons() }
        } else {
            emptyList()
        }

    @Test
    fun `other phones get no guidance`() =
        onMainThread {
            assertNull(BatteryGuidanceSection.forDevice(themedContext, manufacturer = "Google", brand = "google") { })
        }

    @Test
    fun `a Xiaomi phone gets the guidance closed with a way to open the app settings`() =
        onMainThread {
            var openedSettings = 0
            val section =
                checkNotNull(
                    BatteryGuidanceSection.forDevice(themedContext, manufacturer = "Xiaomi", brand = "Xiaomi") {
                        openedSettings +=
                            1
                    },
                )
            val heading = section.view.findViewWithTag<TextView>(CollapsibleSection.HEADING_TAG)
            val details = section.view.findViewWithTag<View>(CollapsibleSection.DETAILS_TAG)

            assertEquals(View.GONE, details.visibility)
            assertEquals("Keep the weather refreshing on Xiaomi", heading.text.toString())

            heading.performClick()
            assertEquals(View.VISIBLE, details.visibility)
            section.view
                .buttons()
                .single { button -> button.text == "Open app settings" }
                .performClick()
            assertEquals(1, openedSettings)
        }

    @Test
    fun `the guidance names Autostart and No restrictions`() =
        onMainThread {
            val section = checkNotNull(BatteryGuidanceSection.forDevice(themedContext, manufacturer = "Xiaomi", brand = "Redmi") { })
            val details = section.view.findViewWithTag<ViewGroup>(CollapsibleSection.DETAILS_TAG)
            val guidance = (details.getChildAt(0) as TextView).text.toString()

            assertTrue(guidance, guidance.contains("Autostart") && guidance.contains("No restrictions"))
        }
}
