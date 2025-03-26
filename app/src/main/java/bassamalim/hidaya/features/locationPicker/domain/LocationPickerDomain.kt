package bassamalim.hidaya.features.locationPicker.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.LocationRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.Searcher
import bassamalim.hidaya.features.locationPicker.ui.LocationPickerItem
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LocationPickerDomain @Inject constructor(
    private val locationRepository: LocationRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    val searcher = Searcher<LocationPickerItem>()
    private var countryId = -1

    fun setCountryId(id: Int) { countryId = id }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getCountries(language: Language) =
        locationRepository.getCountries(language = language)

    suspend fun getCities(language: Language) =
        locationRepository.getCities(
            countryId = countryId,
            language = language
        )

    fun getSearchResults(query: String, items: List<LocationPickerItem>): List<LocationPickerItem> {
        return searcher.containsSearch(
            items = items,
            query = query,
            keySelector = { it.name }
        )
    }

}