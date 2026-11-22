package fyi.punkdev.thymian.domain.location

import fyi.punkdev.thymian.domain.TimeSource
import java.time.Duration

/**
 * The location fallback chain (specification 1.3, CWD25). A recent last known
 * fix is used as it is: the Plan spike got no fresh approximate fix within 30
 * seconds on either device, and a town level fix an hour old is still right for
 * the weather.
 */
class LocationResolver(
    private val deviceLocationSource: DeviceLocationSource,
    private val timeSource: TimeSource,
) {
    fun resolve(preference: LocationPreference): LocationResolution {
        val fixedPlace = preference.fixedPlace
        if (preference.mode == LocationMode.FIXED && fixedPlace != null) {
            return LocationResolution.Resolved(fixedPlace.coordinates, LocationSourceUsed.FIXED_CHOSEN)
        }
        if (deviceLocationSource.permission() == LocationPermission.DENIED) {
            return fixedPlace?.let { place -> LocationResolution.Resolved(place.coordinates, LocationSourceUsed.FIXED_FALLBACK) }
                ?: LocationResolution.Unavailable(LocationUnavailableReason.PERMISSION_DENIED)
        }
        val lastKnown = deviceLocationSource.lastKnownFix()
        if (lastKnown != null && Duration.between(lastKnown.capturedAt, timeSource.now()) <= RECENT_FIX_MAXIMUM_AGE) {
            return LocationResolution.Resolved(lastKnown.coordinates, LocationSourceUsed.DEVICE_RECENT)
        }
        deviceLocationSource.requestCurrentFix()?.let { currentFix ->
            return LocationResolution.Resolved(currentFix.coordinates, LocationSourceUsed.DEVICE_CURRENT)
        }
        if (lastKnown != null) return LocationResolution.Resolved(lastKnown.coordinates, LocationSourceUsed.DEVICE_LAST_KNOWN)
        return fixedPlace?.let { place -> LocationResolution.Resolved(place.coordinates, LocationSourceUsed.FIXED_FALLBACK) }
            ?: LocationResolution.Unavailable(LocationUnavailableReason.NO_FIX)
    }

    companion object {
        val RECENT_FIX_MAXIMUM_AGE: Duration = Duration.ofMinutes(60)
    }
}
