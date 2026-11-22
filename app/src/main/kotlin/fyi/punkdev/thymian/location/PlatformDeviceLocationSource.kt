package fyi.punkdev.thymian.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.CancellationSignal
import android.os.SystemClock
import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.location.DeviceLocationSource
import fyi.punkdev.thymian.domain.location.LocatedFix
import fyi.punkdev.thymian.domain.location.LocationPermission
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Adapter: the platform LocationManager, no Play services (CWD25). Approximate
 * permission only. Blocking; must be called off the main thread.
 */
class PlatformDeviceLocationSource(
    private val context: Context,
) : DeviceLocationSource {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    override fun permission(): LocationPermission =
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationPermission.GRANTED
        } else {
            LocationPermission.DENIED
        }

    override fun lastKnownFix(): LocatedFix? =
        try {
            locationManager
                .getProviders(true)
                .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
                .maxByOrNull { location -> location.elapsedRealtimeNanos }
                ?.toLocatedFix()
        } catch (permissionMissing: SecurityException) {
            // Running in the background without Allow all the time: the chain falls back.
            null
        }

    override fun requestCurrentFix(): LocatedFix? {
        val provider =
            listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .firstOrNull { candidate -> candidate in locationManager.getProviders(true) } ?: return null
        val request =
            LocationRequest
                .Builder(0L)
                .setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(FRESH_FIX_WAIT.toMillis())
                .build()
        val arrived = AtomicReference<Location?>()
        val finished = CountDownLatch(1)
        return try {
            locationManager.getCurrentLocation(provider, request, CancellationSignal(), context.mainExecutor) { location ->
                arrived.set(location)
                finished.countDown()
            }
            finished.await(FRESH_FIX_WAIT.toMillis() + LATCH_MARGIN_MILLISECONDS, TimeUnit.MILLISECONDS)
            arrived.get()?.toLocatedFix()
        } catch (permissionMissing: SecurityException) {
            null
        }
    }

    private fun Location.toLocatedFix(): LocatedFix? {
        val coordinates = Coordinates.orNull(latitude, longitude) ?: return null
        val age = Duration.ofNanos(SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos)
        return LocatedFix(coordinates, capturedAt = Instant.now().minus(age))
    }

    private companion object {
        // The Plan spike waited 30 seconds and got nothing; waiting longer would only hold up the refresh.
        val FRESH_FIX_WAIT: Duration = Duration.ofSeconds(30)
        const val LATCH_MARGIN_MILLISECONDS = 2_000L
    }
}
