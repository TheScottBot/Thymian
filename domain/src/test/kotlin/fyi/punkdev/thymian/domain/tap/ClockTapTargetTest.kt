package fyi.punkdev.thymian.domain.tap

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticsReport
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClockTapTargetTest {
    private val clockAppChosen =
        WidgetSettings
            .defaults()
            .with(SettingDeclarations.CLOCK_TAP_OPENS_APP, true)
            .with(SettingDeclarations.CLOCK_TAP_APP_PACKAGE, "com.android.deskclock")

    @Test
    fun `the clock opens the alarm list when the phone has something to show it`() {
        assertEquals(
            ClockTapTarget.ShowAlarms,
            ClockTapTarget.choose(showAlarmsHandled = true, launchableClockPackages = setOf("com.android.deskclock")),
        )
    }

    @Test
    fun `without an alarm list the clock opens the clock app instead`() {
        // Reported 22 November 2026: on the Xiaomi, touching the time did nothing.
        assertEquals(
            ClockTapTarget.ClockApp("com.android.deskclock"),
            ClockTapTarget.choose(showAlarmsHandled = false, launchableClockPackages = setOf("com.android.deskclock")),
        )
    }

    @Test
    fun `the first known clock app in order is chosen when several are installed`() {
        val target =
            ClockTapTarget.choose(
                showAlarmsHandled = false,
                launchableClockPackages = setOf("com.sec.android.app.clockpackage", "com.google.android.deskclock"),
            )

        assertEquals(ClockTapTarget.ClockApp("com.google.android.deskclock"), target)
    }

    @Test
    fun `with neither an alarm list nor a known clock app the clock has no tap action`() {
        assertEquals(ClockTapTarget.NoTarget, ClockTapTarget.choose(showAlarmsHandled = false, launchableClockPackages = emptySet()))
    }

    @Test
    fun `the clock app follows the alarm list so a refused alarm list still opens something`() {
        // Reported 22 November 2026: on the Xiaomi the alarm list resolved but refused to start with a SecurityException.
        assertEquals(
            listOf(ClockTapTarget.ShowAlarms, ClockTapTarget.ClockApp("com.android.deskclock")),
            ClockTapTarget.candidates(showAlarmsHandled = true, launchableClockPackages = setOf("com.android.deskclock")),
        )
    }

    @Test
    fun `every installed known clock app is a candidate in order of preference`() {
        assertEquals(
            listOf(ClockTapTarget.ClockApp("com.google.android.deskclock"), ClockTapTarget.ClockApp("com.sec.android.app.clockpackage")),
            ClockTapTarget.candidates(
                showAlarmsHandled = false,
                launchableClockPackages = setOf("com.sec.android.app.clockpackage", "com.google.android.deskclock"),
            ),
        )
    }

    @Test
    fun `with nothing to open there are no candidates`() {
        assertEquals(
            emptyList<ClockTapTarget>(),
            ClockTapTarget.candidates(showAlarmsHandled = false, launchableClockPackages = emptySet()),
        )
    }

    @Test
    fun `by default the clock tap has no chosen app`() {
        assertEquals(null, ClockTapTarget.chosenApp(WidgetSettings.defaults()) { true })
    }

    @Test
    fun `with the option on and an app chosen the clock tap opens that app`() {
        // Asked for on 22 November 2026, so the Xiaomi can bypass the alarm list it refuses to open.
        assertEquals(ClockTapTarget.ChosenApp("com.android.deskclock"), ClockTapTarget.chosenApp(clockAppChosen) { true })
    }

    @Test
    fun `with the option on but no app chosen the clock tap has no chosen app`() {
        val settings = WidgetSettings.defaults().with(SettingDeclarations.CLOCK_TAP_OPENS_APP, true)

        assertEquals(null, ClockTapTarget.chosenApp(settings) { true })
    }

    @Test
    fun `a chosen clock app that can no longer be opened is not used`() {
        assertEquals(null, ClockTapTarget.chosenApp(clockAppChosen) { false })
    }

    @Test
    fun `a clock app chosen while the option is off is kept but not used`() {
        val settings = clockAppChosen.with(SettingDeclarations.CLOCK_TAP_OPENS_APP, false)

        assertEquals(null, ClockTapTarget.chosenApp(settings) { true })
        assertEquals("com.android.deskclock", settings[SettingDeclarations.CLOCK_TAP_APP_PACKAGE])
    }

    @Test
    fun `a chosen app is described for diagnostics without naming it`() {
        assertEquals("clock_tap target=chosen_app", ClockTapTarget.ChosenApp("com.example.clock").diagnosticLine)
    }

    @Test
    fun `the Xiaomi clock is among the known clock apps`() {
        assertTrue("com.android.deskclock" in ClockTapTarget.KNOWN_CLOCK_PACKAGES)
    }

    @Test
    fun `each target describes itself for diagnostics`() {
        assertEquals("clock_tap target=show_alarms", ClockTapTarget.ShowAlarms.diagnosticLine)
        assertEquals(
            "clock_tap target=clock_app package=com.android.deskclock",
            ClockTapTarget.ClockApp("com.android.deskclock").diagnosticLine,
        )
        assertEquals("clock_tap target=none", ClockTapTarget.NoTarget.diagnosticLine)
    }

    @Test
    fun `the copied diagnostics state the clock tap target under the version line`() {
        val report = DiagnosticsReport.asText("1.0.2", events = emptyList(), deviceFacts = listOf(ClockTapTarget.NoTarget.diagnosticLine))

        assertEquals(
            listOf("Thymian diagnostics, version 1.0.2", "clock_tap target=none"),
            report.lines().filter { line -> line.isNotEmpty() },
        )
    }
}
