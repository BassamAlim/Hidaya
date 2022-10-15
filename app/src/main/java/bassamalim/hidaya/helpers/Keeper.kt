package bassamalim.hidaya.helpers

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.preference.PreferenceManager
import bassamalim.hidaya.models.MyLocation
import com.google.gson.Gson

class Keeper {

    private val context: Context
    private var gson = Gson()
    private var pref: SharedPreferences
    private lateinit var locJson: String

    constructor(context: Context) {
        this.context = context
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    constructor(context: Context, gLocation: Location) {
        this.context = context
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        storeLocation(gLocation)
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
        locJson = pref.getString("stored location", "")!!
        val myLocation: MyLocation? = gson.fromJson(locJson, MyLocation::class.java)
        return if (myLocation == null) null
        else MyLocation.toLocation(myLocation)
    }

}