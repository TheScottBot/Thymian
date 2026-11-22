package fyi.punkdev.thymian.domain.settings

/** One widget instance's settings: a value for every declaration, always within bounds. */
class WidgetSettings private constructor(
    private val values: Map<SettingDeclaration<*>, Any>,
) {
    @Suppress("UNCHECKED_CAST") // Safe: values are only ever stored under their own declaration's type.
    operator fun <T : Any> get(declaration: SettingDeclaration<T>): T = values.getValue(declaration) as T

    fun valueOf(declaration: SettingDeclaration<*>): Any = values.getValue(declaration)

    fun <T : Any> with(
        declaration: SettingDeclaration<T>,
        value: T,
    ): WidgetSettings = WidgetSettings(values + (declaration to declaration.normalise(value)))

    override fun equals(other: Any?): Boolean = other is WidgetSettings && other.values == values

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String =
        values.entries.joinToString(prefix = "WidgetSettings(", postfix = ")") { (declaration, value) -> "$declaration=$value" }

    companion object {
        fun defaults(): WidgetSettings = WidgetSettings(SettingDeclarations.all.associateWith { declaration -> declaration.defaultValue })
    }
}
