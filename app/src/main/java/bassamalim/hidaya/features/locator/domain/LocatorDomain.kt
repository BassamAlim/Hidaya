package bassamalim.hidaya.features.locator.domain

import android.location.Location
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocatorDomain @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val locationRepository: LocationRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun setAutoLocation(location: Location) {
        locationRepository.setLocation(location)
    }

    suspend fun setManualLocation(cityId: Int) {
        val city = locationRepository.getCity(cityId)
        locationRepository.setLocation(
            countryId = city.countryId,
            cityId = city.id
        )
    }

}