package fyi.punkdev.thymian.spike

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.RemoteViews

/** Spike only: pushes the test layout and the spanned clock format to every instance. */
class SpikeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildSpikeRemoteViews(context))
        }
        Log.i(LOG_TAG, "event=widget_updated instance_count=${appWidgetIds.size}")
    }

    companion object {
        private const val LOG_TAG = "ThymianSpike"

        fun buildSpikeRemoteViews(context: Context): RemoteViews {
            val remoteViews = RemoteViews(context.packageName, R.layout.spike_widget)
            val spannedFormat = buildTimeFormatWithHalfSizeSuffix()
            // Both formats are set so row 5 shows the suffix whatever the device clock setting.
            remoteViews.setCharSequence(R.id.row_spanned_clock, "setFormat12Hour", spannedFormat)
            remoteViews.setCharSequence(R.id.row_spanned_clock, "setFormat24Hour", spannedFormat)
            return remoteViews
        }

        /** The pattern `h:mm a` with the trailing `a` at half size, testing whether spans survive RemoteViews. */
        fun buildTimeFormatWithHalfSizeSuffix(): CharSequence {
            val pattern = "h:mm a"
            val suffixStart = pattern.indexOf('a')
            val spannablePattern = SpannableString(pattern)
            spannablePattern.setSpan(
                RelativeSizeSpan(0.5f),
                suffixStart,
                suffixStart + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            return spannablePattern
        }
    }
}
