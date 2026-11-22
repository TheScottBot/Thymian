package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** The last good weather survives restarts so a failed refresh never blanks the widget (specification 1.3). */
class WeatherSnapshotStoreTest {
    private val keyValueStore = InMemoryKeyValueStore()
    private val snapshotStore = KeyValueWeatherSnapshotStore(keyValueStore)
    private val snapshot =
        WeatherSnapshot(
            currentWeather = CurrentWeather(temperatureCelsius = 4.5, condition = WeatherCondition.PARTLY_CLOUDY, isDay = true),
            placeName = "Berlin",
            locationSource = LocationSourceUsed.DEVICE_RECENT,
            fetchedAt = Instant.parse("2026-11-22T12:00:00Z"),
        )

    @Test
    fun `nothing stored loads nothing`() {
        assertEquals(null, snapshotStore.load())
    }

    @Test
    fun `a snapshot round trips`() {
        snapshotStore.save(snapshot)

        assertEquals(snapshot, snapshotStore.load())
    }

    @Test
    fun `a snapshot without a place name round trips`() {
        snapshotStore.save(snapshot.copy(placeName = null))

        assertEquals(snapshot.copy(placeName = null), snapshotStore.load())
    }

    @Test
    fun `a damaged or foreign stored snapshot is not trusted`() {
        snapshotStore.save(snapshot)
        keyValueStore.writeAll(mapOf("weather.temperature_celsius" to "hot"))
        assertEquals(null, snapshotStore.load())

        snapshotStore.save(snapshot)
        keyValueStore.writeAll(mapOf("weather.schema_version" to "999"))
        assertEquals(null, snapshotStore.load())

        snapshotStore.save(snapshot)
        keyValueStore.writeAll(mapOf("weather.place_name" to "x".repeat(500)))
        assertEquals(null, snapshotStore.load())
    }
}
