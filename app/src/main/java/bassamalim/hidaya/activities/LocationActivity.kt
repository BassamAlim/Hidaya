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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.helpers.LocationPicker
import bassamalim.hidaya.helpers.Keeper
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import com.google.android.gms.location.LocationServices

class LocationActivity: ComponentActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var db: AppDatabase
    private var action = "normal"
    private val locationPickerShown = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
        db = DBUtils.getDB(this)

        action = intent.action!!

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun getLocation() {
        if (granted()) {
            locate()
            background()
        }
        else {
            if (pref.getString("location_type", "auto") == "auto")
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            else launch(null)
        }
    }

    private fun granted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun locate() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                launch(location)

                if (location != null) {
                    val closestCity = db.cityDao().getClosest(location.latitude, location.longitude)

                    pref.edit()
                        .putInt("country_id", closestCity.countryId)
                        .putInt("city_id", closestCity.id)
                        .apply()

                    background()
                }
            }
    }

    private fun launch(location: Location?) {
        var loc = location

        val intent = Intent(this, MainActivity::class.java)
        if (loc == null) loc = Keeper(this).retrieveLocation()
        intent.putExtra("located", loc != null)
        intent.putExtra("location", loc)
        startActivity(intent)

        finish()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION]!! &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION]!!) {
            background()
            getLocation()
        }
        else launch(null)
    }

    private val backgroundLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    private fun background() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this, getString(R.string.choose_allow_all_the_time),
                Toast.LENGTH_LONG
            ).show()

            backgroundLocationPermissionRequest.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    @Composable
    fun UI() {
        Column(
            Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.disclaimer),
                fontSize = 26.nsp,
                modifier = Modifier.padding(horizontal = 15.dp)
            )

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyButton(
                    text = stringResource(R.string.locate),
                    fontSize = 22.sp,
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppTheme.colors.accent),
                    textColor = AppTheme.colors.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 30.dp)
                ) {
                    pref.edit()
                        .putString("location_type", "auto")
                        .apply()

                    getLocation()
                }

                MyButton(
                    text = stringResource(R.string.choose_manually),
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 30.dp)
                ) {
                    pref.edit()
                        .putString("location_type", "manual")
                        .apply()

                    locationPickerShown.value = true
                }

                if (action == "initial") {
                    MyButton(
                        text = stringResource(R.string.rejected),
                        fontSize = 22.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 30.dp)
                    ) {
                        pref.edit()
                            .putString("location_type", "none")
                            .apply()

                        val intent = Intent(
                            this@LocationActivity, MainActivity::class.java
                        )
                        intent.putExtra("located", false)
                        startActivity(intent)

                        finish()
                    }
                }
            }
        }

        if (locationPickerShown.value) {
            LocationPicker(
                this, pref, db, locationPickerShown
            ) { countryId, cityId ->
                pref.edit()
                    .putInt("country_id", countryId)
                    .putInt("city_id", cityId)
                    .apply()

                val city = db.cityDao().getCity(cityId)

                val location = Location("")
                location.latitude = city.latitude
                location.longitude = city.longitude

                launch(location)
            }.UI()
        }
    }
}
