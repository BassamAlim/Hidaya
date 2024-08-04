package bassamalim.hidaya.features.locator.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocatorDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val locationRepo: LocationRepository
) {

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun setAndReturnLocation(
        type: LocationType,
        latitude: Double,
        longitude: Double
    ): Location {
        val closestCity = locationRepo.getClosestCity(latitude, longitude)

        val location = Location(
            type = type,
            latitude = latitude,
            longitude = longitude,
            countryId = closestCity.countryId,
            cityId = closestCity.id
        )

        locationRepo.setLocation(location)

        return location
    }

}