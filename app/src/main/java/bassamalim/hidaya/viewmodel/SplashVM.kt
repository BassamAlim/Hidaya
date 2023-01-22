package bassamalim.hidaya.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen
import bassamalim.hidaya.enum.LocationType
import bassamalim.hidaya.repository.SplashRepo
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.state.SplashState
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject constructor(
    app: Application,
    private val repository: SplashRepo
): AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(SplashState())
    val uiState = _uiState.asStateFlow()

    private val context = app.applicationContext
    private lateinit var navController: NavController
    private lateinit var locationRequestLauncher:
            ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    init {
        // stop athan if it is running
        context.stopService(Intent(context, AthanService::class.java))

        DBUtils.testDB(context, repository.pref)

        ActivityUtils.onActivityCreateSetTheme(context)
        ActivityUtils.onActivityCreateSetLocale(context as Activity)
    }

    fun provide(
        navController: NavController,
        locationRequestLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    ) {
        this.navController = navController
        this.locationRequestLauncher = locationRequestLauncher
    }

    private fun getLocationAndLaunch() {
        if (repository.getLocationType() == LocationType.Auto) {
            if (granted()) locate()
            else {
                locationRequestLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
        else launch(null)
    }

    fun enter() {
        if (repository.getIsFirstTime()) welcome()
        else getLocationAndLaunch()
    }

    private fun welcome() {
        navController.navigate(Screen.Welcome.route) {
            popUpTo(Screen.Welcome.route) {
                inclusive = true
            }
        }
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(context)
            .lastLocation.addOnSuccessListener { location: Location? ->
                launch(location)
            }

        background()
    }

    private fun launch(location: Location?) {
        if (location != null) {
            repository.storeLocation(location)
        }

        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) {
                inclusive = true
            }
        }
    }

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(
                showAllowLocationToastShown = true
            )}

            locationRequestLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        }
    }

    fun onLocationRequestResult(result: Map<String, Boolean>) {
        if (result.keys.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            return

        if (result[Manifest.permission.ACCESS_FINE_LOCATION]!! &&
            result[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
            repository.setLocationType(LocationType.Auto)

            locate()

            background()
        }
        else launch(null)
    }

}