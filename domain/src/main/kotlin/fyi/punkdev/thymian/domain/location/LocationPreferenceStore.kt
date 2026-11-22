package fyi.punkdev.thymian.domain.location

import fyi.punkdev.thymian.domain.settings.KeyValueStore

/** Port: the one location preference for the whole app (CWD36). */
interface LocationPreferenceStore {
    fun load(): LocationPreference

    fun save(preference: LocationPreference)
}

/**
 * Keeps the preference under `location.*` with a schema version, read back
 * defensively (datum 0.10): an unreadable mode means automatic, and a place with
 * any missing, oversized or out of range part is dropped whole.
 */
class KeyValueLocationPreferenceStore(
    private val keyValueStore: KeyValueStore,
) : LocationPreferenceStore {
    override fun load(): LocationPreference {
        val stored =
            keyValueStore
                .readAll()
                .filterKeys { storedKey ->
                    storedKey.startsWith(PREFIX)
                }.mapKeys { (storedKey, _) -> storedKey.removePrefix(PREFIX) }
        if (stored[SCHEMA_VERSION_KEY] != SCHEMA_VERSION) return LocationPreference(LocationMode.AUTOMATIC)
        val mode = LocationMode.entries.firstOrNull { entry -> entry.name == stored[MODE_KEY] } ?: LocationMode.AUTOMATIC
        return LocationPreference(mode, storedPlace(stored))
    }

    override fun save(preference: LocationPreference) {
        val place = preference.fixedPlace
        if (place == null) keyValueStore.removeAll(PLACE_KEYS.mapTo(mutableSetOf()) { placeKey -> PREFIX + placeKey })
        val entries =
            mapOf(SCHEMA_VERSION_KEY to SCHEMA_VERSION, MODE_KEY to preference.mode.name) +
                if (place == null) {
                    emptyMap()
                } else {
                    mapOf(
                        NAME_KEY to place.name,
                        LATITUDE_KEY to place.coordinates.latitudeDegrees.toString(),
                        LONGITUDE_KEY to place.coordinates.longitudeDegrees.toString(),
                    )
                }
        keyValueStore.writeAll(entries.mapKeys { (entryKey, _) -> PREFIX + entryKey })
    }

    private fun storedPlace(stored: Map<String, String>): FixedPlace? {
        val name =
            stored[NAME_KEY]?.takeIf { storedName -> storedName.isNotBlank() && storedName.length <= PlaceNameChoice.MAXIMUM_LENGTH }
                ?: return null
        val latitude =
            stored[LATITUDE_KEY]?.takeIf { storedText -> storedText.length <= MAXIMUM_NUMBER_LENGTH }?.toDoubleOrNull() ?: return null
        val longitude =
            stored[LONGITUDE_KEY]?.takeIf { storedText -> storedText.length <= MAXIMUM_NUMBER_LENGTH }?.toDoubleOrNull() ?: return null
        return Coordinates.orNull(latitude, longitude)?.let { coordinates -> FixedPlace(name, coordinates) }
    }

    private companion object {
        const val PREFIX = "location."
        const val SCHEMA_VERSION_KEY = "schema_version"
        const val SCHEMA_VERSION = "1"
        const val MODE_KEY = "mode"
        const val NAME_KEY = "fixed_name"
        const val LATITUDE_KEY = "fixed_latitude"
        const val LONGITUDE_KEY = "fixed_longitude"
        const val MAXIMUM_NUMBER_LENGTH = 32
        val PLACE_KEYS = setOf(NAME_KEY, LATITUDE_KEY, LONGITUDE_KEY)
    }
}
