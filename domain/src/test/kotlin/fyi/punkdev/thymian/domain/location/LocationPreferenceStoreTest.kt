package fyi.punkdev.thymian.domain.location

import fyi.punkdev.thymian.domain.settings.InMemoryKeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** One location preference for the whole app (CWD36), read back defensively like the other stores. */
class LocationPreferenceStoreTest {
    private val keyValueStore = InMemoryKeyValueStore()
    private val preferenceStore = KeyValueLocationPreferenceStore(keyValueStore)
    private val munich = FixedPlace("München", Coordinates(48.1374, 11.5755))

    @Test
    fun `nothing stored means follow the phone`() {
        assertEquals(LocationPreference(LocationMode.AUTOMATIC), preferenceStore.load())
    }

    @Test
    fun `a fixed place round trips`() {
        preferenceStore.save(LocationPreference(LocationMode.FIXED, munich))

        assertEquals(LocationPreference(LocationMode.FIXED, munich), preferenceStore.load())
    }

    @Test
    fun `automatic mode keeps a chosen place as the fallback`() {
        preferenceStore.save(LocationPreference(LocationMode.AUTOMATIC, munich))

        assertEquals(LocationPreference(LocationMode.AUTOMATIC, munich), preferenceStore.load())
    }

    @Test
    fun `switching back to automatic without a place clears it`() {
        preferenceStore.save(LocationPreference(LocationMode.FIXED, munich))
        preferenceStore.save(LocationPreference(LocationMode.AUTOMATIC))

        assertEquals(LocationPreference(LocationMode.AUTOMATIC), preferenceStore.load())
    }

    @Test
    fun `a damaged place is dropped but the mode survives`() {
        preferenceStore.save(LocationPreference(LocationMode.FIXED, munich))
        keyValueStore.writeAll(mapOf("location.fixed_latitude" to "123.0"))

        assertEquals(LocationPreference(LocationMode.FIXED, fixedPlace = null), preferenceStore.load())
    }

    @Test
    fun `a foreign schema version is not trusted`() {
        preferenceStore.save(LocationPreference(LocationMode.FIXED, munich))
        keyValueStore.writeAll(mapOf("location.schema_version" to "7"))

        assertEquals(LocationPreference(LocationMode.AUTOMATIC), preferenceStore.load())
    }
}
