package bassamalim.hidaya.features.locationPicker.domain

import bassamalim.hidaya.features.locationPicker.data.LocationPickerRepository
import javax.inject.Inject

class LocationPickerDomain @Inject constructor(
    private val repository: LocationPickerRepository
) {

    private var countryId = -1

    fun setCountryId(id: Int) { countryId = id }

    suspend fun getLanguage() = repository.getLanguage()

    suspend fun getCountries() = repository.getCountries()

    suspend fun getCities() = repository.getCities(countryId)

    fun getCity(cityId: Int) = repository.getCity(cityId)

    suspend fun setLocation(cityId: Int) =
        repository.setLocation(countryId, cityId)

}