package bassamalim.hidaya.features.quran.quranMenu.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Sura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuranMenuDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    fun getBookmark() = quranRepository.getPageBookmark()

    fun getAllSuras(language: Language) = quranRepository.observeAllSuras().map {
        it.map { sura ->
            Sura(
                id = sura.id,
                decoratedName =
                    if (language == Language.ARABIC)sura.decoratedNameAr
                    else sura.decoratedNameEn,
                plainName = sura.plainNameAr,
//                    if (language == Language.ARABIC) sura.plainNameAr
//                    else sura.plainNameEn,
                revelation = sura.revelation,
                isFavorite = sura.isFavorite == 1
            )
        }
    }

    fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getFavs() = quranRepository.getSuraFavorites()

    suspend fun setFav(suraId: Int, fav: Boolean) {
        quranRepository.setSuraFavoriteStatus(suraId, fav)
    }

    fun getShouldShowTutorial() = quranRepository.getShouldShowReaderTutorial()

    suspend fun setDoNotShowTutorialAgain() {
        quranRepository.setShouldShowMenuTutorial(false)
    }

}