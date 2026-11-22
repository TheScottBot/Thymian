package fyi.punkdev.thymian.domain.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WidgetAppearanceTest {
    private fun backgroundWith(
        colour: Int,
        opacityPercent: Int,
    ): Int =
        WidgetAppearance
            .from(
                WidgetSettings
                    .defaults()
                    .with(SettingDeclarations.BACKGROUND_COLOUR, colour)
                    .with(SettingDeclarations.BACKGROUND_OPACITY_PERCENT, opacityPercent),
            ).backgroundArgb

    @Test
    fun `zero opacity is a fully transparent background`() {
        assertEquals(0x00000000, backgroundWith(colour = 0x000000, opacityPercent = 0))
    }

    @Test
    fun `full opacity keeps the colour and an opaque alpha`() {
        assertEquals(0xFF203040.toInt(), backgroundWith(colour = 0x203040, opacityPercent = 100))
    }

    @Test
    fun `half opacity rounds to alpha 128`() {
        assertEquals(0x80FFFFFF.toInt(), backgroundWith(colour = 0xFFFFFF, opacityPercent = 50))
    }

    @Test
    fun `text colour is always opaque`() {
        val appearance = WidgetAppearance.from(WidgetSettings.defaults().with(SettingDeclarations.TEXT_COLOUR, 0x112233))

        assertEquals(0xFF112233.toInt(), appearance.textArgb)
    }

    @Test
    fun `corner radius comes straight from the setting`() {
        val appearance = WidgetAppearance.from(WidgetSettings.defaults().with(SettingDeclarations.CORNER_RADIUS_DP, 24))

        assertEquals(24, appearance.cornerRadiusDp)
    }
}
