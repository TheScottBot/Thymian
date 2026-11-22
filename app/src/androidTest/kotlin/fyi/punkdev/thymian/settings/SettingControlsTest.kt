package fyi.punkdev.thymian.settings

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Switch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fyi.punkdev.thymian.domain.ClockMode
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** The settings screen's controls are generated from the declarations (specification 2.2). */
@RunWith(AndroidJUnit4::class)
class SettingControlsTest {
    private val themedContext =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext(), android.R.style.Theme_DeviceDefault)
    private var latestSettings = WidgetSettings.defaults()

    private fun onMainThread(testBody: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(testBody)

    private fun buildControls(): List<View> =
        SettingControls(themedContext) { updatedSettings -> latestSettings = updatedSettings }.build(WidgetSettings.defaults())

    private inline fun <reified T : View> List<View>.firstInControl(settingKey: String): T =
        single { control -> control.tag == settingKey }.descendants().filterIsInstance<T>().first()

    private fun View.descendants(): List<View> =
        if (this is ViewGroup) {
            listOf(this) +
                (0 until childCount).flatMap { childIndex ->
                    getChildAt(childIndex).descendants()
                }
        } else {
            listOf(this)
        }

    @Test
    fun `every declared setting gets exactly one control in declaration order`() =
        onMainThread {
            val controlKeys = buildControls().map { control -> control.tag }

            assertEquals(SettingDeclarations.all.map { declaration -> declaration.key }, controlKeys)
        }

    @Test
    fun `a toggle control switches its setting`() =
        onMainThread {
            buildControls().firstInControl<Switch>(SettingDeclarations.SHOW_DATE.key).performClick()

            assertEquals(false, latestSettings[SettingDeclarations.SHOW_DATE])
        }

    @Test
    fun `a whole number control sets its value within the bounds`() =
        onMainThread {
            buildControls().firstInControl<SeekBar>(SettingDeclarations.BACKGROUND_OPACITY_PERCENT.key).progress = 40

            assertEquals(40, latestSettings[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
        }

    @Test
    fun `a choice control selects one option`() =
        onMainThread {
            val clockOptions =
                buildControls()
                    .single { control -> control.tag == SettingDeclarations.CLOCK_MODE.key }
                    .descendants()
                    .filterIsInstance<RadioButton>()

            clockOptions.single { option -> option.text == "24 hour" }.performClick()

            assertEquals(ClockMode.TWENTY_FOUR_HOUR, latestSettings[SettingDeclarations.CLOCK_MODE])
        }

    @Test
    fun `a colour control accepts six hex digits and ignores anything else`() =
        onMainThread {
            val hexField = buildControls().firstInControl<EditText>(SettingDeclarations.BACKGROUND_COLOUR.key)

            hexField.setText("203040")
            assertEquals(0x203040, latestSettings[SettingDeclarations.BACKGROUND_COLOUR])

            hexField.setText("zz")
            assertEquals(0x203040, latestSettings[SettingDeclarations.BACKGROUND_COLOUR])
        }

    @Test
    fun `a colour swatch sets its colour`() =
        onMainThread {
            val swatches =
                buildControls()
                    .single { control -> control.tag == SettingDeclarations.TEXT_COLOUR.key }
                    .descendants()
                    .filter { view -> view.contentDescription?.toString() == "Black" }

            swatches.single().performClick()

            assertEquals(0x000000, latestSettings[SettingDeclarations.TEXT_COLOUR])
        }

    @Test
    fun `a setting note is shown with its control`() =
        onMainThread {
            val noteTexts =
                buildControls()
                    .single { control -> control.tag == SettingDeclarations.CORNER_RADIUS_DP.key }
                    .descendants()
                    .filterIsInstance<android.widget.TextView>()
                    .map { textView -> textView.text.toString() }

            assertEquals(true, "The home screen may round corners further." in noteTexts)
        }
}
