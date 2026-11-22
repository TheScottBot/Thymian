package fyi.punkdev.thymian.widget

import android.content.Context
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.ClockFormatPolicy
import fyi.punkdev.thymian.domain.TextClockFormats
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetAppearance
import fyi.punkdev.thymian.domain.sizing.SizeBucket
import fyi.punkdev.thymian.domain.sizing.SizeLayoutPolicy
import fyi.punkdev.thymian.domain.sizing.WeatherBlockSizing
import fyi.punkdev.thymian.domain.sizing.WidgetSize
import fyi.punkdev.thymian.domain.weather.TemperatureFormat
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import java.util.Locale

/**
 * The one renderer: turns one widget state into one RemoteViews tree. Every
 * entry point (first add, update, resize, locale change, the settings preview,
 * and later refresh and boot) goes through here (specification 2.2). With
 * several reported sizes it builds Android's responsive tree, one layout per
 * size, and the launcher shows whichever fits (specification 2.4).
 */
object WidgetRenderer {
    /** Roughly a 4 by 1 widget in portrait, used when the launcher reports no size. */
    val DEFAULT_SIZE = WidgetSize(276f, 102f)

    // RemoteViews accepts at most this many sizes in one responsive tree.
    private const val MAXIMUM_RESPONSIVE_SIZES = 16

    fun render(
        context: Context,
        widgetState: WidgetState,
        locale: Locale,
    ): RemoteViews {
        val sizes =
            widgetState.sizes
                .distinct()
                .take(MAXIMUM_RESPONSIVE_SIZES)
                .ifEmpty { listOf(DEFAULT_SIZE) }
        if (sizes.size == 1) return renderForSize(context, widgetState, locale, sizes.single())
        return RemoteViews(
            sizes.associate { size -> SizeF(size.widthDp, size.heightDp) to renderForSize(context, widgetState, locale, size) },
        )
    }

    fun renderForSize(
        context: Context,
        widgetState: WidgetState,
        locale: Locale,
        size: WidgetSize,
    ): RemoteViews {
        val sizeLayout = SizeLayoutPolicy.layoutFor(size)
        val remoteViews = RemoteViews(context.packageName, layoutFor(sizeLayout.bucket))
        val clockFormats = ClockFormatPolicy.formatsFor(widgetState.clockMode, LocalizedDatePattern.forLocale(locale))
        remoteViews.setClockFormats(R.id.clock_digits, clockFormats.digits)
        remoteViews.setClockFormats(R.id.clock_suffix, clockFormats.suffix)
        remoteViews.setClockFormats(R.id.clock_date, clockFormats.date)
        remoteViews.applyAppearance(WidgetAppearance.from(widgetState.settings))
        remoteViews.setTextViewTextSize(R.id.clock_digits, TypedValue.COMPLEX_UNIT_DIP, sizeLayout.clock.digitsDp.toFloat())
        remoteViews.setTextViewTextSize(R.id.clock_date, TypedValue.COMPLEX_UNIT_DIP, sizeLayout.clock.dateDp.toFloat())
        remoteViews.setTextViewTextSize(R.id.clock_suffix, TypedValue.COMPLEX_UNIT_DIP, sizeLayout.clock.suffixDp.toFloat())
        val dateShown = widgetState.settings[SettingDeclarations.SHOW_DATE] && sizeLayout.clock.dateFits
        remoteViews.setViewVisibility(R.id.clock_date, if (dateShown) View.VISIBLE else View.GONE)
        sizeLayout.weather?.let { weatherSizing -> remoteViews.applyWeather(widgetState, weatherSizing) }
        return remoteViews
    }

    /**
     * The weather block: place name, condition icon tinted to the text colour, and
     * the temperature in this widget's unit (specification 1.3). Hidden until the
     * first refresh, and when every element in it is switched off.
     */
    private fun RemoteViews.applyWeather(
        widgetState: WidgetState,
        weatherSizing: WeatherBlockSizing,
    ) {
        val snapshot: WeatherSnapshot? = widgetState.weather
        val settings = widgetState.settings
        val placeName = snapshot?.placeName
        val placeShown = settings[SettingDeclarations.SHOW_PLACE_NAME] && placeName != null
        val iconShown = settings[SettingDeclarations.SHOW_CONDITION_ICON]
        val temperatureShown = settings[SettingDeclarations.SHOW_TEMPERATURE]
        if (snapshot == null || !(placeShown || iconShown || temperatureShown)) {
            setViewVisibility(R.id.weather_block, View.GONE)
            return
        }
        setViewVisibility(R.id.weather_block, View.VISIBLE)

        setViewVisibility(R.id.weather_place, visibleWhen(placeShown))
        setTextViewText(R.id.weather_place, placeName.orEmpty())
        setTextViewTextSize(R.id.weather_place, TypedValue.COMPLEX_UNIT_DIP, weatherSizing.placeDp.toFloat())

        val weather = snapshot.currentWeather
        setViewVisibility(R.id.weather_icon, visibleWhen(iconShown))
        setImageViewResource(R.id.weather_icon, WeatherIconDrawables.forIconName(weather.condition.iconName(weather.isDay)))
        setInt(R.id.weather_icon, "setColorFilter", WidgetAppearance.from(settings).textArgb)
        setViewLayoutWidth(R.id.weather_icon, weatherSizing.iconDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        setViewLayoutHeight(R.id.weather_icon, weatherSizing.iconDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)

        val unit = settings[SettingDeclarations.TEMPERATURE_UNIT]
        setViewVisibility(R.id.weather_temperature, visibleWhen(temperatureShown))
        setTextViewText(R.id.weather_temperature, TemperatureFormat.format(weather.temperatureCelsius, unit))
        setTextViewTextSize(R.id.weather_temperature, TypedValue.COMPLEX_UNIT_DIP, weatherSizing.temperatureDp.toFloat())
        // The icon is not read on its own, so the temperature carries the condition (specification 1.4).
        setContentDescription(
            R.id.weather_temperature,
            "${weather.condition.description}, ${TemperatureFormat.spoken(weather.temperatureCelsius, unit)}",
        )
    }

    private fun visibleWhen(shown: Boolean): Int = if (shown) View.VISIBLE else View.GONE

    private fun layoutFor(bucket: SizeBucket): Int =
        when (bucket) {
            SizeBucket.SMALL -> R.layout.thymian_widget_small
            SizeBucket.WIDE -> R.layout.thymian_widget_wide
            SizeBucket.TALL -> R.layout.thymian_widget_tall
        }

    /**
     * Background colour, opacity, corner radius and text colour, in one place for
     * the widget and the settings preview alike (specification 2.2), since the
     * preview applies this same RemoteViews tree.
     */
    private fun RemoteViews.applyAppearance(appearance: WidgetAppearance) {
        setInt(R.id.widget_root, "setBackgroundColor", appearance.backgroundArgb)
        setViewOutlinePreferredRadius(R.id.widget_root, appearance.cornerRadiusDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        // A view missing from a layout is skipped by RemoteViews, so one list serves every size.
        for (textViewId in TEXT_VIEW_IDS) {
            setTextColor(textViewId, appearance.textArgb)
        }
    }

    private val TEXT_VIEW_IDS =
        listOf(R.id.clock_date, R.id.clock_digits, R.id.clock_suffix, R.id.weather_place, R.id.weather_temperature)

    private fun RemoteViews.setClockFormats(
        clockViewId: Int,
        formats: TextClockFormats,
    ) {
        // TextClock's format setters are remotable, so the launcher's copy keeps ticking by itself.
        setCharSequence(clockViewId, "setFormat12Hour", formats.whenDeviceUses12Hour)
        setCharSequence(clockViewId, "setFormat24Hour", formats.whenDeviceUses24Hour)
    }
}
