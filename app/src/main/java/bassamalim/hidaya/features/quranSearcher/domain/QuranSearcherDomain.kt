package bassamalim.hidaya.features.quranSearcher.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuranSearcherDomain @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val quranRepository: QuranRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getAllVerses() = quranRepository.getAllVerses()

    fun getSuraNames(language: Language) = quranRepository.getSuraNames(language)

    fun getMaxMatches() = quranRepository.getSearchMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        quranRepository.setSearchMaxMatches(value)
    }

}