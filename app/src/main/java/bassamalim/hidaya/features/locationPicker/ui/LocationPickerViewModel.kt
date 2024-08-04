package bassamalim.hidaya.features.locationPicker.ui

import android.os.Bundle
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.features.locationPicker.domain.LocationPickerDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationPickerViewModel @Inject constructor(
    private val domain: LocationPickerDomain,
    private val navigator: Navigator
): ViewModel() {

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var lazyListState: LazyListState
    private lateinit var language: Language

    private val _uiState = MutableStateFlow(LocationPickerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            language = domain.getLanguage()

            fillItems()
        }
    }

    fun onStart(coroutineScope: CoroutineScope, lazyListState: LazyListState) {
        this.coroutineScope = coroutineScope
        this.lazyListState = lazyListState
    }

    fun onSelect(id: Int) {
        if (_uiState.value.mode == LocationPickerMode.CITY) {
            viewModelScope.launch {
                domain.setLocation(cityId = id)
            }

            val city = domain.getCity(id)
            navigator.navigateBackWithResult(
                Bundle().apply {
                    putParcelable(
                        "location",
                        Location(
                            type = LocationType.MANUAL,
                            latitude = city.latitude,
                            longitude = city.longitude,
                            countryId = city.countryId,
                            cityId = city.id
                        )
                    )
                }
            )
        }
        else {
            domain.setCountryId(id)

            _uiState.update { it.copy(
                mode = LocationPickerMode.CITY,
                searchText = ""
            )}

            coroutineScope.launch {
                fillItems()

                lazyListState.animateScrollToItem(0)
            }
        }
    }

    fun onBack() {
        if (_uiState.value.mode == LocationPickerMode.CITY) {
            _uiState.update { it.copy(
                mode = LocationPickerMode.COUNTRY
            )}

            fillItems()
        }
        else navigator.popBackStack()
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text,
        )}

        fillItems()
    }

    private fun fillItems() {
        when (_uiState.value.mode) {
            LocationPickerMode.COUNTRY -> fillWithCountries()
            LocationPickerMode.CITY -> fillWithCities()
        }
    }

    private fun fillWithCountries() {
        viewModelScope.launch {
            val countries = domain.getCountries(language = language).map { country ->
                LocationPickerItem(
                    id = country.id,
                    name = if (language == Language.ARABIC) country.nameAr else country.nameEn
                )
            }

            val searchText = _uiState.value.searchText
            _uiState.update { it.copy(
                items = if (searchText.isEmpty()) countries
                else countries.filter { country ->
                    country.name.contains(searchText, ignoreCase = true)
                }
            )}
        }
    }

    private fun fillWithCities() {
        viewModelScope.launch {
            val cities = domain.getCities(language = language).map { city ->
                LocationPickerItem(
                    id = city.id,
                    name = if (language == Language.ARABIC) city.nameAr else city.nameEn
                )
            }

            val searchText = _uiState.value.searchText
            _uiState.update { it.copy(
                items = if (searchText.isEmpty()) cities
                else cities.filter { city ->
                    city.name.contains(searchText, ignoreCase = true)
                }
            )}
        }
    }

}