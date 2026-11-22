package fyi.punkdev.thymian.spike.location

import android.Manifest
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

/**
 * Spike only, for CWP5. Each button exercises one step so the run can be driven
 * and screenshotted step by step. The device's own place name is shown on screen
 * but never logged; fixed reference coordinates are used for Geocoder evidence.
 */
class LocationSpikeActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusText = TextView(this).apply { textSize = 13f }
        resultText = TextView(this).apply { textSize = 13f }

        val column = LinearLayout(this)
        column.orientation = LinearLayout.VERTICAL
        column.addView(statusText)
        column.addView(makeButton("1 Request approximate location") { requestForegroundLocation() })
        column.addView(TextView(this).apply { text = BACKGROUND_EXPLANATION; textSize = 13f })
        column.addView(makeButton("2 Request background location") { requestBackgroundLocation() })
        column.addView(makeButton("3 Current fix and place name (on screen only)") { probeForegroundFix() })
        column.addView(makeButton("4 Geocode reference coordinates") { geocodeReferenceCoordinates() })
        column.addView(makeButton("5 Schedule background fix in 20 s") { scheduleBackgroundProbe() })
        column.addView(resultText)

        val scrollContainer = ScrollView(this)
        scrollContainer.addView(column)
        scrollContainer.setOnApplyWindowInsetsListener { insetView, windowInsets ->
            val barInsets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            insetView.setPadding(barInsets.left + 24, barInsets.top + 24, barInsets.right + 24, barInsets.bottom + 24)
            windowInsets
        }
        setContentView(scrollContainer)
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshStatus()
    }

    private fun refreshStatus() {
        val locationManager = getSystemService(LocationManager::class.java)
        val lastBackgroundResult = getSharedPreferences(BackgroundLocationJobService.RESULT_STORE_NAME, Context.MODE_PRIVATE)
            .getString(BackgroundLocationJobService.RESULT_KEY, "none yet")
        statusText.text = buildString {
            appendLine("Thymian location spike (CWP5)")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}")
            appendLine("Permissions: ${LocationProbe.describePermissions(this@LocationSpikeActivity)}")
            appendLine("Background option label: ${packageManager.backgroundPermissionOptionLabel}")
            appendLine("Location enabled: ${locationManager.isLocationEnabled}")
            appendLine("Enabled providers: ${locationManager.getProviders(true)}")
            appendLine("Chosen provider: ${LocationProbe.chooseProvider(locationManager)}")
            appendLine("Geocoder.isPresent: ${Geocoder.isPresent()}")
            appendLine("Last known: ${LocationProbe.describeLastKnown(this@LocationSpikeActivity)}")
            appendLine("Last background result: $lastBackgroundResult")
        }
    }

    private fun requestForegroundLocation() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), FOREGROUND_REQUEST_CODE)
    }

    private fun requestBackgroundLocation() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), BACKGROUND_REQUEST_CODE)
    }

    private fun probeForegroundFix() {
        resultText.text = "Requesting a fix..."
        LocationProbe.requestOneFix(this) { outcome ->
            when (outcome) {
                is LocationProbe.ProbeOutcome.Failed -> resultText.text = "Foreground fix failed: ${outcome.category}"
                is LocationProbe.ProbeOutcome.Fixed -> {
                    val fixSummary = "Foreground fix via ${outcome.provider}, accuracy ${outcome.location.accuracy.toInt()} m, age ${outcome.ageSeconds} s"
                    resultText.text = fixSummary
                    geocode(outcome.location.latitude, outcome.location.longitude) { placeDescription ->
                        resultText.text = "$fixSummary\nPlace name: $placeDescription"
                    }
                }
            }
        }
    }

    private fun geocodeReferenceCoordinates() {
        resultText.text = ""
        for (referencePlace in REFERENCE_PLACES) {
            geocode(referencePlace.latitude, referencePlace.longitude) { placeDescription ->
                resultText.append("${referencePlace.label}: $placeDescription\n")
            }
        }
    }

    private fun geocode(latitude: Double, longitude: Double, onDescribed: (placeDescription: String) -> Unit) {
        if (!Geocoder.isPresent()) {
            onDescribed("Geocoder not present")
            return
        }
        Geocoder(this, Locale.getDefault()).getFromLocation(
            latitude,
            longitude,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    val firstAddress = addresses.firstOrNull()
                    val placeDescription = if (firstAddress == null) {
                        "no result"
                    } else {
                        "locality=${firstAddress.locality}, subAdminArea=${firstAddress.subAdminArea}, " +
                            "adminArea=${firstAddress.adminArea}, country=${firstAddress.countryCode}"
                    }
                    runOnUiThread { onDescribed(placeDescription) }
                }

                override fun onError(errorMessage: String?) {
                    runOnUiThread { onDescribed("error: $errorMessage") }
                }
            },
        )
    }

    private fun scheduleBackgroundProbe() {
        val jobInfo = JobInfo.Builder(
            BackgroundLocationJobService.JOB_ID,
            ComponentName(this, BackgroundLocationJobService::class.java),
        )
            .setMinimumLatency(20_000L)
            .setOverrideDeadline(60_000L)
            .build()
        val scheduleResult = getSystemService(JobScheduler::class.java).schedule(jobInfo)
        resultText.text = "Background job scheduled (result $scheduleResult). Leave the app now; reopen after a minute."
    }

    private fun makeButton(label: String, onPressed: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onPressed() }
        }

    private data class ReferencePlace(val label: String, val latitude: Double, val longitude: Double)

    private companion object {
        const val FOREGROUND_REQUEST_CODE = 1
        const val BACKGROUND_REQUEST_CODE = 2
        const val BACKGROUND_EXPLANATION =
            "Background location lets the widget update the weather for where you are while you are " +
                "not using the app. The next screen asks you to choose the all the time option."
        val REFERENCE_PLACES = listOf(
            ReferencePlace("London", 51.5074, -0.1278),
            ReferencePlace("Berlin", 52.5200, 13.4050),
            ReferencePlace("Munich", 48.1374, 11.5755),
        )
    }
}
