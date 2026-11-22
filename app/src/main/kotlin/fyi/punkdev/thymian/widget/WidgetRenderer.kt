package fyi.punkdev.thymian.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.ClockFormatPolicy
import fyi.punkdev.thymian.domain.TextClockFormats
import fyi.punkdev.thymian.domain.WidgetState
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.WidgetAppearance
import java.util.Locale

/**
 * The one renderer: turns one widget state into one RemoteViews tree. Every
 * entry point (first add, update, locale change, and later resize, refresh,
 * reconfigure and boot) goes through here (specification 2.2).
 */
object WidgetRenderer {
    fun render(
        context: Context,
        widgetState: WidgetState,
        locale: Locale,
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.thymian_widget)
        val clockFormats = ClockFormatPolicy.formatsFor(widgetState.clockMode, LocalizedDatePattern.forLocale(locale))
        remoteViews.setClockFormats(R.id.clock_digits, clockFormats.digits)
        remoteViews.setClockFormats(R.id.clock_suffix, clockFormats.suffix)
        remoteViews.setClockFormats(R.id.clock_date, clockFormats.date)
        remoteViews.applyAppearance(WidgetAppearance.from(widgetState.settings))
        remoteViews.setViewVisibility(
            R.id.clock_date,
            if (widgetState.settings[SettingDeclarations.SHOW_DATE]) View.VISIBLE else View.GONE,
        )
        return remoteViews
    }

    /**
     * Background colour, opacity, corner radius and text colour, in one place for
     * the widget and the settings preview alike (specification 2.2), since the
     * preview applies this same RemoteViews tree.
     */
    private fun RemoteViews.applyAppearance(appearance: WidgetAppearance) {
        setInt(R.id.widget_root, "setBackgroundColor", appearance.backgroundArgb)
        setViewOutlinePreferredRadius(R.id.widget_root, appearance.cornerRadiusDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        for (textViewId in TEXT_VIEW_IDS) {
            setTextColor(textViewId, appearance.textArgb)
        }
    }

    private val TEXT_VIEW_IDS = listOf(R.id.clock_date, R.id.clock_digits, R.id.clock_suffix)

    private fun RemoteViews.setClockFormats(
        clockViewId: Int,
        formats: TextClockFormats,
    ) {
        // TextClock's format setters are remotable, so the launcher's copy keeps ticking by itself.
        setCharSequence(clockViewId, "setFormat12Hour", formats.whenDeviceUses12Hour)
        setCharSequence(clockViewId, "setFormat24Hour", formats.whenDeviceUses24Hour)
    }
}
