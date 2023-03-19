package bassamalim.hidaya.features.locator

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class LocatorRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getString(pref, Prefs.Language)

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