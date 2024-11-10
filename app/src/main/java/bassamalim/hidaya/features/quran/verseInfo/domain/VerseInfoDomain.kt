package bassamalim.hidaya.features.quran.verseInfo.domain

import bassamalim.hidaya.core.data.repositories.QuranRepository
import javax.inject.Inject

class VerseInfoDomain @Inject constructor(
    private val quranRepository: QuranRepository
) {

    suspend fun getVerse(id: Int) = quranRepository.getVerse(id)

}