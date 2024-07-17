package bassamalim.hidaya.features.locator.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.features.locator.domain.LocatorDomain
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocatorViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val domain: LocatorDomain,
    private val navigator: Navigator
): AndroidViewModel(app) {

    private val type = savedStateHandle.get<String>("type") ?: "normal"

    private lateinit var locationRequestLauncher:
            ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    private val _uiState = MutableStateFlow(LocatorUiState(
        shouldShowSkipLocationButton = type == "initial"
    ))
    val uiState = _uiState.asStateFlow()

    fun provide(
        locationRequestLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        this.locationRequestLauncher = locationRequestLauncher
    }

    fun onLocateClick() {
        if (granted()) {
            locate()
            background()
        }
        else {
            locationRequestLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    fun onSelectLocationClick() {
        navigator.navigateForResult(
            Screen.LocationPicker
        ) { result ->
            if (result != null) {
                launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        result.getParcelable("location", Location::class.java)
                    else
                        result.getParcelable("location")
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(app)
            .lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModelScope.launch {
                        domain.setLocation(
                            type = LocationType.Auto,
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    }
                }

                launch(location)
            }

        background()
    }

    private fun launch(location: Location?) {
        if (type == "initial") {
            navigator.navigate(Screen.Main) {
                popUpTo(Screen.Locator(type = "{type}").route) {
                    inclusive = true
                }
            }
        }
        else if (type == "normal") {
            navigator.navigateBackWithResult(
                Bundle().apply {
                    putParcelable("location", location)
                }
            )
        }
    }

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(
                shouldShowAllowLocationToast = true
            )}

            locationRequestLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        }
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            app, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    app, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun onSkipLocationClick() {
        launch(null)
    }

    fun onLocationRequestResult(result: Map<String, Boolean>) {
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            return

        val fineLoc = result[Manifest.permission.ACCESS_FINE_LOCATION]
        val coarseLoc = result[Manifest.permission.ACCESS_COARSE_LOCATION]
        if (fineLoc != null && fineLoc && coarseLoc != null && coarseLoc) {
            locate()
            background()
        }
        else launch(null)
    }

}