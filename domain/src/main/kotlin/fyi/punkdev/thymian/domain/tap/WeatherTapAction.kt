package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings

/** What touching the weather does (CWD12): always a refresh, and an app as well when the user chose one. */
sealed interface WeatherTapAction {
    /** For diagnostics; never names the chosen app. */
    val diagnosticName: String

    data object Refresh : WeatherTapAction {
        override val diagnosticName = "refresh"
    }

    data class RefreshAndOpen(
        val packageName: String,
    ) : WeatherTapAction {
        override val diagnosticName = "refresh_and_open"
    }

    companion object {
        fun choose(
            settings: WidgetSettings,
            isLaunchable: (packageName: String) -> Boolean,
        ): WeatherTapAction {
            val chosenPackage = settings[SettingDeclarations.WEATHER_TAP_APP_PACKAGE]
            val opensApp = settings[SettingDeclarations.WEATHER_TAP_OPENS_APP] && chosenPackage.isNotEmpty() && isLaunchable(chosenPackage)
            return if (opensApp) RefreshAndOpen(chosenPackage) else Refresh
        }
    }
}
