package fyi.punkdev.thymian.settings

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticsReport
import fyi.punkdev.thymian.domain.diagnostics.DiagnosticsSummary
import fyi.punkdev.thymian.domain.location.LocationSourceUsed
import fyi.punkdev.thymian.domain.weather.WeatherSnapshot
import java.time.Instant

/**
 * The diagnostics section (specification 1.3, CWD31): last refresh, last error,
 * location source, next scheduled refresh and recent events, plus Copy
 * diagnostics, which hands the plain report text to [onCopy]. Nothing here
 * shows or copies coordinates or weather values (datum 0.15).
 */
class DiagnosticsSection(
    private val context: Context,
    appVersion: String,
    weather: WeatherSnapshot?,
    events: List<DiagnosticEvent>,
    nextRefresh: Instant?,
    private val onCopy: (reportText: String) -> Unit,
) {
    val view: View

    init {
        val summary = DiagnosticsSummary.from(weather, events)
        val lines =
            listOf(
                context.getString(
                    R.string.diagnostics_last_refresh,
                    summary.lastRefresh?.let(::describeTime) ?: context.getString(R.string.diagnostics_not_yet),
                ),
                context.getString(
                    R.string.diagnostics_location_source,
                    summary.locationSource?.let(::describeSource) ?: context.getString(R.string.diagnostics_not_yet),
                ),
                context.getString(
                    R.string.diagnostics_last_error,
                    summary.lastError?.let { error -> "${describeTime(error.occurredAt)}, ${error.kind} ${error.detail}" }
                        ?: context.getString(R.string.diagnostics_none),
                ),
                context.getString(
                    R.string.diagnostics_next_refresh,
                    nextRefresh?.let(::describeTime) ?: context.getString(R.string.diagnostics_not_scheduled),
                ),
            )
        val recent =
            events
                .takeLast(
                    RECENT_EVENTS_SHOWN,
                ).reversed()
                .joinToString(separator = "\n") { event -> "${describeTime(event.occurredAt)} ${event.kind} ${event.detail}" }
        val reportText = DiagnosticsReport.asText(appVersion, events)
        view =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(context).apply { text = context.getString(R.string.diagnostics_heading) })
                lines.forEach { line -> addView(TextView(context).apply { text = line }) }
                if (recent.isNotEmpty()) {
                    addView(
                        TextView(context).apply {
                            text = recent
                            setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Small)
                        },
                    )
                }
                addView(
                    Button(context).apply {
                        text = context.getString(R.string.diagnostics_copy)
                        setOnClickListener { onCopy(reportText) }
                    },
                )
            }
    }

    private fun describeTime(instant: Instant): String =
        DateUtils.formatDateTime(
            context,
            instant.toEpochMilli(),
            DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH,
        )

    private fun describeSource(source: LocationSourceUsed): String =
        context.getString(
            when (source) {
                LocationSourceUsed.FIXED_CHOSEN -> R.string.diagnostics_source_fixed_chosen
                LocationSourceUsed.DEVICE_RECENT -> R.string.diagnostics_source_device_recent
                LocationSourceUsed.DEVICE_CURRENT -> R.string.diagnostics_source_device_current
                LocationSourceUsed.DEVICE_LAST_KNOWN -> R.string.diagnostics_source_device_last_known
                LocationSourceUsed.FIXED_FALLBACK -> R.string.diagnostics_source_fixed_fallback
            },
        )

    private companion object {
        const val RECENT_EVENTS_SHOWN = 10
    }
}
