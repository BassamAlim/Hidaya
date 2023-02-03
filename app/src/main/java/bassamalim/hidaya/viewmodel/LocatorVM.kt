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
import bassamalim.hidaya.Screen
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
    private val repository: LocatorRepo,
    savedStateHandle: SavedStateHandle
): AndroidViewModel(app) {

    private val type = savedStateHandle.get<String>("type") ?: "normal"

    private val _uiState = MutableStateFlow(LocatorState(
        showSkipLocationBtn = type == "initial"
    ))
    val uiState = _uiState.asStateFlow()

    private lateinit var navController: NavController
    private lateinit var locationRequestLauncher:
            ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    fun provide(
        navController: NavController,
        locationRequestLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        this.navController = navController
        this.locationRequestLauncher = locationRequestLauncher
    }

    private fun granted(): Boolean {
        val ctx = app.applicationContext
        return ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    ctx, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(app.applicationContext)
            .lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) repository.storeLocation(location)

                launch()
            }

        background()
    }

    private fun launch() {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) {
                inclusive = true
            }
        }
    }

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                app.applicationContext,
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

    fun onLocateClick() {
        repository.setLocationType(LocationType.Auto)

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

    fun onChooseLocationClick() {
        repository.setLocationType(LocationType.Manual)

        navController.navigate(Screen.LocationPicker.route)
    }

    fun onSkipLocationClick() {
        repository.setLocationType(LocationType.None)

        launch()
    }

    fun onLocationRequestResult(result: Map<String, Boolean>) {
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            return

        if (result[Manifest.permission.ACCESS_FINE_LOCATION]!! &&
            result[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
            locate()
            background()
        }
        else launch()
    }

}