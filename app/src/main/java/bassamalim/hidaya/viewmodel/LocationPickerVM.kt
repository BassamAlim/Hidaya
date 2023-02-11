package bassamalim.hidaya.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.Screen
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
    private val repository: LocationPickerRepo
): ViewModel() {

    private var mode = 0
    private var countryId = -1
    val language = repository.language
    var searchText by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow(LocationPickerState(
        titleResId = R.string.choose_country,
        items = getItems()
    ))
    val uiState = _uiState.asStateFlow()

    private fun getItems(): List<LocationPickerItem> {
        val countries = repository.getCountries().map { country ->
            LocationPickerItem(
                id = country.id,
                nameAr = country.nameAr,
                nameEn = country.nameEn
            )
        }

        return if (searchText.isEmpty()) countries
        else countries.filter { country ->
            country.nameAr.contains(searchText, ignoreCase = true) or
                    country.nameEn.contains(searchText, ignoreCase = true)
        }
    }

    fun onBack(navController: NavController) {
        if (mode == 1) {
            mode = 0

            _uiState.update { it.copy(
                titleResId = R.string.choose_country,
                items = repository.getCountries().map { country ->
                    LocationPickerItem(
                        id = country.id,
                        nameAr = country.nameAr,
                        nameEn = country.nameEn
                    )
                }
            )}
        }
        else navController.popBackStack()
    }

    fun onSelect(id: Int, navController: NavController) {
        if (mode == 1) {
            repository.storeLocation(countryId, id)

            navController.navigate(Screen.Main.route)
        }
        else {
            countryId = id

            mode = 1

            searchText = ""

            _uiState.update { it.copy(
                titleResId = R.string.choose_city,
                items = repository.getCities(id).map { city ->
                    LocationPickerItem(
                        id = city.id,
                        nameAr = city.nameAr,
                        nameEn = city.nameEn
                    )
                }
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