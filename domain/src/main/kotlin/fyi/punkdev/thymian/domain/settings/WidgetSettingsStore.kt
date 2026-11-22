package fyi.punkdev.thymian.domain.settings

/** Port: settings per widget instance (specification 2.1). */
interface WidgetSettingsStore {
    fun load(appWidgetId: Int): WidgetSettings

    fun save(
        appWidgetId: Int,
        settings: WidgetSettings,
    )

    fun delete(appWidgetId: Int)
}

/** Port: a flat string store, such as Android's SharedPreferences. */
interface KeyValueStore {
    fun readAll(): Map<String, String>

    fun writeAll(entriesToWrite: Map<String, String>)

    fun removeAll(keysToRemove: Set<String>)
}

/**
 * Keeps each instance's settings under `widget.<id>.<setting key>` with a
 * schema version. Stored text is read back defensively (datum 0.10): a value
 * longer than [MAXIMUM_STORED_LENGTH] or unreadable falls back to its default,
 * a number out of bounds is clamped, and a different schema version is not
 * trusted at all.
 */
class KeyValueWidgetSettingsStore(
    private val keyValueStore: KeyValueStore,
) : WidgetSettingsStore {
    override fun load(appWidgetId: Int): WidgetSettings {
        val prefix = prefixFor(appWidgetId)
        val stored =
            keyValueStore
                .readAll()
                .filterKeys { storedKey -> storedKey.startsWith(prefix) }
                .mapKeys { (storedKey, _) -> storedKey.removePrefix(prefix) }
        if (stored[SCHEMA_VERSION_KEY] != SCHEMA_VERSION) return WidgetSettings.defaults()
        return SettingDeclarations.all.fold(WidgetSettings.defaults()) { settings, declaration ->
            settings.withStored(declaration, stored[declaration.key])
        }
    }

    override fun save(
        appWidgetId: Int,
        settings: WidgetSettings,
    ) {
        val prefix = prefixFor(appWidgetId)
        val encoded =
            SettingDeclarations.all.associate { declaration ->
                (prefix + declaration.key) to encodeValue(declaration, settings)
            }
        keyValueStore.writeAll(encoded + (prefix + SCHEMA_VERSION_KEY to SCHEMA_VERSION))
    }

    override fun delete(appWidgetId: Int) {
        val prefix = prefixFor(appWidgetId)
        keyValueStore.removeAll(keyValueStore.readAll().keys.filterTo(mutableSetOf()) { storedKey -> storedKey.startsWith(prefix) })
    }

    private fun <T : Any> WidgetSettings.withStored(
        declaration: SettingDeclaration<T>,
        storedText: String?,
    ): WidgetSettings {
        if (storedText == null || storedText.length > MAXIMUM_STORED_LENGTH) return this
        val decoded = declaration.decode(storedText) ?: return this
        return with(declaration, decoded)
    }

    private fun <T : Any> encodeValue(
        declaration: SettingDeclaration<T>,
        settings: WidgetSettings,
    ): String = declaration.encode(settings[declaration])

    private fun prefixFor(appWidgetId: Int): String = "widget.$appWidgetId."

    private companion object {
        const val SCHEMA_VERSION_KEY = "schema_version"
        const val SCHEMA_VERSION = "1"

        // Longer than any value a declaration encodes, so anything bigger is not ours.
        const val MAXIMUM_STORED_LENGTH = 64
    }
}
