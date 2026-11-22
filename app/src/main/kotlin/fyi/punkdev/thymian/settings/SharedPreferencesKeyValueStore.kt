package fyi.punkdev.thymian.settings

import android.content.Context
import fyi.punkdev.thymian.domain.settings.KeyValueStore

/** Adapter: the domain's key value port over one private SharedPreferences file. */
class SharedPreferencesKeyValueStore(
    context: Context,
    preferencesName: String = DEFAULT_PREFERENCES_NAME,
) : KeyValueStore {
    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    // Only strings are ever written, so anything else in the file is not ours and is skipped.
    override fun readAll(): Map<String, String> =
        preferences.all
            .mapNotNull { (storedKey, storedValue) ->
                (storedValue as? String)?.let { storedText -> storedKey to storedText }
            }.toMap()

    override fun writeAll(entriesToWrite: Map<String, String>) {
        val editor = preferences.edit()
        entriesToWrite.forEach { (entryKey, entryValue) -> editor.putString(entryKey, entryValue) }
        // commit, not apply: a widget update straight after must read what was just saved.
        check(editor.commit()) { "settings were not written" }
    }

    override fun removeAll(keysToRemove: Set<String>) {
        val editor = preferences.edit()
        keysToRemove.forEach { keyToRemove -> editor.remove(keyToRemove) }
        check(editor.commit()) { "settings were not removed" }
    }

    companion object {
        const val DEFAULT_PREFERENCES_NAME = "widget_settings"
    }
}
