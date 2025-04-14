package bassamalim.hidaya.features.locator.ui

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.helpers.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.locator.domain.LocatorDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocatorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val domain: LocatorDomain,
    private val navigator: Navigator
): ViewModel() {

    private val isInitialLocation = savedStateHandle.get<Boolean>("is_initial") == true

    private val _uiState = MutableStateFlow(LocatorUiState(
        shouldShowSkipLocationButton = isInitialLocation
    ))
    val uiState = _uiState.asStateFlow()

    fun provide(
        locationRequestLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
        snackbarHostState: SnackbarHostState,
        snackbarMessage: String
    ) {
        domain.setLocationRequestLauncher(locationRequestLauncher)
        domain.setShowBackgroundLocationPermissionNeeded {
            viewModelScope.launch {
                snackbarHostState.showSnackbar(snackbarMessage)
            }
        }
        domain.setLaunch(::launch)
    }

    fun onLocateClick() {
        viewModelScope.launch {
            domain.locate()
        }
    }

    fun onSelectLocationClick() {
        navigator.navigateForResult(Screen.LocationPicker) { result ->
            if (result != null) {
                val cityId = result.getInt("city_id")
                viewModelScope.launch {
                    domain.setManualLocation(cityId)
                }

                launch()
            }
        }
    }

    fun onSkipLocationClick() {
        launch()
    }

    fun onLocationRequestResult(result: Map<String, Boolean>) {
        viewModelScope.launch {
            domain.handleLocationRequestResult(result)
        }
    }

    private fun launch() {
        if (isInitialLocation) {
            navigator.navigate(Screen.Main) {
                popUpTo(Screen.Locator(isInitial = "{is_initial}").route) {
                    inclusive = true
                }
            }
        }
        else navigator.popBackStack()
    }

}