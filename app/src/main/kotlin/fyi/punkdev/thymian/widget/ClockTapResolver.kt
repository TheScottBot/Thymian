package fyi.punkdev.thymian.widget

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import fyi.punkdev.thymian.domain.tap.ClockTapTarget

/** Asks the phone what can answer the clock tap. The manifest's queries make these apps visible to the check. */
object ClockTapResolver {
    fun target(context: Context): ClockTapTarget = candidates(context).firstOrNull() ?: ClockTapTarget.NoTarget

    /** Every target worth trying, best first; a target that refuses to start moves the tap on to the next. */
    fun candidates(context: Context): List<ClockTapTarget> {
        val packageManager = context.packageManager
        val showAlarmsHandled =
            packageManager.resolveActivity(
                Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            ) != null
        val launchableClockPackages =
            ClockTapTarget.KNOWN_CLOCK_PACKAGES
                .filter { packageName -> packageManager.getLaunchIntentForPackage(packageName) != null }
                .toSet()
        return ClockTapTarget.candidates(showAlarmsHandled, launchableClockPackages)
    }

    fun intentFor(
        context: Context,
        target: ClockTapTarget,
    ): Intent? =
        when (target) {
            ClockTapTarget.ShowAlarms -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            is ClockTapTarget.ClockApp -> context.packageManager.getLaunchIntentForPackage(target.packageName)
            is ClockTapTarget.ChosenApp -> context.packageManager.getLaunchIntentForPackage(target.packageName)
            ClockTapTarget.NoTarget -> null
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
