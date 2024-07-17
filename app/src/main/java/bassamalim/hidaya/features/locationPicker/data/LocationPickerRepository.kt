package bassamalim.hidaya.features.locationPicker.data

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.CityDB
import bassamalim.hidaya.core.data.database.dbs.CountryDB
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.data.preferences.repositories.UserPreferencesRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationPickerRepository @Inject constructor(
    private val db: AppDatabase,
    private val appSettingsPrefsRepo: AppSettingsPreferencesRepository,
    private val userPrefsRepo: UserPreferencesRepository
) {

    suspend fun getLanguage() = appSettingsPrefsRepo.getLanguage().first()

    suspend fun getCountries(): List<CountryDB> {
        val language = getLanguage()
        return db.countryDao().getAll().sortedBy { countryDB: CountryDB ->
            if (language == Language.ENGLISH) countryDB.nameEn
            else countryDB.nameAr
        }
    }

    suspend fun getCities(countryId: Int): List<CityDB> {
        val language = getLanguage()
        return if (language == Language.ENGLISH)
            db.cityDao().getTopEn(countryId, "").toList()
        else
            db.cityDao().getTopAr(countryId, "").toList()
    }

    fun getCity(cityId: Int) = db.cityDao().getCity(cityId)

    suspend fun setLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        userPrefsRepo.update { it.copy(
            location = Location(
                type = LocationType.Manual,
                latitude = city.latitude,
                longitude = city.longitude,
                countryId = countryId,
                cityId = cityId
            )
        )}
    }

}