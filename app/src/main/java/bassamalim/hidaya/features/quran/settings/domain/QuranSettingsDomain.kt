package bassamalim.hidaya.features.quran.settings.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.features.quran.reader.ui.QuranViewType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuranSettingsDomain @Inject constructor(
    private val quranRepository: QuranRepository,
    private val recitationsRepository: RecitationsRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

    suspend fun getReciterNames() = recitationsRepository.getVerseReciterNames()

    fun getViewType() = quranRepository.getViewType()

    suspend fun setViewType(viewType: QuranViewType) {
        quranRepository.setViewType(viewType)
    }

    fun getFillPage() = quranRepository.getFillPage()

    suspend fun setFillPage(fillPage: Boolean) {
        quranRepository.setFillPage(fillPage)
    }

    fun getTextSize() = quranRepository.getTextSize()

    suspend fun setTextSize(size: Float) {
        quranRepository.setTextSize(size)
    }

    fun getReciterId() = recitationsRepository.getVerseReciterId()

    suspend fun setReciterId(reciterId: Int) = recitationsRepository.setVerseReciterId(reciterId)

    fun getRepeatMode() = recitationsRepository.getVerseRepeatMode()

    suspend fun setRepeatMode(repeatMode: VerseRepeatMode) =
        recitationsRepository.setVerseRepeatMode(repeatMode)

    fun getShouldStopOnSuraEnd() = recitationsRepository.getShouldStopOnSuraEnd()

    suspend fun setShouldStopOnSuraEnd(shouldStop: Boolean) =
        recitationsRepository.setShouldStopOnSuraEnd(shouldStop)

    fun getShouldStopOnPageEnd() = recitationsRepository.getShouldStopOnPageEnd()

    suspend fun setShouldStopOnPageEnd(shouldStop: Boolean) =
        recitationsRepository.setShouldStopOnPageEnd(shouldStop)

}