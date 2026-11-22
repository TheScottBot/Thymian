package fyi.punkdev.thymian.spike.background

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.TextView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Spike only. Opening it once schedules the soak (KEEP, so reopening never
 * resets the schedule) and shows every run recorded so far.
 */
class SoakActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Same interval and constraint as the real refresh (CWD7, 1.3).
        val soakRequest = PeriodicWorkRequest.Builder(SoakWorker::class.java, 30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(SoakWorker.UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, soakRequest)

        val logText = TextView(this).apply { textSize = 12f }
        val recordedRuns = SoakLog.readLines(this)
        logText.text = "Thymian soak spike (CWP7): every 30 minutes, network required.\n" +
            "Runs recorded: ${recordedRuns.size}\n\n" + recordedRuns.joinToString(separator = "\n")

        val scrollContainer = ScrollView(this)
        scrollContainer.addView(logText)
        scrollContainer.setOnApplyWindowInsetsListener { insetView, windowInsets ->
            val barInsets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            insetView.setPadding(barInsets.left + 24, barInsets.top + 24, barInsets.right + 24, barInsets.bottom + 24)
            windowInsets
        }
        setContentView(scrollContainer)
    }
}
