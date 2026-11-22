package fyi.punkdev.thymian.domain.frontdoor

/**
 * Where the home screen icon leads. Settings belong to a widget, so with a
 * widget placed the icon opens that widget's settings; with none it offers to
 * add one.
 */
sealed interface FrontDoorDestination {
    data object AddWidget : FrontDoorDestination

    data class WidgetSettings(
        val appWidgetId: Int,
    ) : FrontDoorDestination

    companion object {
        /** Launchers hand out widget ids in increasing order, so the highest id is the most recently placed widget. */
        fun choose(placedWidgetIds: List<Int>): FrontDoorDestination =
            placedWidgetIds.maxOrNull()?.let { newestWidgetId -> WidgetSettings(newestWidgetId) } ?: AddWidget

        /** The widget placed since [placedBefore] was read, or null when none was. */
        fun newlyPlacedWidget(
            placedBefore: List<Int>,
            placedNow: List<Int>,
        ): Int? = (placedNow - placedBefore.toSet()).maxOrNull()
    }
}
