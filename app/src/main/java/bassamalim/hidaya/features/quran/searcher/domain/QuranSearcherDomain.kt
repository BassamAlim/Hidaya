package bassamalim.hidaya.features.quran.searcher.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuranSearcherDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun getLanguage() = appSettingsRepository.getLanguage()

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

    suspend fun getAllVerses() = quranRepository.getAllVerses()

    suspend fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getMaxMatches() = quranRepository.getSearchMaxMatches()

    suspend fun setMaxMatches(value: Int) {
        quranRepository.setSearchMaxMatches(value)
    }

}