package bassamalim.hidaya.helpers

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.models.MyLocation
import bassamalim.hidaya.utils.PTUtils
import com.google.gson.Gson
import java.util.*

class Keeper {

    private val context: Context
    private var gson = Gson()
    private var pref: SharedPreferences
    private lateinit var lJson: String
    private lateinit var tJson: String

    constructor(context: Context) {
        this.context = context
        pref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    constructor(context: Context, gLocation: Location) {
        this.context = context
        pref = PreferenceManager.getDefaultSharedPreferences(context)

        storeLocation(gLocation)
        storeTimes(PTUtils.getTimes(context, gLocation))
    }

    /**
     * Store the location in the shared preferences
     *
     * @param gLocation The Location object that you want to store.
     */
    private fun storeLocation(gLocation: Location?) {
        val loc = MyLocation(gLocation!!)
        val editor: SharedPreferences.Editor = pref.edit()

        lJson = gson.toJson(loc)
        editor.putString("stored location", lJson)
        editor.apply()
    }

    /**
     * Stores the times in the array times in the shared preferences
     *
     * @param times The array of Calendar objects that you want to store.
     */
    private fun storeTimes(times: Array<Calendar?>) {
        val editor: SharedPreferences.Editor = pref.edit()
        tJson = gson.toJson(times)
        editor.putString("stored times", tJson)
        editor.apply()
    }

    /**
     * Retrieve the stored location from the shared preferences
     *
     * @return The location object.
     */
    fun retrieveLocation(): Location? {
        lJson = pref.getString("stored location", "")!!
        val myLocation: MyLocation? = gson.fromJson(lJson, MyLocation::class.java)
        return if (myLocation == null) null
        else MyLocation.toLocation(myLocation)
    }

    /**
     * Retrieves the stored times from the shared preferences
     *
     * @return An array of Calendar objects.
     */
    fun retrieveTimes(): Array<Calendar?>? {
        tJson = pref.getString("stored times", "")!!
        return gson.fromJson(tJson, Array<Calendar?>::class.java)
    }

    /**
     * Given a list of strings, reformat the strings into a list of strings, where each string is a
     * time
     *
     * @param givenTimes an ArrayList of the times in the given prayer times
     * @return An array of strings.
     */
    private fun reformatTimes(givenTimes: ArrayList<String>): Array<String?> {
        val arr = arrayOfNulls<String>(givenTimes.size - 1)
        val prayerNames = context.resources.getStringArray(R.array.prayer_names)

        var index = 0
        for (i in 0..4) {
            arr[i] = """
                ${prayerNames[index]}
                ${givenTimes[index]}
                """.trimIndent()

            if (++index == 1) index = 2
        }

        return arr
    }

}