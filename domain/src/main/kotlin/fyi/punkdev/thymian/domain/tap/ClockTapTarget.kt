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

        fun choose(
            showAlarmsHandled: Boolean,
            launchableClockPackages: Set<String>,
        ): ClockTapTarget =
            when {
                showAlarmsHandled -> {
                    ShowAlarms
                }

                else -> {
                    KNOWN_CLOCK_PACKAGES
                        .firstOrNull { packageName -> packageName in launchableClockPackages }
                        ?.let { packageName -> ClockApp(packageName) } ?: NoTarget
                }
            }
    }
}
