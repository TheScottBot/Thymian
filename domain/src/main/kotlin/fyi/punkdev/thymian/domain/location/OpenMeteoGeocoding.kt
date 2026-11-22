package fyi.punkdev.thymian.domain.location

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import java.net.URLEncoder

/** A place name to search for, trimmed and bounded before it is sent anywhere. */
@JvmInline
value class PlaceQuery private constructor(
    val text: String,
) {
    companion object {
        // The geocoding API returns nothing for fewer than two characters (Plan record 8.2).
        private val ACCEPTED_LENGTH = 2..80

        fun orNull(typed: String): PlaceQuery? = typed.trim().takeIf { trimmed -> trimmed.length in ACCEPTED_LENGTH }?.let(::PlaceQuery)
    }
}

/** One search result; region and country tell same named places apart. */
data class PlaceCandidate(
    val name: String,
    val region: String?,
    val countryCode: String?,
    val coordinates: Coordinates,
)

sealed interface PlaceSearchResult {
    data class Found(
        val candidates: List<PlaceCandidate>,
    ) : PlaceSearchResult

    /** The reply could not be used; the reason is logged by category in the adapter, not kept here. */
    data object Failed : PlaceSearchResult
}

/** Port: searches places by name for the fixed location (specification 1.3). Blocking. */
interface PlaceSearch {
    fun search(query: PlaceQuery): PlaceSearchResult
}

/**
 * The Open-Meteo geocoding contract as read on 22 November 2026 (Plan record
 * 8.2), parsed defensively like the forecast (datum 0.10, CWD19): bounded first,
 * unknown fields tolerated, and a candidate with a missing name or invalid
 * coordinates skipped rather than failing the whole reply. A reply with no
 * `results` key means nothing was found, as observed on the live API.
 */
object OpenMeteoGeocoding {
    const val MAXIMUM_BODY_BYTES = 64 * 1024
    private const val ENDPOINT = "https://geocoding-api.open-meteo.com/v1/search"
    private const val MAXIMUM_CANDIDATES = 10
    private val LANGUAGE_CODE_PATTERN = Regex("[a-z]{2}")
    private val COUNTRY_CODE_PATTERN = Regex("[A-Z]{2}")

    fun requestUrl(
        query: PlaceQuery,
        languageCode: String,
    ): String {
        val language = languageCode.lowercase().takeIf { code -> LANGUAGE_CODE_PATTERN.matches(code) } ?: "en"
        val encodedName = URLEncoder.encode(query.text, Charsets.UTF_8).replace("+", "%20")
        return "$ENDPOINT?name=$encodedName&count=$MAXIMUM_CANDIDATES&language=$language&format=json"
    }

    fun parse(body: ByteArray): PlaceSearchResult {
        if (body.size > MAXIMUM_BODY_BYTES) return PlaceSearchResult.Failed
        val root =
            try {
                Json.parseToJsonElement(body.decodeToString()) as? JsonObject
            } catch (malformedJson: SerializationException) {
                null
            } ?: return PlaceSearchResult.Failed
        if ((root["error"] as? JsonPrimitive)?.booleanOrNull == true) return PlaceSearchResult.Failed
        val results = root["results"] ?: return PlaceSearchResult.Found(emptyList())
        if (results !is JsonArray) return PlaceSearchResult.Failed
        return PlaceSearchResult.Found(results.mapNotNull { result -> candidateFrom(result) }.take(MAXIMUM_CANDIDATES))
    }

    private fun candidateFrom(result: JsonElement): PlaceCandidate? {
        val fields = result as? JsonObject ?: return null
        val name =
            fields
                .text("name")
                ?.trim()
                ?.takeIf { trimmed -> trimmed.isNotEmpty() }
                ?.take(PlaceNameChoice.MAXIMUM_LENGTH) ?: return null
        val latitude = fields.number("latitude") ?: return null
        val longitude = fields.number("longitude") ?: return null
        val coordinates = Coordinates.orNull(latitude, longitude) ?: return null
        val region =
            fields
                .text("admin1")
                ?.trim()
                ?.takeIf { trimmed -> trimmed.isNotEmpty() }
                ?.take(PlaceNameChoice.MAXIMUM_LENGTH)
        val countryCode = fields.text("country_code")?.takeIf { code -> COUNTRY_CODE_PATTERN.matches(code) }
        return PlaceCandidate(name, region, countryCode, coordinates)
    }

    private fun JsonObject.text(field: String): String? =
        (this[field] as? JsonPrimitive)?.takeIf { primitive -> primitive.isString }?.content

    private fun JsonObject.number(field: String): Double? =
        (this[field] as? JsonPrimitive)?.takeIf { primitive -> !primitive.isString }?.doubleOrNull
}
