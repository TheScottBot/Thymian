package fyi.punkdev.thymian.domain

import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import fyi.punkdev.thymian.domain.sizing.WidgetSize

/**
 * Everything the renderer needs to draw one widget instance. It grows with each
 * phase (weather in CWE5); the renderer stays the single place that turns it into
 * views (specification 2.2). [sizes] are the sizes the launcher reported; empty
 * means the launcher said nothing and the renderer picks a default.
 */
data class WidgetState(
    val settings: WidgetSettings = WidgetSettings.defaults(),
    val sizes: List<WidgetSize> = emptyList(),
) {
    /** Kept so CWE2's callers and tests, which only set a clock mode, read unchanged. */
    constructor(clockMode: ClockMode) : this(WidgetSettings.defaults().with(SettingDeclarations.CLOCK_MODE, clockMode))

    val clockMode: ClockMode get() = settings[SettingDeclarations.CLOCK_MODE]
}
