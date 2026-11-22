package fyi.punkdev.thymian.domain.location

/** Port: names the place at some coordinates (specification 2.1); the platform Geocoder in the app (CWD6). */
interface PlaceNameSource {
    /** Null when no name is available; blocking, called from background work. */
    fun placeNameFor(coordinates: Coordinates): String?
}

/**
 * Picks the place name from a geocoded address: locality, then sub
 * administrative area, then administrative area, since the locality can be
 * missing (London had none in the Plan spike, section 7.2).
 */
object PlaceNameChoice {
    /** Long enough for any real town; the widget's small text could not show more. */
    const val MAXIMUM_LENGTH = 40

    fun choose(
        locality: String?,
        subAdminArea: String?,
        adminArea: String?,
    ): String? =
        listOf(locality, subAdminArea, adminArea)
            .firstOrNull { candidate -> !candidate.isNullOrBlank() }
            ?.trim()
            ?.take(MAXIMUM_LENGTH)
}
