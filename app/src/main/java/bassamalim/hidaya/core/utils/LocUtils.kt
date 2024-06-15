package bassamalim.hidaya.core.utils

import android.location.Location
import bassamalim.hidaya.core.models.MyLocation
import com.google.gson.Gson

object LocUtils {

    fun storeLocation(
        latitude: Double,
        longitude: Double,
        locationPreferenceSetter: (String) -> Unit
    ) {
        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        val myLoc = MyLocation(location)
        val json = Gson().toJson(myLoc)

        locationPreferenceSetter(json)
    }

    fun storeLocation(
        location: Location?,
        locationPreferenceSetter: (String) -> Unit) {
        val myLoc = MyLocation(location!!)
        val json = Gson().toJson(myLoc)

        locationPreferenceSetter(json)
    }

    fun retrieveLocation(storedLocation: String): Location? {
        return if (storedLocation == "{}") null
        else {
            val myLoc = Gson().fromJson(storedLocation, MyLocation::class.java)
            MyLocation.toLocation(myLoc)
        }
    }

}