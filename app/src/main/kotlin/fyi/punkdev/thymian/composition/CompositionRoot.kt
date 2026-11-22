package fyi.punkdev.thymian.composition

import android.content.Context
import fyi.punkdev.thymian.domain.settings.KeyValueWidgetSettingsStore
import fyi.punkdev.thymian.domain.settings.WidgetSettingsStore
import fyi.punkdev.thymian.settings.SharedPreferencesKeyValueStore

/**
 * The one place adapters are chosen (specification 2.2). Everything else
 * depends on the ports it hands out.
 */
object CompositionRoot {
    fun widgetSettingsStore(context: Context): WidgetSettingsStore =
        KeyValueWidgetSettingsStore(SharedPreferencesKeyValueStore(context.applicationContext))
}
