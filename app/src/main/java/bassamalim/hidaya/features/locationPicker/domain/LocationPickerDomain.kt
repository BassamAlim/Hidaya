package bassamalim.hidaya.features.locationPicker.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationPickerDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository,
    private val locationRepo: LocationRepository
) {

    private var countryId = -1

    fun setCountryId(id: Int) { countryId = id }

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    fun getCountries(language: Language) =
        locationRepo.getCountries(language = language)

    fun getCities(language: Language) =
        locationRepo.getCities(
            countryId = countryId,
            language = language
        )

    fun getCity(cityId: Int) = locationRepo.getCity(cityId)

    suspend fun setLocation(cityId: Int) =
        locationRepo.setLocation(countryId, cityId)

}