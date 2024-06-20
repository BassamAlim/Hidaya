package bassamalim.hidaya.features.locator

import android.location.Location
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.utils.LocUtils
import javax.inject.Inject

class LocatorRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getLanguage() = preferencesDS.getString(Preference.Language)

    fun setLocationType(type: LocationType) {
        preferencesDS.setString(Preference.LocationType, type.name)
    }

    fun storeLocation(location: Location) {
        val closestCity = db.cityDao().getClosest(location.latitude, location.longitude)

        preferencesDS.setInt(Preference.CountryID, closestCity.countryId)
        preferencesDS.setInt(Preference.CityID, closestCity.id)

        LocUtils.storeLocation(
            location = location,
            locationPreferenceSetter = {
                json -> preferencesDS.setString(Preference.StoredLocation, json)
            }
        )
    }

}