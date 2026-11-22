package fyi.punkdev.thymian.spike

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

/**
 * Spike only. Shows device facts plus the same widget layout rendered inside the
 * app, where bundled fonts always load. Comparing this screen with the home
 * screen widget shows what the launcher dropped. One screenshot of each is the
 * whole result, so a remote tester can send it back.
 */
class SpikeSummaryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factsText = TextView(this)
        factsText.textSize = 13f
        factsText.text = describeDevice()

        // A mid grey backdrop so white text and any unwanted background both show.
        val referenceBackdrop = FrameLayout(this)
        referenceBackdrop.setBackgroundColor(Color.rgb(90, 107, 125))
        val referenceWidget = layoutInflater.inflate(R.layout.spike_widget, referenceBackdrop, false)
        referenceBackdrop.addView(referenceWidget)
        SpikeWidgetProvider.buildSpikeRemoteViews(this).reapply(this, referenceWidget)

        val instructionsText = TextView(this)
        instructionsText.textSize = 13f
        instructionsText.text = INSTRUCTIONS

        val column = LinearLayout(this)
        column.orientation = LinearLayout.VERTICAL
        column.addView(factsText)
        column.addView(TextView(this).apply { text = "In app reference rendering:" })
        column.addView(referenceBackdrop)
        column.addView(instructionsText)

        val scrollContainer = ScrollView(this)
        scrollContainer.addView(column)
        applySystemBarPadding(scrollContainer)
        setContentView(scrollContainer)
    }

    private fun describeDevice(): String {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homeActivity = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        val launcherPackage = homeActivity?.activityInfo?.packageName ?: "unknown"
        val launcherVersion = try {
            packageManager.getPackageInfo(launcherPackage, 0).versionName ?: "unknown"
        } catch (notFound: PackageManager.NameNotFoundException) {
            "not readable"
        }
        return buildString {
            appendLine("Thymian spike: font, clock, transparency")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}")
            appendLine("Build: ${Build.DISPLAY}")
            appendLine("Launcher: $launcherPackage $launcherVersion")
            appendLine("Locale: ${Locale.getDefault().toLanguageTag()}")
            appendLine("Device uses 24 hour time: ${DateFormat.is24HourFormat(this@SpikeSummaryActivity)}")
        }
    }

    private fun applySystemBarPadding(targetView: View) {
        // targetSdk 37 forces edge to edge, so content must keep clear of the bars itself.
        targetView.setOnApplyWindowInsetsListener { insetView, windowInsets ->
            val barInsets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            insetView.setPadding(barInsets.left + 24, barInsets.top + 24, barInsets.right + 24, barInsets.bottom + 24)
            windowInsets
        }
    }

    private companion object {
        const val INSTRUCTIONS =
            "Next: add the Thymian spike widget to the home screen, once over a light " +
                "wallpaper and once over a dark one. Screenshot this screen and the " +
                "home screen each time."
    }
}
