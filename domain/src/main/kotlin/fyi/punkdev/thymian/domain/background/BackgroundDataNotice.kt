package fyi.punkdev.thymian.domain.background

import fyi.punkdev.thymian.domain.diagnostics.DiagnosticEvent

/**
 * Whether the settings screen should offer to allow background data. The block
 * can only be seen from the background, and the settings screen is on screen,
 * so the last scheduled run that could tell decides.
 */
object BackgroundDataNotice {
    fun shouldShow(events: List<DiagnosticEvent>): Boolean =
        events
            .filter { event -> event.kind == "background_state" }
            .lastOrNull { event -> "network_blocked=true" in event.detail || "network_blocked=false" in event.detail }
            ?.detail
            ?.contains("network_blocked=true") == true
}
