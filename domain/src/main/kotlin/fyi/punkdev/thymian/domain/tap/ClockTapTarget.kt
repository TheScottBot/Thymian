package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings

/**
 * What touching the clock opens (CWD12). The alarm list where the phone can
 * show one; otherwise the clock app itself, because on the Xiaomi touching the
 * time did nothing (reported 22 November 2026); otherwise nothing.
 */
sealed interface ClockTapTarget {
    /** Category words only: the target kind, and for a clock app its package. */
    val detail: String

    val diagnosticLine: String get() = "clock_tap $detail"

    data object ShowAlarms : ClockTapTarget {
        override val detail = "target=show_alarms"
    }

    data class ClockApp(
        val packageName: String,
    ) : ClockTapTarget {
        override val detail = "target=clock_app package=$packageName"
    }

    /** The app picked in the widget's settings. Diagnostics never name it, as for the weather side. */
    data class ChosenApp(
        val packageName: String,
    ) : ClockTapTarget {
        override val detail = "target=chosen_app"
    }

    data object NoTarget : ClockTapTarget {
        override val detail = "target=none"
    }

    companion object {
        /** Clock apps by package, in order of preference. The first is the AOSP clock, which Xiaomi phones also use. */
        val KNOWN_CLOCK_PACKAGES =
            listOf(
                "com.android.deskclock",
                "com.google.android.deskclock",
                "com.sec.android.app.clockpackage",
                "com.oneplus.deskclock",
                "com.coloros.alarmclock",
            )

        /**
         * Every target worth trying, best first: the alarm list, then each installed
         * known clock app. On the Xiaomi the alarm list resolved but refused to start
         * (reported 22 November 2026), so the tap must be able to move on.
         */
        fun candidates(
            showAlarmsHandled: Boolean,
            launchableClockPackages: Set<String>,
        ): List<ClockTapTarget> =
            buildList {
                if (showAlarmsHandled) add(ShowAlarms)
                KNOWN_CLOCK_PACKAGES
                    .filter { packageName -> packageName in launchableClockPackages }
                    .forEach { packageName -> add(ClockApp(packageName)) }
            }

        /** The app picked in settings, when the option is on, an app is picked and it can still be opened. */
        fun chosenApp(
            settings: WidgetSettings,
            isLaunchable: (packageName: String) -> Boolean,
        ): ChosenApp? {
            val chosenPackage = settings[SettingDeclarations.CLOCK_TAP_APP_PACKAGE]
            val opensApp = settings[SettingDeclarations.CLOCK_TAP_OPENS_APP] && chosenPackage.isNotEmpty() && isLaunchable(chosenPackage)
            return if (opensApp) ChosenApp(chosenPackage) else null
        }

        fun choose(
            showAlarmsHandled: Boolean,
            launchableClockPackages: Set<String>,
        ): ClockTapTarget = candidates(showAlarmsHandled, launchableClockPackages).firstOrNull() ?: NoTarget
    }
}
