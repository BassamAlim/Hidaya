package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.models.LocationPickerItem
import bassamalim.hidaya.repository.LocationPickerRepo
import bassamalim.hidaya.state.LocationPickerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LocationPickerVM @Inject constructor(
    private val repo: LocationPickerRepo
): ViewModel() {

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
                    city.nameEn.contains(searchText, ignoreCase = true) }
    }

    fun onBack(navController: NavController) {
        if (mode == 1) {
            mode = 0

            _uiState.update { it.copy(
                titleResId = getTitleResId(),
                searchHintResId = R.string.country_hint,
                items = getItems()
            )}
        }
        else navController.popBackStack()
    }

    fun onSelect(id: Int, nc: NavController) {
        if (mode == 1) {
            repo.storeLocation(countryId, id)

            nc.navigate(Screen.Main.route) {
                popUpTo(Screen.LocationPicker.route) {
                    inclusive = true
                }
            }
            nc.backQueue.removeAt(nc.backQueue.size - 2)  // pop Locator from the back stack
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
        }
    }

    fun onSearchTextChange(text: String) {
        searchText = text

        _uiState.update { it.copy(
            items = getItems()
        )}
    }

}