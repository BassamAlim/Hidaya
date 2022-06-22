package bassamalim.hidaya.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.other.Utils
import bassamalim.hidaya.services.AthanService
import com.google.android.gms.location.LocationServices

class Splash : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }

        super.onCreate(savedInstanceState)

        stopService(Intent(this, AthanService::class.java))

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("new_user", true))
            welcome()
        else Utils.onActivityCreateSetLocale(this)

        if (granted()) {
            getLocation()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                background()
        }
        else {
            if (Keeper(this).retrieveLocation() == null)
                requestMultiplePermissions.launch(permissions)
            else launch(null)
        }
    }

    private fun welcome() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun granted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this).lastLocation
                .addOnSuccessListener(this) { location: Location? -> launch(location) }
        }
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

    private val requestMultiplePermissions: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(RequestMultiplePermissions()
        ) { permissions: Map<String, Boolean> ->

            val collection = permissions.values
            val array = collection.toTypedArray()

            if (array[0] && array[1]) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    background()
                getLocation()
            }
            else launch(null)
        }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun background() {
        Toast.makeText(
            this, getString(R.string.choose_allow_all_the_time), Toast.LENGTH_LONG
        ).show()

        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 0
        )
    }

}
