package fyi.punkdev.thymian.settings

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * A heading that opens and closes the [details] below it, closed by default, so
 * long optional sections do not push Save down the settings screen. Shared by
 * the diagnostics and the battery guidance (specification 2.2). The heading's
 * label changes with its state, which is what TalkBack reads.
 */
class CollapsibleSection(
    context: Context,
    showLabel: String,
    hideLabel: String,
    details: View,
) {
    val view: View

    init {
        details.tag = DETAILS_TAG
        details.visibility = View.GONE
        val heading =
            TextView(context).apply {
                tag = HEADING_TAG
                text = showLabel
                isClickable = true
                isFocusable = true
                minHeight = (MINIMUM_TOUCH_DP * context.resources.displayMetrics.density).toInt()
                gravity = Gravity.CENTER_VERTICAL
                setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium)
                setOnClickListener {
                    val opening = details.visibility != View.VISIBLE
                    details.visibility = if (opening) View.VISIBLE else View.GONE
                    text = if (opening) hideLabel else showLabel
                }
            }
        view =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(heading)
                addView(details)
            }
    }

    companion object {
        const val HEADING_TAG = "collapsible_heading"
        const val DETAILS_TAG = "collapsible_details"

        // Android's recommended minimum touch target.
        private const val MINIMUM_TOUCH_DP = 48
    }
}
