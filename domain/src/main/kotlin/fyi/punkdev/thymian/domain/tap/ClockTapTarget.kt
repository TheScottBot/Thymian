package fyi.punkdev.thymian.domain.tap

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

        fun choose(
            showAlarmsHandled: Boolean,
            launchableClockPackages: Set<String>,
        ): ClockTapTarget = candidates(showAlarmsHandled, launchableClockPackages).firstOrNull() ?: NoTarget
    }
}
