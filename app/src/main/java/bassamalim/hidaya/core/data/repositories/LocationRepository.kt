package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.CitiesDao
import bassamalim.hidaya.core.data.database.daos.CountriesDao
import bassamalim.hidaya.core.data.database.models.Country
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val countriesDao: CountriesDao,
    private val citiesDao: CitiesDao
) {

    fun getLocation() = userPreferencesDataSource.flow.map {
        it.location
    }

    suspend fun setLocation(location: android.location.Location) {
        val closestCity = getClosestCity(location.latitude, location.longitude)
        userPreferencesDataSource.update { it.copy(
            location = Location(
                type = LocationType.AUTO,
                latitude = location.latitude,
                longitude = location.longitude,
                countryId = closestCity.countryId,
                cityId = closestCity.id
            )
        )}
    }

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

    fun getTimeZone(cityId: Int) = citiesDao.getCity(cityId).timeZone

    fun getCountries(language: Language) =
        countriesDao.getAll().sortedBy { country: Country ->
            if (language == Language.ENGLISH) country.nameEn
            else country.nameAr
        }

    fun getCities(countryId: Int, language: Language) =
        if (language == Language.ENGLISH)
            citiesDao.getTopEn(countryId, "").toList()
        else
            citiesDao.getTopAr(countryId, "").toList()

    fun getCity(cityId: Int) = citiesDao.getCity(cityId)

    fun getClosestCity(latitude: Double, longitude: Double) =
        citiesDao.getClosest(latitude, longitude)

    fun getCountryName(countryId: Int, language: Language): String =
        if (language == Language.ENGLISH) countriesDao.getNameEn(countryId)
        else countriesDao.getNameAr(countryId)

    fun getCityName(cityId: Int, language: Language): String =
        if (language == Language.ENGLISH) citiesDao.getCity(cityId).nameEn
        else citiesDao.getCity(cityId).nameAr

}