package fyi.punkdev.thymian.domain.settings

import fyi.punkdev.thymian.domain.ClockMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Persistence per widget instance over a key value port, so the per instance
 * rules run on the JVM; the Android adapter only implements the port.
 */
class WidgetSettingsStoreTest {
    private val keyValueStore = InMemoryKeyValueStore()
    private val settingsStore = KeyValueWidgetSettingsStore(keyValueStore)

    private val customised =
        WidgetSettings
            .defaults()
            .with(SettingDeclarations.BACKGROUND_OPACITY_PERCENT, 60)
            .with(SettingDeclarations.BACKGROUND_COLOUR, 0x203040)
            .with(SettingDeclarations.CLOCK_MODE, ClockMode.TWENTY_FOUR_HOUR)
            .with(SettingDeclarations.SHOW_DATE, false)

    @Test
    fun `settings round trip for one widget instance`() {
        settingsStore.save(appWidgetId = 7, settings = customised)

        assertEquals(customised, settingsStore.load(appWidgetId = 7))
    }

    @Test
    fun `two widget instances keep separate settings`() {
        settingsStore.save(appWidgetId = 7, settings = customised)
        settingsStore.save(appWidgetId = 8, settings = WidgetSettings.defaults())

        assertEquals(customised, settingsStore.load(appWidgetId = 7))
        assertEquals(WidgetSettings.defaults(), settingsStore.load(appWidgetId = 8))
    }

    @Test
    fun `an instance with nothing saved loads the defaults`() {
        assertEquals(WidgetSettings.defaults(), settingsStore.load(appWidgetId = 42))
    }

    @Test
    fun `deleting a widget removes its settings and only its settings`() {
        settingsStore.save(appWidgetId = 7, settings = customised)
        settingsStore.save(appWidgetId = 8, settings = customised)

        settingsStore.delete(appWidgetId = 7)

        assertEquals(WidgetSettings.defaults(), settingsStore.load(appWidgetId = 7))
        assertEquals(customised, settingsStore.load(appWidgetId = 8))
        assertEquals(true, keyValueStore.readAll().keys.none { storedKey -> storedKey.startsWith("widget.7.") })
    }

    @Test
    fun `stored values out of range are clamped when loaded`() {
        settingsStore.save(appWidgetId = 7, settings = WidgetSettings.defaults())
        keyValueStore.writeAll(mapOf("widget.7.background_opacity_percent" to "250"))

        assertEquals(100, settingsStore.load(appWidgetId = 7)[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
    }

    @Test
    fun `unreadable or oversized stored values fall back to the default`() {
        settingsStore.save(appWidgetId = 7, settings = customised)
        keyValueStore.writeAll(
            mapOf(
                "widget.7.clock_mode" to "SOMETIMES",
                "widget.7.background_colour" to "9".repeat(200),
            ),
        )

        val loaded = settingsStore.load(appWidgetId = 7)

        assertEquals(ClockMode.FOLLOW_DEVICE, loaded[SettingDeclarations.CLOCK_MODE])
        assertEquals(0x000000, loaded[SettingDeclarations.BACKGROUND_COLOUR])
        assertEquals(60, loaded[SettingDeclarations.BACKGROUND_OPACITY_PERCENT])
    }

    @Test
    fun `settings stored under a different schema version are not trusted`() {
        settingsStore.save(appWidgetId = 7, settings = customised)
        keyValueStore.writeAll(mapOf("widget.7.schema_version" to "999"))

        assertEquals(WidgetSettings.defaults(), settingsStore.load(appWidgetId = 7))
    }
}

/** Test double for the key value port: a plain map. Shared by every store test. */
class InMemoryKeyValueStore : KeyValueStore {
    private val entries = mutableMapOf<String, String>()

    override fun readAll(): Map<String, String> = entries.toMap()

    override fun writeAll(entriesToWrite: Map<String, String>) {
        entries.putAll(entriesToWrite)
    }

    override fun removeAll(keysToRemove: Set<String>) {
        keysToRemove.forEach { keyToRemove -> entries.remove(keyToRemove) }
    }
}
