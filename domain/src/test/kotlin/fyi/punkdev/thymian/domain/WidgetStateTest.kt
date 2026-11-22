package fyi.punkdev.thymian.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WidgetStateTest {
    @Test
    fun `a new widget follows the device clock setting`() {
        assertEquals(ClockMode.FOLLOW_DEVICE, WidgetState().clockMode)
    }
}

class WidgetStateSettingsTest {
    @Test
    fun `a widget state reads its clock mode from its settings`() {
        val settings =
            fyi.punkdev.thymian.domain.settings.WidgetSettings
                .defaults()
                .with(fyi.punkdev.thymian.domain.settings.SettingDeclarations.CLOCK_MODE, ClockMode.TWELVE_HOUR)

        assertEquals(ClockMode.TWELVE_HOUR, WidgetState(settings).clockMode)
    }
}

class WidgetStateSizesTest {
    @Test
    fun `a widget state with no reported sizes leaves the choice of size to the renderer`() {
        assertEquals(emptyList<fyi.punkdev.thymian.domain.sizing.WidgetSize>(), WidgetState().sizes)
    }
}
