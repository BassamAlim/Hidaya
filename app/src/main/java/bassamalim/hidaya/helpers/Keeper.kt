package bassamalim.hidaya.helpers

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.models.MyLocation
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson

class Keeper(
    private var pref: SharedPreferences,
    gLocation: Location? = null
) {

    private var gson = Gson()
    private lateinit var locJson: String

    init {
        if (gLocation != null) storeLocation(gLocation)
    }

    /**
     * Store the location in the shared preferences
     *
     * @param gLocation The Location object that you want to store.
     */
    private fun storeLocation(gLocation: Location?) {
        val loc = MyLocation(gLocation!!)
        locJson = gson.toJson(loc)

        pref.edit()
            .putString("stored location", locJson)
            .apply()
    }

    /**
     * Retrieve the stored location from the shared preferences
     *
     * @return The location object.
     */
    fun retrieveLocation(): Location? {
        locJson = PrefUtils.getString(pref, Prefs.StoredLocation)
        val myLocation = gson.fromJson(locJson, MyLocation::class.java)
        return if (myLocation == null) null
        else MyLocation.toLocation(myLocation)
    }

}