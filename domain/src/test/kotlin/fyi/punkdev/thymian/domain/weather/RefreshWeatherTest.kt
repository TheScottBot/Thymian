package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.location.FakeDeviceLocationSource
import fyi.punkdev.thymian.domain.location.FixedPlace
import fyi.punkdev.thymian.domain.location.LocatedFix
import fyi.punkdev.thymian.domain.location.LocationMode
import fyi.punkdev.thymian.domain.location.LocationPermission
import fyi.punkdev.thymian.domain.location.LocationPreference
import fyi.punkdev.thymian.domain.location.LocationResolver
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.location.LocationUnavailableReason
import fyi.punkdev.thymian.domain.location.PlaceNameSource
import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * One refresh: resolve the location, fetch the weather, name the place, keep the
 * result. Any failure keeps the last good snapshot and says why (specification 1.3, 1.4).
 */
class RefreshWeatherTest {
    private val now = Instant.parse("2026-11-22T12:00:00Z")
    private val berlin = Coordinates(52.52, 13.405)
    private val munich = Coordinates(48.1374, 11.5755)
    private val partlyCloudy = CurrentWeather(4.5, WeatherCondition.PARTLY_CLOUDY, isDay = true)
    private val snapshotStore = KeyValueWeatherSnapshotStore(InMemoryKeyValueStore())

    private val previousSnapshot =
        WeatherSnapshot(
            currentWeather = CurrentWeather(1.0, WeatherCondition.FOG, isDay = false),
            placeName = "Earlier",
            locationSource = LocationSourceUsed.DEVICE_RECENT,
            fetchedAt = now.minusSeconds(3_600),
        )

    private fun refresh(
        device: FakeDeviceLocationSource = FakeDeviceLocationSource(lastKnown = LocatedFix(berlin, now)),
        weatherSource: WeatherSource = FakeWeatherSource(WeatherFetchResult.Fetched(partlyCloudy)),
        placeNameSource: PlaceNameSource = FakePlaceNameSource("Berlin"),
        preference: LocationPreference = LocationPreference(LocationMode.AUTOMATIC),
    ): RefreshOutcome =
        RefreshWeather(LocationResolver(device) { now }, weatherSource, placeNameSource, snapshotStore) { now }.refresh(preference)

    @Test
    fun `a successful refresh stores and reports the new snapshot`() {
        val outcome = refresh()

        val expected = WeatherSnapshot(partlyCloudy, "Berlin", LocationSourceUsed.DEVICE_RECENT, now)
        assertEquals(RefreshOutcome.Refreshed(expected), outcome)
        assertEquals(expected, snapshotStore.load())
    }

    @Test
    fun `a fixed place uses its own name without asking the geocoder`() {
        val placeNames = FakePlaceNameSource("Should not be used")

        val outcome =
            refresh(placeNameSource = placeNames, preference = LocationPreference(LocationMode.FIXED, FixedPlace("München", munich)))

        assertEquals("München", (outcome as RefreshOutcome.Refreshed).snapshot.placeName)
        assertEquals(0, placeNames.lookups)
    }

    @Test
    fun `the weather is still shown when the place cannot be named`() {
        val outcome = refresh(placeNameSource = FakePlaceNameSource(null))

        assertEquals(null, (outcome as RefreshOutcome.Refreshed).snapshot.placeName)
    }

    @Test
    fun `no location keeps the last good snapshot and says why`() {
        snapshotStore.save(previousSnapshot)

        val outcome = refresh(device = FakeDeviceLocationSource(permission = LocationPermission.DENIED))

        assertEquals(RefreshOutcome.LocationUnavailable(LocationUnavailableReason.PERMISSION_DENIED), outcome)
        assertEquals(previousSnapshot, snapshotStore.load())
    }

    @Test
    fun `a failed fetch keeps the last good snapshot and says why`() {
        snapshotStore.save(previousSnapshot)

        val outcome = refresh(weatherSource = FakeWeatherSource(WeatherFetchResult.Failed(WeatherFetchFailure.NETWORK)))

        assertEquals(RefreshOutcome.WeatherUnavailable(WeatherFetchFailure.NETWORK, LocationSourceUsed.DEVICE_RECENT), outcome)
        assertEquals(previousSnapshot, snapshotStore.load())
    }
}

/** Test double for the weather port; records the coordinates it was asked about. Shared by refresh tests. */
class FakeWeatherSource(
    private val result: WeatherFetchResult,
) : WeatherSource {
    val requestedCoordinates = mutableListOf<Coordinates>()

    override fun fetch(coordinates: Coordinates): WeatherFetchResult {
        requestedCoordinates += coordinates
        return result
    }
}

/** Test double for the place name port; counts lookups. */
class FakePlaceNameSource(
    private val placeName: String?,
) : PlaceNameSource {
    var lookups = 0
        private set

    override fun placeNameFor(coordinates: Coordinates): String? {
        lookups += 1
        return placeName
    }
}
