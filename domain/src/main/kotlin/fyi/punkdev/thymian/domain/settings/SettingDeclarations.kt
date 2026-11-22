package fyi.punkdev.thymian.domain.settings

import fyi.punkdev.thymian.domain.ClockMode
import fyi.punkdev.thymian.domain.TemperatureUnit

/**
 * Every widget setting (specification 1.3). Adding a setting means adding one
 * declaration here, listing it in [all], and rendering it; nothing else.
 * Labels are the user facing strings, in UK English, kept here so each lives in
 * exactly one place (datum 0.7).
 */
object SettingDeclarations {
    val CLOCK_MODE =
        ChoiceSetting(
            key = "clock_mode",
            label = "Clock",
            defaultValue = ClockMode.DEFAULT,
            options =
                listOf(
                    ChoiceOption(ClockMode.FOLLOW_DEVICE, "Follow device"),
                    ChoiceOption(ClockMode.TWELVE_HOUR, "12 hour"),
                    ChoiceOption(ClockMode.TWENTY_FOUR_HOUR, "24 hour"),
                ),
        )

    val TEMPERATURE_UNIT =
        ChoiceSetting(
            key = "temperature_unit",
            label = "Temperature unit",
            defaultValue = TemperatureUnit.CELSIUS,
            options =
                listOf(
                    ChoiceOption(TemperatureUnit.CELSIUS, "Celsius"),
                    ChoiceOption(TemperatureUnit.FAHRENHEIT, "Fahrenheit"),
                ),
        )

    val BACKGROUND_COLOUR = ColourSetting(key = "background_colour", label = "Background colour", defaultValue = 0x000000)

    // Zero by default: the widget is fully transparent until the user asks otherwise.
    val BACKGROUND_OPACITY_PERCENT =
        WholeNumberSetting(
            key = "background_opacity_percent",
            label = "Background opacity",
            defaultValue = 0,
            bounds = 0..100,
            unitLabel = "%",
        )

    val CORNER_RADIUS_DP =
        WholeNumberSetting(key = "corner_radius_dp", label = "Corner radius", defaultValue = 16, bounds = 0..32, unitLabel = "dp")

    val TEXT_COLOUR = ColourSetting(key = "text_colour", label = "Text colour", defaultValue = 0xFFFFFF)

    val SHOW_DATE = ToggleSetting(key = "show_date", label = "Show date", defaultValue = true)
    val SHOW_PLACE_NAME = ToggleSetting(key = "show_place_name", label = "Show place name", defaultValue = true)
    val SHOW_CONDITION_ICON = ToggleSetting(key = "show_condition_icon", label = "Show weather icon", defaultValue = true)
    val SHOW_TEMPERATURE = ToggleSetting(key = "show_temperature", label = "Show temperature", defaultValue = true)

    val elementVisibility: List<ToggleSetting> = listOf(SHOW_DATE, SHOW_PLACE_NAME, SHOW_CONDITION_ICON, SHOW_TEMPERATURE)

    /** In the order the settings screen shows them. */
    val all: List<SettingDeclaration<*>> =
        listOf(
            CLOCK_MODE,
            TEMPERATURE_UNIT,
            BACKGROUND_COLOUR,
            BACKGROUND_OPACITY_PERCENT,
            CORNER_RADIUS_DP,
            TEXT_COLOUR,
        ) + elementVisibility
}
