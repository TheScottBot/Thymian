package fyi.punkdev.thymian.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** The apps with a home screen icon, for choosing which one the weather tap opens. */
object LaunchableApps {
    data class LaunchableApp(
        val label: String,
        val packageName: String,
    )

    /** Every app but Thymian itself, one entry per app, sorted by name. */
    fun list(context: Context): List<LaunchableApp> {
        val packageManager = context.packageManager
        return packageManager
            .queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.ResolveInfoFlags.of(0),
            ).map { resolved -> LaunchableApp(resolved.loadLabel(packageManager).toString(), resolved.activityInfo.packageName) }
            .filter { app -> app.packageName != context.packageName }
            .distinctBy { app -> app.packageName }
            .sortedBy { app -> app.label.lowercase() }
    }

    /** The app's name, or null when it is no longer installed. */
    fun labelFor(
        context: Context,
        packageName: String,
    ): String? =
        try {
            val packageManager = context.packageManager
            packageManager
                .getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                .loadLabel(packageManager)
                .toString()
        } catch (notInstalled: PackageManager.NameNotFoundException) {
            null
        }
}
