package fyi.punkdev.thymian.location

import android.util.Log
import fyi.punkdev.thymian.domain.location.OpenMeteoGeocoding
import fyi.punkdev.thymian.domain.location.PlaceQuery
import fyi.punkdev.thymian.domain.location.PlaceSearch
import fyi.punkdev.thymian.domain.location.PlaceSearchResult
import fyi.punkdev.thymian.weather.BoundedHttpGet
import fyi.punkdev.thymian.weather.HttpReply

/** Adapter: Open-Meteo place search over HTTPS. Logs lengths and counts, never the query (datum 0.15). */
class HttpPlaceSearch(
    private val languageCode: String,
    private val httpGet: BoundedHttpGet = BoundedHttpGet(OpenMeteoGeocoding.MAXIMUM_BODY_BYTES + 1),
) : PlaceSearch {
    override fun search(query: PlaceQuery): PlaceSearchResult {
        val reply = httpGet.get(OpenMeteoGeocoding.requestUrl(query, languageCode))
        if (reply is HttpReply.NetworkFailure) {
            Log.w(LOG_TAG, "event=place_search_failed failure=NETWORK exception=${reply.exceptionName}")
            return PlaceSearchResult.Failed
        }
        val body = reply as HttpReply.Body
        val result = if (body.status in BoundedHttpGet.HTTP_SUCCESS) OpenMeteoGeocoding.parse(body.bytes) else PlaceSearchResult.Failed
        when (result) {
            is PlaceSearchResult.Found -> Log.i(LOG_TAG, "event=place_search status=${body.status} candidates=${result.candidates.size}")
            PlaceSearchResult.Failed -> Log.w(LOG_TAG, "event=place_search_failed status=${body.status} body_length=${body.bytes.size}")
        }
        return result
    }

    private companion object {
        const val LOG_TAG = "Thymian"
    }
}
