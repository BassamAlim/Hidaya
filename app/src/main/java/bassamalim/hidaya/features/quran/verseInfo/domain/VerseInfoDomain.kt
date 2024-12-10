package bassamalim.hidaya.features.quran.verseInfo.domain

import bassamalim.hidaya.core.data.repositories.QuranRepository
import javax.inject.Inject

class VerseInfoDomain @Inject constructor(
    private val quranRepository: QuranRepository
) {

    suspend fun getVerse(id: Int) = quranRepository.getVerse(id)

    fun getBookmarks() = quranRepository.getBookmarks()

    fun setBookmark1VerseId(verseId: Int?) {
        quranRepository.setBookmark1VerseId(verseId)
    }

    fun setBookmark2VerseId(verseId: Int?) {
        quranRepository.setBookmark2VerseId(verseId)
    }

    fun setBookmark3VerseId(verseId: Int?) {
        quranRepository.setBookmark3VerseId(verseId)
    }

    fun setBookmark4VerseId(verseId: Int?) {
        quranRepository.setBookmark4VerseId(verseId)
    }

}