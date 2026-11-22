package fyi.punkdev.thymian.domain.settings

import kotlin.math.roundToInt

/** A widget's appearance in the form the views take it, derived from its settings. */
data class WidgetAppearance(
    val backgroundArgb: Int,
    val cornerRadiusDp: Int,
    val textArgb: Int,
) {
    companion object {
        private const val OPAQUE_ALPHA = 0xFF
        private const val MAXIMUM_PERCENT = 100.0

        fun from(settings: WidgetSettings): WidgetAppearance {
            val opacityAlpha =
                (settings[SettingDeclarations.BACKGROUND_OPACITY_PERCENT] * OPAQUE_ALPHA / MAXIMUM_PERCENT).roundToInt()
            return WidgetAppearance(
                backgroundArgb = withAlpha(settings[SettingDeclarations.BACKGROUND_COLOUR], opacityAlpha),
                cornerRadiusDp = settings[SettingDeclarations.CORNER_RADIUS_DP],
                textArgb = withAlpha(settings[SettingDeclarations.TEXT_COLOUR], OPAQUE_ALPHA),
            )
        }

        private fun withAlpha(
            rgb: Int,
            alpha: Int,
        ): Int = (alpha shl 24) or rgb
    }
}
