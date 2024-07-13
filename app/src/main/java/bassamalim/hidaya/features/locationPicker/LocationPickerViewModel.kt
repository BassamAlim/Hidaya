package bassamalim.hidaya.features.locationPicker

import android.location.Location
import android.os.Bundle
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.nav.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val repo: LocationPickerRepository,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var lazyListState: LazyListState
    private var mode = 0
    private var countryId = -1
    val language = repo.language
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LocationPickerState(
        titleResId = getTitleResId(),
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    fun onStart(coroutineScope: CoroutineScope, lazyListState: LazyListState) {
        this.coroutineScope = coroutineScope
        this.lazyListState = lazyListState
    }

    fun onSelect(id: Int) {
        if (mode == 1) {
            repo.setLocationType(LocationType.Manual)
            repo.storeLocation(countryId, id)

            val city = repo.getCity(id)
            navigator.navigateBackWithResult(
                Bundle().apply {
                    putParcelable(
                        "location",
                        Location("").apply {
                            latitude = city.latitude
                            longitude = city.longitude
                        }
                    )
                }
            )
        }
        else {
            countryId = id

            mode = 1

            searchText = ""

            _uiState.update { it.copy(
                titleResId = getTitleResId(),
                searchHintResId = R.string.city_hint,
                items = getItems()
            )}

            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    fun onBack() {
        if (mode == 1) {
            mode = 0

            _uiState.update { it.copy(
                titleResId = getTitleResId(),
                searchHintResId = R.string.country_hint,
                items = getItems()
            )}
        }
        else navigator.popBackStack()
    }

    fun onSearchTextChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems()
        )}
    }

    private fun getTitleResId(): Int {
        return if (mode == 0) R.string.choose_country
        else R.string.choose_city
    }

    private fun getItems(): List<LocationPickerItem> {
        return if (mode == 0) getCountries()
        else getCities()
    }

    private fun getCountries(): List<LocationPickerItem> {
        val countries = repo.getCountries().map { country ->
            LocationPickerItem(
                id = country.id,
                nameAr = country.nameAr,
                nameEn = country.nameEn
            )
        }

        return if (searchText.isEmpty()) countries
        else countries.filter { country ->
            country.nameAr.contains(searchText, ignoreCase = true) or
                    country.nameEn.contains(searchText, ignoreCase = true) }
    }

    private fun getCities(): List<LocationPickerItem> {
        val cities = repo.getCities(countryId).map { city ->
            LocationPickerItem(
                id = city.id,
                nameAr = city.nameAr,
                nameEn = city.nameEn
            )
        }

        return if (searchText.isEmpty()) cities
        else cities.filter { city ->
            city.nameAr.contains(searchText, ignoreCase = true) or
                    city.nameEn.contains(searchText, ignoreCase = true)
        }
    }

}