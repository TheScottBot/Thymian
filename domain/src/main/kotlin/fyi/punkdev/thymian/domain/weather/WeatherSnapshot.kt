package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.location.PlaceNameChoice
import fyi.punkdev.thymian.domain.settings.KeyValueStore
import java.time.Instant

/** The last good weather, kept so a failed refresh never blanks the widget (specification 1.3). */
data class WeatherSnapshot(
    val currentWeather: CurrentWeather,
    val placeName: String?,
    val locationSource: LocationSourceUsed,
    val fetchedAt: Instant,
)

/** Why a fetch failed, by category only (datum 0.15). */
enum class WeatherFetchFailure {
    NETWORK,
    HTTP_STATUS,
    PROVIDER_ERROR,
    REJECTED_RESPONSE,
}

sealed interface WeatherFetchResult {
    data class Fetched(
        val currentWeather: CurrentWeather,
    ) : WeatherFetchResult

    data class Failed(
        val failure: WeatherFetchFailure,
    ) : WeatherFetchResult
}

/** Port: current weather at some coordinates (specification 2.1). Blocking; called from background work. */
interface WeatherSource {
    fun fetch(coordinates: Coordinates): WeatherFetchResult
}

/** Port: the one last good snapshot for the whole app (CWD36). */
interface WeatherSnapshotStore {
    fun load(): WeatherSnapshot?

    fun save(snapshot: WeatherSnapshot)
}

/**
 * Keeps the snapshot under `weather.*` with a schema version. Read back
 * defensively (datum 0.10): any missing, oversized or unreadable value means the
 * whole snapshot is not trusted, since half a snapshot could pair one place's
 * name with another's weather.
 */
class KeyValueWeatherSnapshotStore(
    private val keyValueStore: KeyValueStore,
) : WeatherSnapshotStore {
    override fun load(): WeatherSnapshot? {
        val stored =
            keyValueStore
                .readAll()
                .filterKeys { storedKey ->
                    storedKey.startsWith(PREFIX)
                }.mapKeys { (storedKey, _) -> storedKey.removePrefix(PREFIX) }
        if (stored[SCHEMA_VERSION_KEY] != SCHEMA_VERSION) return null
        if (stored.values.any { storedValue -> storedValue.length > MAXIMUM_STORED_LENGTH }) return null
        val temperature = stored[TEMPERATURE_KEY]?.toDoubleOrNull()?.takeIf { celsius -> celsius.isFinite() } ?: return null
        val condition = WeatherCondition.entries.firstOrNull { entry -> entry.name == stored[CONDITION_KEY] } ?: return null
        val isDay = stored[IS_DAY_KEY]?.toBooleanStrictOrNull() ?: return null
        val source = LocationSourceUsed.entries.firstOrNull { entry -> entry.name == stored[SOURCE_KEY] } ?: return null
        val fetchedAt = stored[FETCHED_AT_KEY]?.toLongOrNull()?.let { epochMillis -> Instant.ofEpochMilli(epochMillis) } ?: return null
        val placeName = stored[PLACE_NAME_KEY]
        if (placeName != null && placeName.length > PlaceNameChoice.MAXIMUM_LENGTH) return null
        return WeatherSnapshot(CurrentWeather(temperature, condition, isDay), placeName, source, fetchedAt)
    }

    override fun save(snapshot: WeatherSnapshot) {
        val entries =
            mapOf(
                SCHEMA_VERSION_KEY to SCHEMA_VERSION,
                TEMPERATURE_KEY to snapshot.currentWeather.temperatureCelsius.toString(),
                CONDITION_KEY to snapshot.currentWeather.condition.name,
                IS_DAY_KEY to snapshot.currentWeather.isDay.toString(),
                SOURCE_KEY to snapshot.locationSource.name,
                FETCHED_AT_KEY to snapshot.fetchedAt.toEpochMilli().toString(),
            ) + listOfNotNull(snapshot.placeName?.let { placeName -> PLACE_NAME_KEY to placeName })
        if (snapshot.placeName == null) keyValueStore.removeAll(setOf(PREFIX + PLACE_NAME_KEY))
        keyValueStore.writeAll(entries.mapKeys { (entryKey, _) -> PREFIX + entryKey })
    }

    private companion object {
        const val PREFIX = "weather."
        const val SCHEMA_VERSION_KEY = "schema_version"
        const val SCHEMA_VERSION = "1"
        const val TEMPERATURE_KEY = "temperature_celsius"
        const val CONDITION_KEY = "condition"
        const val IS_DAY_KEY = "is_day"
        const val PLACE_NAME_KEY = "place_name"
        const val SOURCE_KEY = "location_source"
        const val FETCHED_AT_KEY = "fetched_at_epoch_millis"
        const val MAXIMUM_STORED_LENGTH = 64
    }
}
