package fyi.punkdev.thymian.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import fyi.punkdev.thymian.R

/**
 * Shown at the top of the settings screen when Thymian may not use mobile data in
 * the background, which stops the weather refreshing away from Wi-Fi. Kept in
 * plain sight with one button, because the documentation goes unread
 * (24 November 2026).
 */
class BackgroundDataNoticeSection private constructor(
    context: Context,
    onAllowBackgroundData: () -> Unit,
) {
    val view: View =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply { text = context.getString(R.string.background_data_notice_text) })
            addView(
                Button(context).apply {
                    text = context.getString(R.string.background_data_notice_allow)
                    setOnClickListener { onAllowBackgroundData() }
                },
            )
        }

    companion object {
        fun forState(
            context: Context,
            shouldShow: Boolean,
            onAllowBackgroundData: () -> Unit,
        ): BackgroundDataNoticeSection? = if (shouldShow) BackgroundDataNoticeSection(context, onAllowBackgroundData) else null

        /** The phone's background data page for this app; on Pixel it holds the Background data switch. */
        fun backgroundDataSettingsIntent(context: Context): Intent =
            Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    }
}
