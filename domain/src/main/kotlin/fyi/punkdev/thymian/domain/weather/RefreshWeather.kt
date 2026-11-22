package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.TimeSource
import fyi.punkdev.thymian.domain.location.LocationPreference
import fyi.punkdev.thymian.domain.location.LocationResolution
import fyi.punkdev.thymian.domain.location.LocationResolver
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.location.LocationUnavailableReason
import fyi.punkdev.thymian.domain.location.PlaceNameSource

/** What one refresh did, for logging and diagnostics (specification 1.3); never contains coordinates. */
sealed interface RefreshOutcome {
    data class Refreshed(
        val snapshot: WeatherSnapshot,
    ) : RefreshOutcome

    data class LocationUnavailable(
        val reason: LocationUnavailableReason,
    ) : RefreshOutcome

    data class WeatherUnavailable(
        val failure: WeatherFetchFailure,
        val locationSource: LocationSourceUsed,
    ) : RefreshOutcome
}

/**
 * One weather refresh for the whole app (CWD36): resolve the location, fetch the
 * weather, name the place, keep the snapshot. Any failure leaves the last good
 * snapshot in place, so the widget keeps showing it (specification 1.3 and 1.4).
 */
class RefreshWeather(
    private val locationResolver: LocationResolver,
    private val weatherSource: WeatherSource,
    private val placeNameSource: PlaceNameSource,
    private val snapshotStore: WeatherSnapshotStore,
    private val timeSource: TimeSource,
) {
    fun refresh(preference: LocationPreference): RefreshOutcome {
        val resolution = locationResolver.resolve(preference)
        if (resolution is LocationResolution.Unavailable) return RefreshOutcome.LocationUnavailable(resolution.reason)
        val resolved = resolution as LocationResolution.Resolved

        val fetched =
            when (val fetchResult = weatherSource.fetch(resolved.coordinates)) {
                is WeatherFetchResult.Failed -> return RefreshOutcome.WeatherUnavailable(fetchResult.failure, resolved.source)
                is WeatherFetchResult.Fetched -> fetchResult.currentWeather
            }
        val placeName =
            when (resolved.source) {
                // A chosen place keeps the name the user picked rather than the geocoder's.
                LocationSourceUsed.FIXED_CHOSEN, LocationSourceUsed.FIXED_FALLBACK -> preference.fixedPlace?.name

                else -> placeNameSource.placeNameFor(resolved.coordinates)
            }
        val snapshot = WeatherSnapshot(fetched, placeName, resolved.source, timeSource.now())
        snapshotStore.save(snapshot)
        return RefreshOutcome.Refreshed(snapshot)
    }
}
