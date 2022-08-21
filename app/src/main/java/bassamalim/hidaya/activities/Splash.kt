package bassamalim.hidaya.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.android.gms.location.LocationServices

class Splash : AppCompatActivity() {

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }

        super.onCreate(savedInstanceState)

        stopService(Intent(this, AthanService::class.java))

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        DBUtils.testDB(this, pref)

        if (pref.getBoolean("new_user", true)) welcome()
        else ActivityUtils.onActivityCreateSetLocale(this)

        when(pref.getString("location_type", "auto")) {
            "auto" -> {
                if (granted()) locate()
                else
                    locationPermissionRequest.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            "manual" -> {
                val cityId = pref.getInt("city_id", -1)

                if (cityId == -1) launch(null)
                else {
                    val city = DBUtils.getDB(this).cityDao().getCity(cityId)

                    val location = Location("")
                    location.latitude = city.latitude
                    location.longitude = city.longitude
                    launch(location)
                }
            }
            "none" -> {
                launch(null)
            }
        }
    }

    private fun welcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener(this) { location: Location? -> launch(location) }

        background()
    }

    private fun launch(location: Location?) {
        var loc = location

        val intent = Intent(this, MainActivity::class.java)
        if (loc == null) {
            loc = Keeper(this).retrieveLocation()
            intent.putExtra("located", loc != null)
        }
        else intent.putExtra("located", true)

        intent.putExtra("location", loc)
        startActivity(intent)
        finish()
    }

    private val locationPermissionRequest = registerForActivityResult(
        RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION]!!
            && permissions[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
            background()
            locate()
        }
        else launch(null)
    }

    private val backgroundLocationPermissionRequest = registerForActivityResult(
        RequestMultiplePermissions()) {}

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this,
                getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG).show()

            backgroundLocationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }

}
