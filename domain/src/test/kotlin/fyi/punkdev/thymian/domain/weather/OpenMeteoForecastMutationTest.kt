package fyi.punkdev.thymian.domain.weather

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * The fuzzing substitute decided in CWD20: thousands of seeded mutations of the
 * recorded responses. The parser must always return a result, never throw, and
 * never accept a value outside its ranges. The fixed seed makes every run, local
 * or in CI, test the same inputs, so a failure can be reproduced.
 */
class OpenMeteoForecastMutationTest {
    private val seed = 20_261_122
    private val mutationsPerFixture = 3_000

    private val fixtures =
        listOf("forecast-berlin.json", "forecast-with-unknown-fields.json", "error-latitude.json").map { name ->
            checkNotNull(javaClass.getResourceAsStream("/open-meteo/$name")) { "missing fixture $name" }.readBytes()
        }

    // Valid JSON numbers at and beyond every range boundary: random byte changes almost never produce
    // a well formed out of range value, which is what a missing range check would let through.
    private val boundaryNumbers =
        listOf(
            "70",
            "70.1",
            "71",
            "-100",
            "-100.1",
            "-101",
            "999",
            "-999",
            "1e308",
            "-1e308",
            "0",
            "-0",
            "1",
            "2",
            "-1",
            "99",
            "100",
            "2.5",
        )

    private val numberPattern = Regex("""-?\d+(\.\d+)?([eE][+-]?\d+)?""")

    private fun replaceOneNumber(
        original: ByteArray,
        random: Random,
    ): ByteArray {
        val text = original.decodeToString()
        val numbers = numberPattern.findAll(text).toList()
        if (numbers.isEmpty()) return original
        val chosen = numbers.random(random)
        return text.replaceRange(chosen.range, boundaryNumbers.random(random)).encodeToByteArray()
    }

    private val jsonFragments = listOf("{", "}", "[", "]", ":", ",", "\"", "null", "true", "1e999", "-0", "\\u0000", "9".repeat(40))

    private fun mutate(
        original: ByteArray,
        random: Random,
    ): ByteArray {
        val bytes = original.toMutableList()
        repeat(random.nextInt(1, 6)) {
            if (bytes.isEmpty()) return@repeat
            val position = random.nextInt(bytes.size)
            when (random.nextInt(5)) {
                0 -> bytes[position] = random.nextInt(256).toByte()
                1 -> bytes.removeAt(position)
                2 -> bytes.addAll(position, jsonFragments.random(random).encodeToByteArray().toList())
                3 -> while (bytes.size > position) bytes.removeAt(bytes.size - 1)
                else -> bytes.addAll(position, bytes.subList(position, minOf(bytes.size, position + random.nextInt(1, 40))).toList())
            }
        }
        return bytes.toByteArray()
    }

    @Test
    fun `mutated responses never crash the parser or slip a value out of range`() {
        val random = Random(seed)
        var parsedCount = 0
        for (fixture in fixtures) {
            repeat(mutationsPerFixture) { mutationIndex ->
                val mutated = if (random.nextBoolean()) mutate(fixture, random) else replaceOneNumber(fixture, random)
                val result =
                    runCatching { OpenMeteoForecast.parse(mutated) }
                        .getOrElse { failure ->
                            throw AssertionError("mutation $mutationIndex threw ${failure::class.simpleName}", failure)
                        }
                if (result is ForecastParseResult.Parsed) {
                    parsedCount += 1
                    val currentWeather = result.currentWeather
                    assertTrue(currentWeather.temperatureCelsius in -100.0..70.0, "temperature ${currentWeather.temperatureCelsius}")
                    assertTrue(currentWeather.condition in WeatherCondition.entries, "condition")
                    assertTrue(result.unknownFieldCount >= 0)
                }
            }
        }
        // Some mutations must survive as valid responses, or the test would only ever exercise rejection.
        assertTrue(parsedCount > 0, "no mutation parsed")
    }

    @Test
    fun `deeply nested input is rejected without exhausting the stack`() {
        val depth = 20_000
        val nestedArrays = "[".repeat(depth) + "]".repeat(depth)
        val nestedInsideObject = "{\"current\":" + "[".repeat(depth) + "]".repeat(depth) + "}"

        assertEquals(ForecastParseResult.Rejected(ForecastRejection.MALFORMED), OpenMeteoForecast.parse(nestedArrays.encodeToByteArray()))
        assertTrue(OpenMeteoForecast.parse(nestedInsideObject.encodeToByteArray()) is ForecastParseResult.Rejected)
    }
}
