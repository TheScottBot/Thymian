package fyi.punkdev.thymian.settings

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.settings.AppPackageSetting
import fyi.punkdev.thymian.domain.settings.ChoiceSetting
import fyi.punkdev.thymian.domain.settings.ColourSetting
import fyi.punkdev.thymian.domain.settings.SettingDeclaration
import fyi.punkdev.thymian.domain.settings.SettingDeclarations
import fyi.punkdev.thymian.domain.settings.ToggleSetting
import fyi.punkdev.thymian.domain.settings.WholeNumberSetting
import fyi.punkdev.thymian.domain.settings.WidgetSettings

/**
 * Generates one control per declared setting (specification 2.2): a new
 * declaration appears on the settings screen with no change here unless it is a
 * new kind of setting. Each control is tagged with its setting's key.
 */
class SettingControls(
    private val context: Context,
    private val onSettingsChanged: (updatedSettings: WidgetSettings) -> Unit,
) {
    private var currentSettings = WidgetSettings.defaults()

    fun build(initialSettings: WidgetSettings): List<View> {
        currentSettings = initialSettings
        return SettingDeclarations.all.map { declaration -> withNote(declaration, controlFor(declaration)).apply { tag = declaration.key } }
    }

    private fun controlFor(declaration: SettingDeclaration<*>): View =
        when (declaration) {
            is ToggleSetting -> toggleControl(declaration)
            is WholeNumberSetting -> wholeNumberControl(declaration)
            is ColourSetting -> colourControl(declaration)
            is ChoiceSetting<*> -> choiceControl(declaration)
            is AppPackageSetting -> appControl(declaration)
        }

    private fun withNote(
        declaration: SettingDeclaration<*>,
        control: View,
    ): View {
        val note = declaration.note ?: return control
        val noteText =
            TextView(context).apply {
                text = note
                setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Small)
            }
        return verticalGroup(control, noteText)
    }

    private fun update(changedSettings: WidgetSettings) {
        currentSettings = changedSettings
        onSettingsChanged(currentSettings)
    }

    private fun toggleControl(declaration: ToggleSetting): View =
        Switch(context).apply {
            text = declaration.label
            isChecked = currentSettings[declaration]
            setOnCheckedChangeListener { _, isNowChecked -> update(currentSettings.with(declaration, isNowChecked)) }
            setPadding(0, spacing(), 0, spacing())
        }

    private fun wholeNumberControl(declaration: WholeNumberSetting): View {
        val valueLabel = TextView(context)

        fun describe(value: Int) {
            valueLabel.text = context.getString(R.string.setting_value_with_unit, declaration.label, value, declaration.unitLabel)
        }
        describe(currentSettings[declaration])
        val slider =
            SeekBar(context).apply {
                min = declaration.bounds.first
                max = declaration.bounds.last
                progress = currentSettings[declaration]
                contentDescription = declaration.label
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            changedSlider: SeekBar,
                            newProgress: Int,
                            changedByUser: Boolean,
                        ) {
                            describe(newProgress)
                            update(currentSettings.with(declaration, newProgress))
                        }

                        override fun onStartTrackingTouch(touchedSlider: SeekBar) = Unit

                        override fun onStopTrackingTouch(touchedSlider: SeekBar) = Unit
                    },
                )
            }
        return verticalGroup(valueLabel, slider)
    }

    private fun choiceControl(declaration: ChoiceSetting<*>): View {
        val options =
            RadioGroup(context).apply {
                orientation = RadioGroup.HORIZONTAL
                declaration.optionLabels.forEachIndexed { optionIndex, optionLabel ->
                    addView(
                        RadioButton(context).apply {
                            id = View.generateViewId()
                            text = optionLabel
                            tag = optionIndex
                        },
                    )
                }
                check(getChildAt(declaration.selectedIndexIn(currentSettings)).id)
                setOnCheckedChangeListener { group, checkedId ->
                    val optionIndex = group.findViewById<View>(checkedId).tag as Int
                    update(declaration.withOptionAt(currentSettings, optionIndex))
                }
            }
        return verticalGroup(labelFor(declaration), options)
    }

    /** Shows the chosen app and offers every app with a home screen icon, by name. */
    private fun appControl(declaration: AppPackageSetting): View {
        val chosenApp = TextView(context)

        fun describe(packageName: String) {
            val appName =
                when {
                    packageName.isEmpty() -> {
                        context.getString(R.string.weather_app_none_chosen)
                    }

                    else -> {
                        LaunchableApps.labelFor(context, packageName)
                            ?: context.getString(R.string.weather_app_not_installed, packageName)
                    }
                }
            chosenApp.text = context.getString(R.string.weather_app_chosen, declaration.label, appName)
        }
        describe(currentSettings[declaration])
        val chooseButton =
            Button(context).apply {
                text = context.getString(R.string.weather_app_choose)
                setOnClickListener {
                    val apps = LaunchableApps.list(context)
                    AlertDialog
                        .Builder(context)
                        .setTitle(R.string.weather_app_dialog_title)
                        .setItems(apps.map { app -> app.label }.toTypedArray()) { _, chosenIndex ->
                            val chosenPackage = apps[chosenIndex].packageName
                            describe(chosenPackage)
                            update(currentSettings.with(declaration, chosenPackage))
                        }.setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            }
        return verticalGroup(chosenApp, chooseButton)
    }

    private fun colourControl(declaration: ColourSetting): View {
        val hexField =
            EditText(context).apply {
                setText(declaration.encode(currentSettings[declaration]))
                hint = "RRGGBB"
                contentDescription = context.getString(R.string.colour_hex_description, declaration.label)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                filters = arrayOf(InputFilter.LengthFilter(HEX_DIGITS))
                addTextChangedListener(
                    object : TextWatcher {
                        override fun afterTextChanged(editedText: Editable) {
                            // Partial or invalid entries are ignored until they form a colour.
                            declaration.decode(editedText.toString())?.let { typedColour ->
                                if (typedColour != currentSettings[declaration]) update(currentSettings.with(declaration, typedColour))
                            }
                        }

                        override fun beforeTextChanged(
                            unchangedText: CharSequence,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) = Unit

                        override fun onTextChanged(
                            changedText: CharSequence,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) = Unit
                    },
                )
            }
        val swatchRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        for (namedColour in ColourPalette.swatches) {
            swatchRow.addView(
                View(context).apply {
                    contentDescription = namedColour.name
                    isClickable = true
                    isFocusable = true
                    background =
                        GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(OPAQUE or namedColour.rgb)
                            setStroke(dp(1), OUTLINE_GREY)
                        }
                    layoutParams = LinearLayout.LayoutParams(dp(SWATCH_DP), dp(SWATCH_DP)).apply { marginEnd = spacing() }
                    setOnClickListener { hexField.setText(declaration.encode(namedColour.rgb)) }
                },
            )
        }
        return verticalGroup(labelFor(declaration), HorizontalScrollView(context).apply { addView(swatchRow) }, hexField)
    }

    private fun labelFor(declaration: SettingDeclaration<*>): TextView = TextView(context).apply { text = declaration.label }

    private fun verticalGroup(vararg children: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, spacing(), 0, spacing())
            children.forEach { child -> addView(child) }
        }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()

    private fun spacing(): Int = dp(SPACING_DP)

    private companion object {
        const val HEX_DIGITS = 6
        const val SWATCH_DP = 36
        const val SPACING_DP = 8
        const val OPAQUE = 0xFF000000.toInt()
        const val OUTLINE_GREY = 0xFF808080.toInt()
    }
}
