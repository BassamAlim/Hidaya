package bassamalim.hidaya.features.locator.domain

import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.models.Location
import bassamalim.hidaya.features.locator.data.LocatorRepository
import javax.inject.Inject

class LocatorDomain @Inject constructor(
    private val repository: LocatorRepository
) {

    suspend fun getLanguage() = repository.getLanguage()

    suspend fun setLocation(
        type: LocationType,
        latitude: Double,
        longitude: Double
    ) {
        val closestCity = repository.getClosestCity(latitude, longitude)
        repository.setLocation(
            Location(
                type = type,
                latitude = latitude,
                longitude = longitude,
                countryId = closestCity.countryId,
                cityId = closestCity.id
            )
        )
    }

}