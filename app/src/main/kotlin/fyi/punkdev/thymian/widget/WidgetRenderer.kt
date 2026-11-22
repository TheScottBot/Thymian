package fyi.punkdev.thymian.widget

import android.content.Context
import android.widget.RemoteViews
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.ClockFormatPolicy
import fyi.punkdev.thymian.domain.TextClockFormats
import fyi.punkdev.thymian.domain.WidgetState
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
        return remoteViews
    }

    private fun RemoteViews.setClockFormats(
        clockViewId: Int,
        formats: TextClockFormats,
    ) {
        // TextClock's format setters are remotable, so the launcher's copy keeps ticking by itself.
        setCharSequence(clockViewId, "setFormat12Hour", formats.whenDeviceUses12Hour)
        setCharSequence(clockViewId, "setFormat24Hour", formats.whenDeviceUses24Hour)
    }
}
