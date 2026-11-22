package fyi.punkdev.thymian.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.location.PlaceNameChoice
import fyi.punkdev.thymian.domain.location.PlaceNameSource
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Adapter: the platform Geocoder's asynchronous API (CWD6). Blocking; called from background work. */
class GeocoderPlaceNameSource(
    private val context: Context,
    private val locale: Locale,
) : PlaceNameSource {
    override fun placeNameFor(coordinates: Coordinates): String? {
        if (!Geocoder.isPresent()) {
            Log.w(LOG_TAG, "event=geocoder_absent")
            return null
        }
        val found = AtomicReference<Address?>()
        val finished = CountDownLatch(1)
        Geocoder(context, locale).getFromLocation(
            coordinates.latitudeDegrees,
            coordinates.longitudeDegrees,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    found.set(addresses.firstOrNull())
                    finished.countDown()
                }

                override fun onError(errorMessage: String?) {
                    Log.w(LOG_TAG, "event=geocoder_failed message_length=${errorMessage?.length ?: 0}")
                    finished.countDown()
                }
            },
        )
        finished.await(WAIT_SECONDS, TimeUnit.SECONDS)
        val address = found.get() ?: return null
        return PlaceNameChoice.choose(address.locality, address.subAdminArea, address.adminArea)
    }

    private companion object {
        const val LOG_TAG = "Thymian"
        const val WAIT_SECONDS = 10L
    }
}
