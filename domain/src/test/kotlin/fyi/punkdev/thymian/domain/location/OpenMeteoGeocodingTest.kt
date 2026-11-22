package fyi.punkdev.thymian.domain.location

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Place search for the fixed location (specification 1.3, 2.5), against the
 * Open-Meteo geocoding contract read on 22 November 2026 (Plan record 8.2).
 */
class OpenMeteoGeocodingTest {
    private fun fixture(name: String): ByteArray =
        checkNotNull(javaClass.getResourceAsStream("/open-meteo/$name")) { "missing fixture $name" }.readBytes()

    @Test
    fun `the request encodes the name and asks for ten results in the given language`() {
        assertEquals(
            "https://geocoding-api.open-meteo.com/v1/search?name=Bad%20T%C3%B6lz&count=10&language=de&format=json",
            OpenMeteoGeocoding.requestUrl(PlaceQuery.orNull(" Bad Tölz ")!!, languageCode = "de"),
        )
    }

    @Test
    fun `a query needs at least two characters and at most eighty`() {
        assertEquals(null, PlaceQuery.orNull(" a "))
        assertEquals(null, PlaceQuery.orNull("x".repeat(81)))
        assertEquals("Bo", PlaceQuery.orNull("Bo")?.text)
    }

    @Test
    fun `results become candidates with region and country for telling them apart`() {
        val result = OpenMeteoGeocoding.parse(fixture("geocoding-munich.json"))

        assertEquals(
            PlaceSearchResult.Found(
                listOf(
                    PlaceCandidate("München", region = "Bayern", countryCode = "DE", coordinates = Coordinates(48.13743, 11.57549)),
                    PlaceCandidate("Munich", region = "North Dakota", countryCode = "US", coordinates = Coordinates(48.6025, -98.83148)),
                ),
            ),
            result,
        )
    }

    @Test
    fun `a reply with no results key means nothing was found`() {
        assertEquals(PlaceSearchResult.Found(emptyList()), OpenMeteoGeocoding.parse(fixture("geocoding-no-results.json")))
    }

    @Test
    fun `a candidate missing a name or valid coordinates is skipped, not the whole reply`() {
        val body =
            """{"results":[{"name":"Nowhere","latitude":95.0,"longitude":0.0},""" +
                """{"latitude":1.0,"longitude":1.0},{"name":"Ulm","latitude":48.4,"longitude":9.99}]}"""

        val result = OpenMeteoGeocoding.parse(body.toByteArray())

        assertEquals(PlaceSearchResult.Found(listOf(PlaceCandidate("Ulm", null, null, Coordinates(48.4, 9.99)))), result)
    }

    @Test
    fun `the provider error shape, oversized and malformed replies are refused`() {
        assertEquals(PlaceSearchResult.Failed, OpenMeteoGeocoding.parse("""{"error":true,"reason":"bad"}""".toByteArray()))
        assertEquals(PlaceSearchResult.Failed, OpenMeteoGeocoding.parse(ByteArray(64 * 1024 + 1) { ' '.code.toByte() }))
        assertEquals(PlaceSearchResult.Failed, OpenMeteoGeocoding.parse("not json".toByteArray()))
        assertEquals(PlaceSearchResult.Failed, OpenMeteoGeocoding.parse("""{"results":"many"}""".toByteArray()))
    }

    @Test
    fun `at most ten candidates are kept and long names are cut to the widget limit`() {
        val many =
            (1..15).joinToString(
                separator = ",",
            ) { index -> """{"name":"${"P".repeat(60)}$index","latitude":1.0,"longitude":1.0}""" }

        val candidates = (OpenMeteoGeocoding.parse("""{"results":[$many]}""".toByteArray()) as PlaceSearchResult.Found).candidates

        assertEquals(10, candidates.size)
        assertEquals(PlaceNameChoice.MAXIMUM_LENGTH, candidates.first().name.length)
    }
}
