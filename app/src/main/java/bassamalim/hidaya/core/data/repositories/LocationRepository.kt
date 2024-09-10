package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.database.daos.CitiesDao
import bassamalim.hidaya.core.data.database.daos.CountriesDao
import bassamalim.hidaya.core.data.database.models.Country
import bassamalim.hidaya.core.data.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.LocationIds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val countriesDao: CountriesDao,
    private val citiesDao: CitiesDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    fun getLocation() = userPreferencesDataSource.flow.map {
        it.location
    }

    suspend fun setLocation(location: android.location.Location) {
        val closestCity = getClosestCity(
            Coordinates(
                latitude = location.latitude,
                longitude = location.longitude
            )
        )
        userPreferencesDataSource.update { it.copy(
            location = Location(
                type = LocationType.AUTO,
                coordinates = Coordinates(
                    latitude = location.latitude,
                    longitude = location.longitude
                ),
                ids = LocationIds(
                    countryId = closestCity.countryId,
                    cityId = closestCity.id
                )
            )
        )}
    }

    suspend fun setLocation(countryId: Int, cityId: Int) {
        val city = getCity(cityId)

        userPreferencesDataSource.update { it.copy(
            location = Location(
                type = LocationType.MANUAL,
                coordinates = Coordinates(
                    latitude = city.latitude,
                    longitude = city.longitude
                ),
                ids = LocationIds(
                    countryId = countryId,
                    cityId = cityId
                )
            )
        )}
    }

    suspend fun getTimeZone(cityId: Int) = withContext(dispatcher) {
        citiesDao.getCity(cityId).timeZone
    }

    suspend fun getCountries(language: Language) = withContext(dispatcher) {
        countriesDao.getAll().sortedBy { country: Country ->
            if (language == Language.ENGLISH) country.nameEn
            else country.nameAr
        }
    }

    suspend fun getCities(countryId: Int, language: Language) = withContext(dispatcher) {
        if (language == Language.ENGLISH)
            citiesDao.getTopEn(countryId, "").toList()
        else
            citiesDao.getTopAr(countryId, "").toList()
    }

    suspend fun getCity(cityId: Int) = withContext(dispatcher) {
        citiesDao.getCity(cityId)
    }

    private suspend fun getClosestCity(coordinates: Coordinates) = withContext(dispatcher) {
        citiesDao.getClosest(latitude = coordinates.latitude, longitude = coordinates.longitude)
    }

    suspend fun getCountryName(countryId: Int, language: Language) = withContext(dispatcher) {
        if (language == Language.ENGLISH) countriesDao.getNameEn(countryId)
        else countriesDao.getNameAr(countryId)
    }

    suspend fun getCityName(cityId: Int, language: Language) = withContext(dispatcher) {
        if (language == Language.ENGLISH) citiesDao.getCity(cityId).nameEn
        else citiesDao.getCity(cityId).nameAr
    }

}