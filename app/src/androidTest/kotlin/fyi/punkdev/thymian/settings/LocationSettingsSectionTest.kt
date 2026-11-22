package fyi.punkdev.thymian.settings

import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fyi.punkdev.thymian.domain.location.Coordinates
import fyi.punkdev.thymian.domain.location.FixedPlace
import fyi.punkdev.thymian.domain.location.LocationMode
import fyi.punkdev.thymian.domain.location.LocationPreference
import fyi.punkdev.thymian.domain.location.PlaceCandidate
import fyi.punkdev.thymian.domain.location.PlaceQuery
import fyi.punkdev.thymian.domain.location.PlaceSearch
import fyi.punkdev.thymian.domain.location.PlaceSearchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor

/** The app wide location choice on the settings screen (specification 1.3, CWD36), with a fake place search. */
@RunWith(AndroidJUnit4::class)
class LocationSettingsSectionTest {
    private val themedContext = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), android.R.style.Theme_DeviceDefault)
    private val munich = PlaceCandidate("München", region = "Bayern", countryCode = "DE", coordinates = Coordinates(48.13743, 11.57549))
    private val immediately = Executor { task -> task.run() }

    private class RecordingPlaceSearch(
        private val result: PlaceSearchResult,
    ) : PlaceSearch {
        val queries = mutableListOf<String>()

        override fun search(query: PlaceQuery): PlaceSearchResult {
            queries += query.text
            return result
        }
    }

    private fun onMainThread(testBody: () -> Unit) = InstrumentationRegistry.getInstrumentation().runOnMainSync(testBody)

    private fun section(
        initial: LocationPreference = LocationPreference(LocationMode.AUTOMATIC),
        search: PlaceSearch = RecordingPlaceSearch(PlaceSearchResult.Found(listOf(munich))),
    ) = LocationSettingsSection(themedContext, initial, search, backgroundExecutor = immediately, mainExecutor = immediately) { }

    private fun View.descendants(): List<View> =
        if (this is ViewGroup) {
            listOf(this) +
                (0 until childCount).flatMap { childIndex ->
                    getChildAt(childIndex).descendants()
                }
        } else {
            listOf(this)
        }

    private fun View.textViews() = descendants().filterIsInstance<TextView>()

    @Test
    fun `choosing fixed and a search result makes that place the app wide location`() =
        onMainThread {
            val locationSection = section()
            val view = locationSection.view
            view
                .textViews()
                .filterIsInstance<RadioButton>()
                .single { option -> option.text == "Fixed place" }
                .performClick()
            view
                .descendants()
                .filterIsInstance<EditText>()
                .single()
                .setText("Munich")
            view
                .textViews()
                .filterIsInstance<Button>()
                .single { button -> button.text == "Search" }
                .performClick()
            view
                .textViews()
                .filterIsInstance<Button>()
                .single { button -> button.text == "München, Bayern, DE" }
                .performClick()

            assertEquals(LocationPreference(LocationMode.FIXED, FixedPlace("München", munich.coordinates)), locationSection.preference())
        }

    @Test
    fun `following the phone keeps a chosen place as the fallback`() =
        onMainThread {
            val chosen = FixedPlace("München", munich.coordinates)
            val locationSection = section(initial = LocationPreference(LocationMode.FIXED, chosen))
            locationSection.view
                .textViews()
                .filterIsInstance<RadioButton>()
                .single { option ->
                    option.text == "Follow the phone"
                }.performClick()

            assertEquals(LocationPreference(LocationMode.AUTOMATIC, chosen), locationSection.preference())
        }

    @Test
    fun `a one letter search is not sent and says why`() =
        onMainThread {
            val search = RecordingPlaceSearch(PlaceSearchResult.Found(listOf(munich)))
            val view = section(search = search).view
            view
                .descendants()
                .filterIsInstance<EditText>()
                .single()
                .setText("M")
            view
                .textViews()
                .filterIsInstance<Button>()
                .single { button -> button.text == "Search" }
                .performClick()

            assertEquals(emptyList<String>(), search.queries)
            assertTrue(view.textViews().any { textView -> textView.text == "Type at least two letters." })
        }

    @Test
    fun `a failed search says so and keeps the current choice`() =
        onMainThread {
            val chosen = FixedPlace("München", munich.coordinates)
            val locationSection =
                section(initial = LocationPreference(LocationMode.FIXED, chosen), search = RecordingPlaceSearch(PlaceSearchResult.Failed))
            val view = locationSection.view
            view
                .descendants()
                .filterIsInstance<EditText>()
                .single()
                .setText("Ulm")
            view
                .textViews()
                .filterIsInstance<Button>()
                .single { button -> button.text == "Search" }
                .performClick()

            assertTrue(view.textViews().any { textView -> textView.text == "Search failed. Check the connection and try again." })
            assertEquals(LocationPreference(LocationMode.FIXED, chosen), locationSection.preference())
        }

    @Test
    fun `no results says nothing was found`() =
        onMainThread {
            val view = section(search = RecordingPlaceSearch(PlaceSearchResult.Found(emptyList()))).view
            view
                .descendants()
                .filterIsInstance<EditText>()
                .single()
                .setText("Xqzqx")
            view
                .textViews()
                .filterIsInstance<Button>()
                .single { button -> button.text == "Search" }
                .performClick()

            assertTrue(view.textViews().any { textView -> textView.text == "No places found." })
        }
}
