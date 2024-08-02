package bassamalim.hidaya.features.locationPicker.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationPickerDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val userRepository: UserRepository
) {

    private var countryId = -1

    fun setCountryId(id: Int) { countryId = id }

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun getCountries() = userRepository.getCountries()

    suspend fun getCities() = userRepository.getCities(countryId)

    fun getCity(cityId: Int) = userRepository.getCity(cityId)

    suspend fun setLocation(cityId: Int) =
        userRepository.setLocation(countryId, cityId)

}