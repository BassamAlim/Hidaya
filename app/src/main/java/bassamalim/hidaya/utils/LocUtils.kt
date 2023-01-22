package bassamalim.hidaya.utils

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.Prefs
import com.google.gson.Gson

object LocUtils {

    fun storeLocation(pref: SharedPreferences, latitude: Float, longitude: Float) {
        val location = Location("").apply {
            this.latitude = latitude.toDouble()
            this.longitude = longitude.toDouble()
        }

        val json = Gson().toJson(location)

        pref.edit()
            .putString(Prefs.StoredLocation.key, json)
            .apply()
    }

    fun storeLocation(pref: SharedPreferences, latitude: Double, longitude: Double) {
        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        val json = Gson().toJson(location)

        pref.edit()
            .putString(Prefs.StoredLocation.key, json)
            .apply()
    }

    fun storeLocation(pref: SharedPreferences, location: Location?) {
        val json = Gson().toJson(location)

        pref.edit()
            .putString(Prefs.StoredLocation.key, json)
            .apply()
    }

    fun retrieveLocation(pref: SharedPreferences): Location? {
        val json = PrefUtils.getString(pref, Prefs.StoredLocation)

        return Gson().fromJson(json, Location::class.java)
    }

}