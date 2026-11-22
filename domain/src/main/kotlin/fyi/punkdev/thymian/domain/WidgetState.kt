package fyi.punkdev.thymian.domain

/**
 * Everything the renderer needs to draw one widget instance. It grows with each
 * phase (appearance in CWE3, size in CWE4, weather in CWE5); the renderer stays
 * the single place that turns it into views (specification 2.2).
 */
data class WidgetState(
    val clockMode: ClockMode = ClockMode.DEFAULT,
)
