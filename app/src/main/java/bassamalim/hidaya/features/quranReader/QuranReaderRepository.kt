package bassamalim.hidaya.features.quranReader

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class QuranReaderRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getTheme() = preferencesDS.getTheme()

    fun getSuraPageNum(suraId: Int) = db.suarDao().getSuraPageNum(suraId)

    fun getAyaPageNum(ayaId: Int) = db.ayatDao().getAyaPageNum(ayaId)

    fun getAyat() = db.ayatDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()

    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getViewType() =
        QuranViewType.valueOf(preferencesDS.getString(Preference.QuranViewType))

    fun getShowTutorial() =
        preferencesDS.getBoolean(Preference.ShowQuranViewerTutorial)

    fun getTextSize() = preferencesDS.getFloat(Preference.QuranTextSize)

    fun getBookmarkedPage() = preferencesDS.getInt(Preference.BookmarkedPage)

    fun setBookmarkedPage(pageNum: Int, suraNum: Int) {
        preferencesDS.setInt(Preference.BookmarkedPage, pageNum)
        preferencesDS.setInt(Preference.BookmarkedSura, suraNum)
    }

    fun getPagesRecord() = preferencesDS.getInt(Preference.QuranPagesRecord)

    fun setPagesRecord(record: Int) {
        preferencesDS.setInt(Preference.QuranPagesRecord, record)
    }

    fun getWerdPage() = preferencesDS.getInt(Preference.WerdPage)

    fun setWerdDone() {
        preferencesDS.setBoolean(Preference.WerdDone, true)
    }

    fun setDoNotShowTutorial() {
        preferencesDS.setBoolean(Preference.ShowQuranViewerTutorial, false)
    }

}