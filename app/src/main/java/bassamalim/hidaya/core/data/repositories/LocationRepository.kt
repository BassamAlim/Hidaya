package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.UserPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.CitiesDao
import bassamalim.hidaya.core.data.dataSources.room.daos.CountriesDao
import bassamalim.hidaya.core.data.dataSources.room.entities.Country
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Coordinates
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.core.models.LocationIds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val countriesDao: CountriesDao,
    private val citiesDao: CitiesDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) {

    fun getLocation() = userPreferencesDataSource.getLocation()

    suspend fun setLocation(location: android.location.Location) {
        scope.launch {
            val closestCity = getClosestCity(
                Coordinates(latitude = location.latitude, longitude = location.longitude)
            )
            userPreferencesDataSource.updateLocation(
                location = Location(
                    type = LocationType.AUTO,
                    coordinates = Coordinates(
                        latitude = location.latitude,
                        longitude = location.longitude
                    ),
                    ids = LocationIds(countryId = closestCity.countryId, cityId = closestCity.id)
                )
            )
        }
    }

    suspend fun setLocation(countryId: Int, cityId: Int) {
        scope.launch {
            val city = getCity(cityId)

            userPreferencesDataSource.updateLocation(
                location = Location(
                    type = LocationType.MANUAL,
                    coordinates = Coordinates(latitude = city.latitude, longitude = city.longitude),
                    ids = LocationIds(countryId = countryId, cityId = cityId)
                )
            )
        }
    }

    suspend fun getTimeZone(cityId: Int) = withContext(dispatcher) {
        citiesDao.getCity(cityId).timeZone
    }

    suspend fun getCountries(language: Language) = withContext(dispatcher) {
        countriesDao.getAll().sortedBy { country: Country ->
            when (language) {
                Language.ARABIC -> country.nameAr
                Language.ENGLISH -> country.nameEn
            }
        }
    }

    suspend fun getCities(countryId: Int, language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> citiesDao.getTopAr(countryId, "").toList()
            Language.ENGLISH -> citiesDao.getTopEn(countryId, "").toList()
        }
    }

    suspend fun getCity(cityId: Int) = withContext(dispatcher) {
        citiesDao.getCity(cityId)
    }

    private suspend fun getClosestCity(coordinates: Coordinates) = withContext(dispatcher) {
        citiesDao.getClosest(latitude = coordinates.latitude, longitude = coordinates.longitude)
    }

    suspend fun getCountryName(countryId: Int, language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> countriesDao.getNameAr(countryId)
            Language.ENGLISH -> countriesDao.getNameEn(countryId)
        }
    }

    suspend fun getCityName(cityId: Int, language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> citiesDao.getCity(cityId).nameAr
            Language.ENGLISH -> citiesDao.getCity(cityId).nameEn
        }
    }

}