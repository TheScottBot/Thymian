package fyi.punkdev.thymian.spike.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import android.os.SystemClock
import android.util.Log

/**
 * Spike only. One fresh fix from the platform LocationManager, shared by the
 * foreground button and the background job so both test exactly the same call.
 * Coordinates are never logged or shown: only provider, accuracy and age.
 */
object LocationProbe {

    const val LOG_TAG = "ThymianLocationSpike"

    /** Network first: runs 1 and 2 got no fused fix within 30 s with approximate permission. */
    fun chooseProvider(locationManager: LocationManager): String? {
        val enabledProviders = locationManager.getProviders(true)
        return listOf(LocationManager.NETWORK_PROVIDER, LocationManager.FUSED_PROVIDER)
            .firstOrNull { candidateProvider -> candidateProvider in enabledProviders }
    }

    fun requestOneFix(context: Context, onOutcome: (outcome: ProbeOutcome) -> Unit) {
        val locationManager = context.getSystemService(LocationManager::class.java)
        if (!locationManager.isLocationEnabled) {
            onOutcome(ProbeOutcome.Failed("location_off"))
            return
        }
        val provider = chooseProvider(locationManager)
        if (provider == null) {
            onOutcome(ProbeOutcome.Failed("no_provider"))
            return
        }
        val locationRequest = LocationRequest.Builder(0L)
            // Run 1 used QUALITY_LOW_POWER and got no fix within 30 s on both devices.
            .setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY)
            .setDurationMillis(30_000L)
            .build()
        try {
            locationManager.getCurrentLocation(
                provider,
                locationRequest,
                CancellationSignal(),
                context.mainExecutor,
            ) { freshLocation: Location? ->
                val outcome = if (freshLocation == null) {
                    ProbeOutcome.Failed("no_fix_within_timeout")
                } else {
                    ProbeOutcome.Fixed(freshLocation, provider)
                }
                Log.i(LOG_TAG, "event=fix_outcome provider=$provider outcome=${outcome.category}")
                onOutcome(outcome)
            }
        } catch (securityException: SecurityException) {
            Log.w(LOG_TAG, "event=fix_outcome provider=$provider outcome=permission_denied")
            onOutcome(ProbeOutcome.Failed("permission_denied"))
        }
    }

    /** Last known fix per provider, as accuracy and age only, for the fallback chain question. */
    fun describeLastKnown(context: Context): String {
        val locationManager = context.getSystemService(LocationManager::class.java)
        return try {
            locationManager.getProviders(true).joinToString(separator = "; ") { providerName ->
                val lastKnown = locationManager.getLastKnownLocation(providerName)
                if (lastKnown == null) {
                    "$providerName none"
                } else {
                    val ageMinutes = (SystemClock.elapsedRealtimeNanos() - lastKnown.elapsedRealtimeNanos) / 60_000_000_000L
                    "$providerName ${lastKnown.accuracy.toInt()} m ${ageMinutes} min old"
                }
            }
        } catch (securityException: SecurityException) {
            "permission_denied"
        }
    }

    fun describePermissions(context: Context): String {
        fun stateOf(permission: String): String =
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) "granted" else "denied"
        return "fine=${stateOf(Manifest.permission.ACCESS_FINE_LOCATION)} " +
            "coarse=${stateOf(Manifest.permission.ACCESS_COARSE_LOCATION)} " +
            "background=${stateOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)}"
    }

    sealed class ProbeOutcome(val category: String) {
        class Fixed(val location: Location, val provider: String) : ProbeOutcome("fixed") {
            val ageSeconds: Long
                get() = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000_000L
        }

        class Failed(reason: String) : ProbeOutcome(reason)
    }
}
