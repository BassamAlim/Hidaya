package bassamalim.hidaya.features.quran.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuranDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val appSettingsRepo: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

    fun getBookmark() = quranRepository.getPageBookmark()

    fun getAllSuar() = quranRepository.observeAllSuar()

    fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getFavs() = quranRepository.getSuraFavorites()

    suspend fun setFav(suraId: Int, fav: Int) {
        quranRepository.setSuraFavorites(suraId, fav)
    }

    fun getShouldShowTutorial() = quranRepository.getShouldShowReaderTutorial()

    suspend fun setDoNotShowTutorialAgain() {
        quranRepository.setShouldShowMenuTutorial(false)
    }

}