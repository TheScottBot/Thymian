package fyi.punkdev.thymian.icons

import fyi.punkdev.thymian.domain.weather.WeatherCondition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * The drawables and their lookup are derived from the vendored SVGs and the one
 * WeatherCondition table (CWD24). These tests fail if a condition lacks an icon,
 * or if the committed output no longer matches a fresh conversion.
 */
class WeatherIconConversionTest {
    // Gradle runs each module's tests from the module directory.
    private val repositoryRoot = File("../..").canonicalFile
    private val paths = IconConversionPaths.inRepository(repositoryRoot)

    @Test
    fun `resource names are the icon names in snake case with a weather prefix`() {
        assertEquals("weather_partly_cloudy_day", IconNaming.resourceNameFor("partly-cloudy-day"))
        assertEquals("weather_not_available", IconNaming.resourceNameFor("not-available"))
    }

    @Test
    fun `every condition icon has a committed drawable`() {
        for (iconName in WeatherCondition.allIconNames()) {
            val drawable = File(paths.drawableDirectory, IconNaming.resourceNameFor(iconName) + ".xml")
            assertTrue(drawable.isFile, "missing ${drawable.path}")
        }
    }

    @Test
    fun `the committed drawables and lookup match a fresh conversion`(
        @TempDir freshOutput: File,
    ) {
        val freshPaths =
            paths.copy(
                drawableDirectory = File(freshOutput, "drawable"),
                lookupFile = File(freshOutput, "WeatherIconDrawables.kt"),
            )

        WeatherIconConversion.convert(freshPaths)

        for (iconName in WeatherCondition.allIconNames()) {
            val fileName = IconNaming.resourceNameFor(iconName) + ".xml"
            assertEquals(
                File(freshPaths.drawableDirectory, fileName).readText(),
                File(paths.drawableDirectory, fileName).readText(),
                fileName,
            )
        }
        assertEquals(freshPaths.lookupFile.readText(), paths.lookupFile.readText(), "WeatherIconDrawables.kt")
    }

    @Test
    fun `converted icons are white so the widget can tint them to the text colour`(
        @TempDir freshOutput: File,
    ) {
        val freshPaths =
            paths.copy(
                drawableDirectory = File(freshOutput, "drawable"),
                lookupFile = File(freshOutput, "WeatherIconDrawables.kt"),
            )
        WeatherIconConversion.convert(freshPaths)
        val colourPattern = Regex("""android:(fillColor|strokeColor)="([^"]+)"""")

        for (drawable in freshPaths.drawableDirectory.listFiles().orEmpty()) {
            val colours = colourPattern.findAll(drawable.readText()).map { match -> match.groupValues[2] }.toSet()
            // Fully transparent comes from SVG fill="none" and stays transparent when tinted.
            val tintable = setOf("#FFFFFF", "#FFFFFFFF", "#00000000")
            assertTrue(colours.all { colour -> colour.uppercase() in tintable }, "${drawable.name} has $colours")
        }
    }

    @Test
    fun `masks become clip paths rather than being dropped`(
        @TempDir freshOutput: File,
    ) {
        val freshPaths =
            paths.copy(
                drawableDirectory = File(freshOutput, "drawable"),
                lookupFile = File(freshOutput, "WeatherIconDrawables.kt"),
            )
        WeatherIconConversion.convert(freshPaths)

        assertTrue(File(freshPaths.drawableDirectory, "weather_partly_cloudy_day.xml").readText().contains("<clip-path"))
    }

    @Test
    fun `an opaque black alpha mask becomes a white clip mask, shapes outside it are untouched`() {
        val svg =
            "<svg><mask id=\"m\" style=\"mask-type:alpha\"><rect fill=\"#000\"/></mask>" +
                "<rect fill=\"#000\"/><path fill=\"currentColor\"/></svg>"

        assertEquals(
            "<svg><mask fill=\"#FFFFFF\" id=\"m\"><rect fill=\"#FFFFFF\"/></mask>" +
                "<rect fill=\"#000\"/><path fill=\"#FFFFFF\"/></svg>",
            WeatherIconConversion.prepareForConversion(svg),
        )
    }

    @Test
    fun `an alpha mask with any other colour or opacity is left for the converter to refuse`() {
        val greyMask = "<svg><mask id=\"m\" style=\"mask-type:alpha\"><rect fill=\"#808080\"/></mask></svg>"
        val fadedMask = "<svg><mask id=\"m\" style=\"mask-type:alpha\"><rect fill=\"#000\" fill-opacity=\".5\"/></mask></svg>"

        assertEquals(greyMask, WeatherIconConversion.prepareForConversion(greyMask))
        assertEquals(fadedMask, WeatherIconConversion.prepareForConversion(fadedMask))
    }
}
