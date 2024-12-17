package bassamalim.hidaya.features.quran.surasMenu.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuranSurasDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getBookmarks() = quranRepository.getBookmarks()

    fun getAllSuras(language: Language) = quranRepository.observeAllSuras(language)

    suspend fun getAllVerses() = quranRepository.getAllVerses()

    suspend fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun setFav(suraId: Int, fav: Boolean) {
        quranRepository.setSuraFavoriteStatus(suraId, fav)
    }

    suspend fun getShouldShowTutorial() = quranRepository.getShouldShowMenuTutorial().first()

    fun setDoNotShowTutorialAgain() {
        quranRepository.setShouldShowMenuTutorial(false)
    }

}