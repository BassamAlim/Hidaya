package bassamalim.hidaya.features.locationPicker

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.utils.LocUtils
import javax.inject.Inject

class LocationPickerRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    private val language = preferencesDS.getLanguage()
    fun getLanguage() = language

    fun getCountries(): List<CountryDB> =
        db.countryDao().getAll().sortedBy { countryDB: CountryDB ->
            if (language == Language.ENGLISH) countryDB.nameEn
            else countryDB.nameAr
        }

    fun getCities(countryId: Int): List<CityDB> =
        if (language == Language.ENGLISH) db.cityDao().getTopEn(countryId, "").toList()
        else db.cityDao().getTopAr(countryId, "").toList()

    fun getCity(cityId: Int) = db.cityDao().getCity(cityId)

    fun storeLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        preferencesDS.setInt(Preference.CountryID, countryId)
        preferencesDS.setInt(Preference.CityID, cityId)

        LocUtils.storeLocation(
            latitude = city.latitude,
            longitude = city.longitude,
            locationPreferenceSetter = { json ->
                preferencesDS.setString(Preference.StoredLocation, json)
            }
        )
    }

    fun setLocationType(type: LocationType) {
        preferencesDS.setString(Preference.LocationType, type.name)
    }

}