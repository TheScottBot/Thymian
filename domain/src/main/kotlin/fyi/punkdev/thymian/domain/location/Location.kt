package fyi.punkdev.thymian.domain.location

import java.time.Instant

/** A point on the globe; constructing one outside the valid ranges fails. */
data class Coordinates(
    val latitudeDegrees: Double,
    val longitudeDegrees: Double,
) {
    init {
        require(latitudeDegrees in LATITUDE_RANGE && longitudeDegrees in LONGITUDE_RANGE) {
            "coordinates out of range"
        }
    }

    companion object {
        private val LATITUDE_RANGE = -90.0..90.0
        private val LONGITUDE_RANGE = -180.0..180.0

        /** For values from outside the app: null instead of an exception when out of range. */
        fun orNull(
            latitudeDegrees: Double,
            longitudeDegrees: Double,
        ): Coordinates? =
            if (latitudeDegrees in LATITUDE_RANGE && longitudeDegrees in LONGITUDE_RANGE) {
                Coordinates(latitudeDegrees, longitudeDegrees)
            } else {
                null
            }
    }
}

data class LocatedFix(
    val coordinates: Coordinates,
    val capturedAt: Instant,
)

/** A place the user chose by searching its name (specification 1.3). */
data class FixedPlace(
    val name: String,
    val coordinates: Coordinates,
)

enum class LocationMode {
    AUTOMATIC,
    FIXED,
}

data class LocationPreference(
    val mode: LocationMode,
    val fixedPlace: FixedPlace? = null,
)

enum class LocationPermission {
    GRANTED,
    DENIED,
}

/** Port: the device's own location (specification 2.1). Blocking; called from background work. */
interface DeviceLocationSource {
    fun permission(): LocationPermission

    fun lastKnownFix(): LocatedFix?

    /** May take time and may return null when no fix arrives. */
    fun requestCurrentFix(): LocatedFix?
}

/** Which source was used, recorded for diagnostics (specification 1.3). */
enum class LocationSourceUsed {
    FIXED_CHOSEN,
    DEVICE_RECENT,
    DEVICE_CURRENT,
    DEVICE_LAST_KNOWN,
    FIXED_FALLBACK,
}

enum class LocationUnavailableReason {
    PERMISSION_DENIED,
    NO_FIX,
}

sealed interface LocationResolution {
    data class Resolved(
        val coordinates: Coordinates,
        val source: LocationSourceUsed,
    ) : LocationResolution

    data class Unavailable(
        val reason: LocationUnavailableReason,
    ) : LocationResolution
}
