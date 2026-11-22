package fyi.punkdev.thymian.spike.location

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Spike only. Runs with no activity visible, which is what makes it background
 * for the location permission model. Stores the outcome category for the
 * activity to show; a platform job stands in for WorkManager here because the
 * question is about the permission, not the scheduler.
 */
class BackgroundLocationJobService : JobService() {

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        val permissionsAtRun = LocationProbe.describePermissions(this)
        LocationProbe.requestOneFix(this) { outcome ->
            val detail = when (outcome) {
                is LocationProbe.ProbeOutcome.Fixed ->
                    "fixed via ${outcome.provider}, accuracy ${outcome.location.accuracy.toInt()} m, age ${outcome.ageSeconds} s"
                is LocationProbe.ProbeOutcome.Failed -> "failed: ${outcome.category}"
            }
            val ranAt = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            getSharedPreferences(RESULT_STORE_NAME, Context.MODE_PRIVATE).edit()
                .putString(RESULT_KEY, "$ranAt, $permissionsAtRun, $detail")
                .apply()
            jobFinished(jobParameters, false)
        }
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean = false

    companion object {
        const val JOB_ID = 4021
        const val RESULT_STORE_NAME = "background_probe"
        const val RESULT_KEY = "last_result"
    }
}
