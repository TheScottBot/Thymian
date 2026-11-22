package fyi.punkdev.thymian.domain

/** How the clock chooses between 12 and 24 hour time (specification 1.3). */
enum class ClockMode {
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR,
    FOLLOW_DEVICE,
    ;

    companion object {
        val DEFAULT = FOLLOW_DEVICE
    }
}
