package fyi.punkdev.thymian.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import fyi.punkdev.thymian.R
import fyi.punkdev.thymian.domain.location.FixedPlace
import fyi.punkdev.thymian.domain.location.LocationMode
import fyi.punkdev.thymian.domain.location.LocationPreference
import fyi.punkdev.thymian.domain.location.PlaceCandidate
import fyi.punkdev.thymian.domain.location.PlaceQuery
import fyi.punkdev.thymian.domain.location.PlaceSearch
import fyi.punkdev.thymian.domain.location.PlaceSearchResult
import java.util.concurrent.Executor

/**
 * The app wide location choice (specification 1.3, CWD36): follow the phone, or
 * a fixed place found by name. Following the phone walks through the two step
 * permission flow with an explanation before each system prompt (CWD5). The
 * search runs on [backgroundExecutor] and updates the views on [mainExecutor],
 * so tests can run both immediately.
 */
class LocationSettingsSection(
    private val context: Context,
    initialPreference: LocationPreference,
    private val placeSearch: PlaceSearch,
    private val backgroundExecutor: Executor,
    private val mainExecutor: Executor,
    private val onPermissionRequested: (permission: String) -> Unit,
) {
    private var mode = initialPreference.mode
    private var chosenPlace: FixedPlace? = initialPreference.fixedPlace

    private val permissionExplanation = TextView(context)
    private val permissionButton = Button(context).apply { text = context.getString(R.string.location_permission_continue) }
    private val searchField =
        EditText(context).apply {
            hint = context.getString(R.string.location_search_hint)
            isSingleLine = true
        }
    private val searchStatus = TextView(context)
    private val searchResults = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    private val chosenPlaceText = TextView(context)
    private val automaticArea = verticalGroup(permissionExplanation, permissionButton)
    private val fixedArea =
        verticalGroup(
            searchField,
            Button(context).apply {
                text = context.getString(R.string.location_search)
                setOnClickListener { startSearch() }
            },
            searchStatus,
            searchResults,
            chosenPlaceText,
        )

    val view: View =
        verticalGroup(
            TextView(context).apply { text = context.getString(R.string.location_heading) },
            modeChoice(),
            automaticArea,
            fixedArea,
        )

    init {
        refreshPermissionState()
        showChosenPlace()
        showModeAreas()
    }

    fun preference(): LocationPreference = LocationPreference(mode, chosenPlace)

    /** Called after a permission prompt returns, and on first display. */
    fun refreshPermissionState() {
        val coarseGranted = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val backgroundGranted = isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        val nextPermission =
            when {
                !coarseGranted -> Manifest.permission.ACCESS_COARSE_LOCATION
                !backgroundGranted -> Manifest.permission.ACCESS_BACKGROUND_LOCATION
                else -> null
            }
        permissionExplanation.text =
            context.getString(
                when (nextPermission) {
                    Manifest.permission.ACCESS_COARSE_LOCATION -> R.string.location_permission_explain_approximate
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> R.string.location_permission_explain_background
                    else -> R.string.location_permission_granted
                },
            )
        permissionButton.visibility = if (nextPermission == null) View.GONE else View.VISIBLE
        permissionButton.setOnClickListener { nextPermission?.let(onPermissionRequested) }
    }

    private fun modeChoice(): RadioGroup {
        val automaticOption =
            RadioButton(context).apply {
                id = View.generateViewId()
                text = context.getString(R.string.location_mode_automatic)
            }
        val fixedOption =
            RadioButton(context).apply {
                id = View.generateViewId()
                text = context.getString(R.string.location_mode_fixed)
            }
        return RadioGroup(context).apply {
            addView(automaticOption)
            addView(fixedOption)
            check(if (mode == LocationMode.FIXED) fixedOption.id else automaticOption.id)
            setOnCheckedChangeListener { _, checkedId ->
                mode = if (checkedId == fixedOption.id) LocationMode.FIXED else LocationMode.AUTOMATIC
                showModeAreas()
            }
        }
    }

    private fun showModeAreas() {
        automaticArea.visibility = if (mode == LocationMode.AUTOMATIC) View.VISIBLE else View.GONE
        // The chosen place stays visible in automatic mode too: it is the fallback (specification 1.3).
        fixedArea.visibility = View.VISIBLE
    }

    private fun startSearch() {
        searchResults.removeAllViews()
        val query = PlaceQuery.orNull(searchField.text.toString())
        if (query == null) {
            searchStatus.text = context.getString(R.string.location_search_too_short)
            return
        }
        searchStatus.text = context.getString(R.string.location_searching)
        backgroundExecutor.execute {
            val result = placeSearch.search(query)
            mainExecutor.execute { showResult(result) }
        }
    }

    private fun showResult(result: PlaceSearchResult) {
        when (result) {
            PlaceSearchResult.Failed -> {
                searchStatus.text = context.getString(R.string.location_search_failed)
            }

            is PlaceSearchResult.Found -> {
                searchStatus.text = if (result.candidates.isEmpty()) context.getString(R.string.location_search_nothing_found) else ""
                result.candidates.forEach { candidate -> searchResults.addView(candidateButton(candidate)) }
            }
        }
    }

    private fun candidateButton(candidate: PlaceCandidate): Button =
        Button(context).apply {
            isAllCaps = false
            text = listOfNotNull(candidate.name, candidate.region, candidate.countryCode).joinToString(separator = ", ")
            setOnClickListener {
                chosenPlace = FixedPlace(candidate.name, candidate.coordinates)
                searchResults.removeAllViews()
                showChosenPlace()
            }
        }

    private fun showChosenPlace() {
        chosenPlaceText.text =
            chosenPlace?.let { place -> context.getString(R.string.location_chosen_place, place.name) }
                ?: context.getString(R.string.location_no_place_chosen)
    }

    private fun isGranted(permission: String): Boolean = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun verticalGroup(vararg children: View): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            children.forEach { child -> addView(child) }
        }
}
