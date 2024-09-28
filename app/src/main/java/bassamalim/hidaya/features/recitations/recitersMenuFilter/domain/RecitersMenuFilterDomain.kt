package bassamalim.hidaya.features.recitations.recitersMenuFilter.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RecitersMenuFilterDomain @Inject constructor(
    private val recitationsRepository: RecitationsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getOptions(language: Language) =
        recitationsRepository.getNarrationSelections(language)

    suspend fun setOptions(options: Map<String, Boolean>) {
        recitationsRepository.setNarrationSelections(options)
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

}