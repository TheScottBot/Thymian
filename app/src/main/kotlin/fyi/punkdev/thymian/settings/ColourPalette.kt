package fyi.punkdev.thymian.settings

/** A named colour; the name is what TalkBack reads for its swatch. */
data class NamedColour(
    val name: String,
    val rgb: Int,
)

/** The swatches every colour setting offers; any other colour can be typed as hex. */
object ColourPalette {
    val swatches =
        listOf(
            NamedColour("White", 0xFFFFFF),
            NamedColour("Light grey", 0xBDBDBD),
            NamedColour("Dark grey", 0x424242),
            NamedColour("Black", 0x000000),
            NamedColour("Thyme pink", 0xF894F8),
            NamedColour("Blue", 0x4A90E2),
            NamedColour("Green", 0x4CAF50),
            NamedColour("Amber", 0xFFC107),
            NamedColour("Red", 0xE53935),
        )
}
