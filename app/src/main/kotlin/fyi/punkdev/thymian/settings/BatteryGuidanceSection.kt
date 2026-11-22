package fyi.punkdev.thymian.settings

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.diagnostics.BatteryGuidance

/**
 * Guidance for keeping the refresh alive on HyperOS (specification 1.3), on
 * Xiaomi phones only. The wording names the two settings the specification gives
 * (Autostart and No restrictions) without exact menu paths: the HyperOS menus
 * could not be checked on the phone (CWP7, CWD30), so the text says names may
 * differ. The button opens the app's own system settings page, a standard
 * Android screen.
 */
class BatteryGuidanceSection private constructor(
    context: Context,
    onOpenAppSettings: () -> Unit,
) {
    val view: View =
        CollapsibleSection(
            context,
            showLabel = context.getString(R.string.battery_guidance_heading),
            hideLabel = context.getString(R.string.battery_guidance_hide),
            details =
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(TextView(context).apply { text = context.getString(R.string.battery_guidance_text) })
                    addView(
                        Button(context).apply {
                            text = context.getString(R.string.battery_guidance_open_settings)
                            setOnClickListener { onOpenAppSettings() }
                        },
                    )
                },
        ).view

    companion object {
        /** Null on phones that are not Xiaomi, Redmi or POCO. */
        fun forDevice(
            context: Context,
            manufacturer: String,
            brand: String,
            onOpenAppSettings: () -> Unit,
        ): BatteryGuidanceSection? =
            if (BatteryGuidance.appliesTo(manufacturer, brand)) BatteryGuidanceSection(context, onOpenAppSettings) else null
    }
}
