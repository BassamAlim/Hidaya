package bassamalim.hidaya.utils

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.Prefs
import com.google.gson.Gson

object LocUtils {

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

        return if (json.isEmpty()) null
        else Gson().fromJson(json, Location::class.java)
    }

}