package fyi.punkdev.thymian.domain.location

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

/**
 * The fallback chain (specification 1.3, CWD25): a chosen fixed place wins;
 * otherwise a recent last known fix, then a fresh fix, then any last known fix,
 * then the fixed place if one is set. Approximate fixes did not arrive fresh in
 * the Plan spike, so a recent last known fix is used without asking for one.
 */
class LocationResolverTest {
    private val now = Instant.parse("2026-11-22T12:00:00Z")
    private val london = Coordinates(51.5074, -0.1278)
    private val berlin = Coordinates(52.52, 13.405)
    private val munich = Coordinates(48.1374, 11.5755)
    private val fixedBerlin = FixedPlace(name = "Berlin", coordinates = berlin)

    private fun fixAged(
        coordinates: Coordinates,
        minutes: Long,
    ) = LocatedFix(coordinates, capturedAt = now.minus(Duration.ofMinutes(minutes)))

    private fun resolve(
        device: FakeDeviceLocationSource,
        preference: LocationPreference,
    ): LocationResolution = LocationResolver(device) { now }.resolve(preference)

    @Test
    fun `a chosen fixed place wins without touching the device`() {
        val device = FakeDeviceLocationSource(lastKnown = fixAged(london, 5))

        val resolution = resolve(device, LocationPreference(LocationMode.FIXED, fixedBerlin))

        assertEquals(LocationResolution.Resolved(berlin, LocationSourceUsed.FIXED_CHOSEN), resolution)
        assertEquals(0, device.currentFixRequests)
    }

    @Test
    fun `a recent last known fix is used without asking for a fresh one`() {
        val device = FakeDeviceLocationSource(lastKnown = fixAged(london, 59), current = fixAged(munich, 0))

        val resolution = resolve(device, LocationPreference(LocationMode.AUTOMATIC))

        assertEquals(LocationResolution.Resolved(london, LocationSourceUsed.DEVICE_RECENT), resolution)
        assertEquals(0, device.currentFixRequests)
    }

    @Test
    fun `an old last known fix triggers a request for a fresh one`() {
        val device = FakeDeviceLocationSource(lastKnown = fixAged(london, 61), current = fixAged(munich, 0))

        val resolution = resolve(device, LocationPreference(LocationMode.AUTOMATIC))

        assertEquals(LocationResolution.Resolved(munich, LocationSourceUsed.DEVICE_CURRENT), resolution)
        assertEquals(1, device.currentFixRequests)
    }

    @Test
    fun `with no fresh fix an old last known fix is still better than nothing`() {
        val device = FakeDeviceLocationSource(lastKnown = fixAged(london, 600), current = null)

        val resolution = resolve(device, LocationPreference(LocationMode.AUTOMATIC, fixedBerlin))

        assertEquals(LocationResolution.Resolved(london, LocationSourceUsed.DEVICE_LAST_KNOWN), resolution)
    }

    @Test
    fun `with no device fix at all the fixed place is the fallback`() {
        val device = FakeDeviceLocationSource(lastKnown = null, current = null)

        val resolution = resolve(device, LocationPreference(LocationMode.AUTOMATIC, fixedBerlin))

        assertEquals(LocationResolution.Resolved(berlin, LocationSourceUsed.FIXED_FALLBACK), resolution)
    }

    @Test
    fun `background access refused falls back to the fixed place`() {
        val device = FakeDeviceLocationSource(permission = LocationPermission.DENIED, lastKnown = fixAged(london, 5))

        val resolution = resolve(device, LocationPreference(LocationMode.AUTOMATIC, fixedBerlin))

        assertEquals(LocationResolution.Resolved(berlin, LocationSourceUsed.FIXED_FALLBACK), resolution)
        assertEquals(0, device.currentFixRequests)
    }

    @Test
    fun `nothing available says why`() {
        val refused = FakeDeviceLocationSource(permission = LocationPermission.DENIED)
        val noFix = FakeDeviceLocationSource(lastKnown = null, current = null)

        assertEquals(
            LocationResolution.Unavailable(LocationUnavailableReason.PERMISSION_DENIED),
            resolve(refused, LocationPreference(LocationMode.AUTOMATIC)),
        )
        assertEquals(
            LocationResolution.Unavailable(LocationUnavailableReason.NO_FIX),
            resolve(noFix, LocationPreference(LocationMode.AUTOMATIC)),
        )
    }

    @Test
    fun `fixed mode with no place chosen behaves as automatic`() {
        val device = FakeDeviceLocationSource(lastKnown = fixAged(london, 5))

        val resolution = resolve(device, LocationPreference(LocationMode.FIXED, fixedPlace = null))

        assertEquals(LocationResolution.Resolved(london, LocationSourceUsed.DEVICE_RECENT), resolution)
    }

    @Test
    fun `coordinates outside the globe are refused`() {
        assertEquals(null, Coordinates.orNull(91.0, 0.0))
        assertEquals(null, Coordinates.orNull(0.0, -180.1))
        assertEquals(Coordinates(-90.0, 180.0), Coordinates.orNull(-90.0, 180.0))
    }
}

/** Test double for the device location port; counts fresh fix requests. Shared by location tests. */
class FakeDeviceLocationSource(
    private val permission: LocationPermission = LocationPermission.GRANTED,
    private val lastKnown: LocatedFix? = null,
    private val current: LocatedFix? = null,
) : DeviceLocationSource {
    var currentFixRequests = 0
        private set

    override fun permission(): LocationPermission = permission

    override fun lastKnownFix(): LocatedFix? = lastKnown

    override fun requestCurrentFix(): LocatedFix? {
        currentFixRequests += 1
        return current
    }
}
