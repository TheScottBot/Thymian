package fyi.punkdev.thymian.domain.settings

/**
 * One setting, declared once: key, type, default, bounds and display label
 * (specification 2.2). Persistence, defaults, validation and the settings screen
 * controls are all derived from these declarations.
 */
sealed class SettingDeclaration<T : Any>(
    val key: String,
    val label: String,
    val defaultValue: T,
) {
    /** Brings any value inside this setting's bounds; the one place validation happens. */
    abstract fun normalise(value: T): T

    abstract fun encode(value: T): String

    /** Returns null when [storedText] is not a value of this setting. */
    abstract fun decode(storedText: String): T?

    override fun toString(): String = key
}

class ToggleSetting(
    key: String,
    label: String,
    defaultValue: Boolean,
) : SettingDeclaration<Boolean>(key, label, defaultValue) {
    override fun normalise(value: Boolean): Boolean = value

    override fun encode(value: Boolean): String = value.toString()

    override fun decode(storedText: String): Boolean? = storedText.toBooleanStrictOrNull()
}

class WholeNumberSetting(
    key: String,
    label: String,
    defaultValue: Int,
    val bounds: IntRange,
    val unitLabel: String,
) : SettingDeclaration<Int>(key, label, defaultValue) {
    override fun normalise(value: Int): Int = value.coerceIn(bounds)

    override fun encode(value: Int): String = value.toString()

    override fun decode(storedText: String): Int? = storedText.toIntOrNull()?.let { storedNumber -> normalise(storedNumber) }
}

/** A colour as 0xRRGGBB. Opacity is its own setting, so any alpha channel is dropped. */
class ColourSetting(
    key: String,
    label: String,
    defaultValue: Int,
) : SettingDeclaration<Int>(key, label, defaultValue) {
    override fun normalise(value: Int): Int = value and RGB_MASK

    override fun encode(value: Int): String = "%06X".format(normalise(value))

    override fun decode(storedText: String): Int? =
        if (storedText.length == HEX_DIGITS) storedText.toIntOrNull(radix = 16)?.let { storedColour -> normalise(storedColour) } else null

    private companion object {
        const val RGB_MASK = 0xFFFFFF
        const val HEX_DIGITS = 6
    }
}

data class ChoiceOption<E : Enum<E>>(
    val value: E,
    val label: String,
)

class ChoiceSetting<E : Enum<E>>(
    key: String,
    label: String,
    defaultValue: E,
    val options: List<ChoiceOption<E>>,
) : SettingDeclaration<E>(key, label, defaultValue) {
    override fun normalise(value: E): E = value

    override fun encode(value: E): String = value.name

    override fun decode(storedText: String): E? = options.firstOrNull { option -> option.value.name == storedText }?.value
}
