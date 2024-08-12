package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.CityDao
import bassamalim.hidaya.core.data.database.daos.CountryDao
import bassamalim.hidaya.core.data.database.models.Country
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val countryDao: CountryDao,
    private val cityDao: CityDao
) {

    fun getLocation() = userPreferencesDataSource.flow.map {
        it.location
    }
    suspend fun setLocation(location: Location) {
        userPreferencesDataSource.update { it.copy(
            location = location
        )}
    }

    fun getTimeZone(cityId: Int) = cityDao.getCity(cityId).timeZone

    fun getCountries(language: Language) =
        countryDao.getAll().sortedBy { country: Country ->
            if (language == Language.ENGLISH) country.nameEn
            else country.nameAr
        }

    fun getCities(countryId: Int, language: Language) =
        if (language == Language.ENGLISH)
            cityDao.getTopEn(countryId, "").toList()
        else
            cityDao.getTopAr(countryId, "").toList()

    fun getCity(cityId: Int) = cityDao.getCity(cityId)

    suspend fun setLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        userPreferencesDataSource.update { it.copy(
            location = Location(
                type = LocationType.MANUAL,
                latitude = city.latitude,
                longitude = city.longitude,
                countryId = countryId,
                cityId = cityId
            )
        )}
    }

    fun getClosestCity(latitude: Double, longitude: Double) =
        cityDao.getClosest(latitude, longitude)

    fun getCountryName(countryId: Int, language: Language): String =
        if (language == Language.ENGLISH) countryDao.getNameEn(countryId)
        else countryDao.getNameAr(countryId)

    fun getCityName(cityId: Int, language: Language): String =
        if (language == Language.ENGLISH) cityDao.getCity(cityId).nameEn
        else cityDao.getCity(cityId).nameAr

}