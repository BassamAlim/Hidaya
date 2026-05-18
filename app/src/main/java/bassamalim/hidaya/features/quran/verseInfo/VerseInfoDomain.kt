package bassamalim.hidaya.features.quran.verseInfo

import bassamalim.hidaya.core.data.repositories.QuranRepository
import javax.inject.Inject

class VerseInfoDomain @Inject constructor(
    private val quranRepository: QuranRepository
) {

    suspend fun getVerse(id: Int) = quranRepository.getVerse(id)

    fun getBookmarks() = quranRepository.getBookmarks()

    suspend fun setBookmarkVerseId(index: Int, verseId: Int?) {
        quranRepository.setBookmarkVerseId(index, verseId)
    }

}