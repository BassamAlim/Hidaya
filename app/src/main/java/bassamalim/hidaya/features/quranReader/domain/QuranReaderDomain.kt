package bassamalim.hidaya.features.quranReader.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.UserRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.QuranPageBookmark
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuranReaderDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val userRepository: UserRepository
) {

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getTheme() = appSettingsRepository.getTheme().first()

    fun getSuraPageNum(suraId: Int) = quranRepository.getSuraPageNum(suraId)

    fun getAyaPageNum(ayaId: Int) = quranRepository.getAyaPageNum(ayaId)

    fun getAyas() = quranRepository.getAyas()

    fun getSuraNames(language: Language) = quranRepository.getSuraNames(language)

    fun getViewType() = quranRepository.getViewType()

    fun getShouldShowTutorial() = quranRepository.getShouldShowReaderTutorial()

    fun getTextSize() = quranRepository.getTextSize()

    fun getPageBookmark() = quranRepository.getPageBookmark()

    suspend fun setBookmarkedPage(pageNum: Int, suraNum: Int) {
        quranRepository.setPageBookmark(
            QuranPageBookmark(
                pageNum = pageNum,
                suraId = suraNum
            )
        )
    }

    fun getPagesRecord() = userRepository.getLocalRecord().map { 
        it.quranPages
    }

    suspend fun setPagesRecord(record: Int) {
        userRepository.setLocalRecord(
            userRepository.getLocalRecord().first().copy(
                quranPages = record
            )
        )
    }

    fun getWerdPage() = quranRepository.getWerdPage()

    suspend fun setWerdDone() {
        quranRepository.setIsWerdDone(true)
    }

    suspend fun setDoNotShowTutorial() {
        quranRepository.setShouldShowReaderTutorial(false)
    }

}