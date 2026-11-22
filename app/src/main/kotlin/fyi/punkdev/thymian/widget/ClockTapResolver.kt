package fyi.punkdev.thymian.widget

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import fyi.punkdev.thymian.domain.tap.ClockTapTarget

/** Asks the phone what can answer the clock tap. The manifest's queries make these apps visible to the check. */
object ClockTapResolver {
    fun target(context: Context): ClockTapTarget {
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
        return ClockTapTarget.choose(showAlarmsHandled, launchableClockPackages)
    }

    fun intentFor(
        context: Context,
        target: ClockTapTarget,
    ): Intent? =
        when (target) {
            ClockTapTarget.ShowAlarms -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            is ClockTapTarget.ClockApp -> context.packageManager.getLaunchIntentForPackage(target.packageName)
            ClockTapTarget.NoTarget -> null
        }?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
