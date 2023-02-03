package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enum.LocationType
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class SplashRepo @Inject constructor(
    val pref: SharedPreferences,
    private val db: AppDatabase
) {

    fun getIsFirstTime() = PrefUtils.getBoolean(pref, Prefs.FirstTime)

    fun getLocationType(): LocationType {
        return LocationType.valueOf(
            PrefUtils.getString(pref, Prefs.LocationType)
        )
    }

    fun setLocationType(type: LocationType) {
        pref.edit()
            .putString(Prefs.LocationType.key, type.name)
            .apply()
    }

    fun storeLocation(location: Location) {
        val closestCity = db.cityDao().getClosest(location.latitude, location.longitude)

        pref.edit()
            .putInt(Prefs.CountryID.key, closestCity.countryId)
            .putInt(Prefs.CityID.key, closestCity.id)
            .apply()

        LocUtils.storeLocation(pref, location)
    }

}