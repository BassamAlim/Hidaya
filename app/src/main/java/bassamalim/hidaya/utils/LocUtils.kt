package bassamalim.hidaya.utils

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.models.MyLocation
import com.google.gson.Gson

object LocUtils {

    fun storeLocation(sp: SharedPreferences, latitude: Double, longitude: Double) {
        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        val myLoc = MyLocation(location)
        val json = Gson().toJson(myLoc)

        sp.edit()
            .putString(Prefs.StoredLocation.key, json)
            .apply()
    }

    fun storeLocation(sp: SharedPreferences, location: Location?) {
        val myLoc = MyLocation(location!!)
        val json = Gson().toJson(myLoc)

        sp.edit()
            .putString(Prefs.StoredLocation.key, json)
            .apply()
    }

    fun retrieveLocation(sp: SharedPreferences): Location? {
        val json = PrefUtils.getString(sp, Prefs.StoredLocation)

        return if (json == "{}") null
        else {
            val myLoc = Gson().fromJson(json, MyLocation::class.java)
            MyLocation.toLocation(myLoc)
        }
    }

}