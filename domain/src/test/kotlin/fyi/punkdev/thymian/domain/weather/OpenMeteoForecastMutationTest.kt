package fyi.punkdev.thymian.domain.weather

import fyi.punkdev.thymian.domain.SeededMutations
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
    private val mutationsPerFixture = 3_000

    private val fixtures =
        listOf("forecast-berlin.json", "forecast-with-unknown-fields.json", "error-latitude.json").map { name ->
            checkNotNull(javaClass.getResourceAsStream("/open-meteo/$name")) { "missing fixture $name" }.readBytes()
        }

    @Test
    fun `mutated responses never crash the parser or slip a value out of range`() {
        val random = Random(SeededMutations.SEED)
        var parsedCount = 0
        for (fixture in fixtures) {
            repeat(mutationsPerFixture) { mutationIndex ->
                val mutated = SeededMutations.mutated(fixture, random)
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
