package fyi.punkdev.thymian.domain.location

import fyi.punkdev.thymian.domain.SeededMutations
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

/** Seeded mutations of the recorded place search replies (CWD20): never throws, never yields an invalid place. */
class OpenMeteoGeocodingMutationTest {
    private val mutationsPerFixture = 3_000

    private val fixtures =
        listOf("geocoding-munich.json", "geocoding-no-results.json").map { name ->
            checkNotNull(javaClass.getResourceAsStream("/open-meteo/$name")) { "missing fixture $name" }.readBytes()
        }

    @Test
    fun `mutated replies never crash the parser or yield an invalid candidate`() {
        val random = Random(SeededMutations.SEED)
        var candidatesSeen = 0
        for (fixture in fixtures) {
            repeat(mutationsPerFixture) { mutationIndex ->
                val result =
                    runCatching { OpenMeteoGeocoding.parse(SeededMutations.mutated(fixture, random)) }
                        .getOrElse { failure ->
                            throw AssertionError("mutation $mutationIndex threw ${failure::class.simpleName}", failure)
                        }
                if (result is PlaceSearchResult.Found) {
                    assertTrue(result.candidates.size <= 10)
                    for (candidate in result.candidates) {
                        candidatesSeen += 1
                        assertTrue(candidate.name.isNotBlank() && candidate.name.length <= PlaceNameChoice.MAXIMUM_LENGTH, candidate.name)
                        assertTrue(candidate.coordinates.latitudeDegrees in -90.0..90.0)
                        assertTrue(candidate.coordinates.longitudeDegrees in -180.0..180.0)
                    }
                }
            }
        }
        assertTrue(candidatesSeen > 0, "no mutation produced a candidate")
    }
}
