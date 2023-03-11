package bassamalim.hidaya.features.locationPicker

import android.content.SharedPreferences
import android.location.Location
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class LocationPickerRepo @Inject constructor(
    val pref: SharedPreferences,
    val db: AppDatabase
) {

    val language = Language.valueOf(PrefUtils.getString(pref, bassamalim.hidaya.core.data.Prefs.Language))

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

    fun storeLocation(countryId: Int, cityId: Int) {
        val city = db.cityDao().getCity(cityId)

        pref.edit()
            .putInt(bassamalim.hidaya.core.data.Prefs.CountryID.key, countryId)
            .putInt(bassamalim.hidaya.core.data.Prefs.CityID.key, cityId)
            .apply()

        val location = Location("")
        location.latitude = city.latitude
        location.longitude = city.longitude

        LocUtils.storeLocation(pref, city.latitude, city.longitude)
    }

}