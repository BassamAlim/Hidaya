package bassamalim.hidaya.features.locationPicker

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class LocationPickerRepo @Inject constructor(
    val pref: SharedPreferences,
    val db: AppDatabase
) {

    val language = Language.valueOf(PrefUtils.getString(pref, Prefs.Language))

    fun getCountries(): List<CountryDB> {
        return db.countryDao().getAll().sortedBy { countryDB: CountryDB ->
            if (language == Language.ENGLISH) countryDB.nameEn
            else countryDB.nameAr
        }
    }

    fun getCities(countryId: Int): List<CityDB> {
        return if (language == Language.ENGLISH) db.cityDao().getTopEn(countryId, "").toList()
        else db.cityDao().getTopAr(countryId, "").toList()
    }

    fun getCity(cityId: Int) = db.cityDao().getCity(cityId)

    fun storeLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        pref.edit()
            .putInt(Prefs.CountryID.key, countryId)
            .putInt(Prefs.CityID.key, cityId)
            .apply()

        LocUtils.storeLocation(pref, city.latitude, city.longitude)
    }

    fun setLocationType(type: LocationType) {
        pref.edit()
            .putString(Prefs.LocationType.key, type.name)
            .apply()
    }

}