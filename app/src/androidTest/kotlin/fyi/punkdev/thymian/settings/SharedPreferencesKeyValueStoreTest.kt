package fyi.punkdev.thymian.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fyi.punkdev.thymian.domain.settings.KeyValueWidgetSettingsStore
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetSettings
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** The SharedPreferences adapter behind the domain's per widget store, on a real device. */
@RunWith(AndroidJUnit4::class)
class SharedPreferencesKeyValueStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    // A file of its own, so the test never touches a placed widget's settings.
    private val preferencesName = "widget_settings_under_test"
    private val settingsStore = KeyValueWidgetSettingsStore(SharedPreferencesKeyValueStore(context, preferencesName))

    private val customised =
        WidgetSettings
            .defaults()
            .with(SettingDeclarations.BACKGROUND_OPACITY_PERCENT, 75)
            .with(SettingDeclarations.TEXT_COLOUR, 0x102030)

    @After
    fun removeTestPreferences() {
        context.deleteSharedPreferences(preferencesName)
    }

    @Test
    fun `settings survive a fresh adapter over the same preferences file`() {
        settingsStore.save(appWidgetId = 3, settings = customised)

        val reopened = KeyValueWidgetSettingsStore(SharedPreferencesKeyValueStore(context, preferencesName))

        assertEquals(customised, reopened.load(appWidgetId = 3))
    }

    @Test
    fun `deleting one widget keeps the settings of another widget`() {
        settingsStore.save(appWidgetId = 3, settings = customised)
        settingsStore.save(appWidgetId = 4, settings = customised)

        settingsStore.delete(appWidgetId = 3)

        assertEquals(WidgetSettings.defaults(), settingsStore.load(appWidgetId = 3))
        assertEquals(customised, settingsStore.load(appWidgetId = 4))
    }
}
