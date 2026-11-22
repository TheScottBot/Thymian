package fyi.punkdev.thymian.composition

import android.content.Context
import fyi.punkdev.thymian.domain.TimeSource
import fyi.punkdev.thymian.domain.location.LocationMode
import fyi.punkdev.thymian.domain.location.LocationPreference
import fyi.punkdev.thymian.domain.location.LocationResolver
import fyi.punkdev.thymian.domain.settings.KeyValueWidgetSettingsStore
import fyi.punkdev.thymian.domain.settings.WidgetSettingsStore
import fyi.punkdev.thymian.domain.weather.KeyValueWeatherSnapshotStore
import fyi.punkdev.thymian.domain.weather.RefreshWeather
import fyi.punkdev.thymian.domain.weather.WeatherSnapshotStore
import fyi.punkdev.thymian.location.GeocoderPlaceNameSource
import fyi.punkdev.thymian.location.PlatformDeviceLocationSource
import fyi.punkdev.thymian.settings.SharedPreferencesKeyValueStore
import fyi.punkdev.thymian.weather.HttpWeatherSource
import java.time.Instant

/**
 * The one place adapters are chosen (specification 2.2). Everything else
 * depends on the ports it hands out.
 */
object CompositionRoot {
    private const val WEATHER_PREFERENCES_NAME = "weather_snapshot"

    private val systemClock = TimeSource { Instant.now() }

    fun widgetSettingsStore(context: Context): WidgetSettingsStore =
        KeyValueWidgetSettingsStore(SharedPreferencesKeyValueStore(context.applicationContext))

    fun weatherSnapshotStore(context: Context): WeatherSnapshotStore =
        KeyValueWeatherSnapshotStore(SharedPreferencesKeyValueStore(context.applicationContext, WEATHER_PREFERENCES_NAME))

    /** Until the location settings arrive (CWE5 batch D2), every refresh follows the phone. */
    fun locationPreference(context: Context): LocationPreference = LocationPreference(LocationMode.AUTOMATIC)

    fun refreshWeather(context: Context): RefreshWeather {
        val applicationContext = context.applicationContext
        return RefreshWeather(
            locationResolver = LocationResolver(PlatformDeviceLocationSource(applicationContext), systemClock),
            weatherSource = HttpWeatherSource(),
            placeNameSource = GeocoderPlaceNameSource(applicationContext, applicationContext.resources.configuration.locales[0]),
            snapshotStore = weatherSnapshotStore(applicationContext),
            timeSource = systemClock,
        )
    }
}
