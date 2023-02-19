package bassamalim.hidaya.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.enums.LocationType
import bassamalim.hidaya.repository.LocatorRepo
import bassamalim.hidaya.state.LocatorState
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LocatorVM @Inject constructor(
    private val app: Application,
    private val repo: LocatorRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val type = savedStateHandle.get<String>("type") ?: "normal"

    private lateinit var nc: NavController
    private lateinit var locationRequestLauncher:
            ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    private val _uiState = MutableStateFlow(LocatorState(
        showSkipLocationBtn = type == "initial"
    ))
    val uiState = _uiState.asStateFlow()

    fun provide(
        navController: NavController,
        locationRequestLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        this.nc = navController
        this.locationRequestLauncher = locationRequestLauncher
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(app)
            .lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) repo.storeLocation(location)

                launch()
            }

        background()
    }

    private fun launch() {
        nc.navigate(Screen.Main.route) {
            popUpTo(Screen.Locator(type).route) {
                inclusive = true
            }
        }
    }

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                app,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            _uiState.update { it.copy(
                showAllowLocationToast = true
            )}

            locationRequestLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
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

    fun onLocateClk() {
        repo.setLocationType(LocationType.Auto)

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

    fun onChooseLocationClk() {
        repo.setLocationType(LocationType.Manual)

        nc.navigate(Screen.LocationPicker.route)
    }

    fun onSkipLocationClk() {
        repo.setLocationType(LocationType.None)

        launch()
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
        else launch()
    }

}