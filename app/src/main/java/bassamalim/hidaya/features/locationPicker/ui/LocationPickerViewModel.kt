package bassamalim.hidaya.features.locationPicker.ui

import android.os.Bundle
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.features.locationPicker.domain.LocationPickerDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
    val uiState = _uiState.onStart {
        initializeData()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = LocationPickerUiState()
    )

    private fun initializeData() {
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
            navigator.navigateBackWithResult(
                Bundle().apply {
                    putInt("city_id", id)
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

            viewModelScope.launch {
                fillItems()
            }
        }
        else navigator.popBackStack()
    }

    fun onSearchTextChange(text: String) {
        _uiState.update { it.copy(
            searchText = text,
        )}

        viewModelScope.launch {
            fillItems()
        }
    }

    private suspend fun fillItems() {
        when (_uiState.value.mode) {
            LocationPickerMode.COUNTRY -> fillWithCountries()
            LocationPickerMode.CITY -> fillWithCities()
        }
    }

    private suspend fun fillWithCountries() {
        val countries = domain.getCountries(language = language).map { country ->
            LocationPickerItem(
                id = country.id,
                name = when (language) {
                    Language.ARABIC -> country.nameAr
                    Language.ENGLISH -> country.nameEn
                }
            )
        }

        _uiState.update { it.copy(
            items = if (_uiState.value.searchText.isEmpty()) countries
            else domain.getSearchResults(
                query = _uiState.value.searchText,
                items = countries
            )
        )}
    }

    private fun fillWithCities() {
        viewModelScope.launch {
            val cities = domain.getCities(language = language).map { city ->
                LocationPickerItem(
                    id = city.id,
                    name = when (language) {
                        Language.ARABIC -> city.nameAr
                        Language.ENGLISH -> city.nameEn
                    }
                )
            }

            _uiState.update { it.copy(
                items = if (_uiState.value.searchText.isEmpty()) cities
                else domain.getSearchResults(
                    query = _uiState.value.searchText,
                    items = cities
                )
            )}
        }
    }

}