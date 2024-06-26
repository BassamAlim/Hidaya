package bassamalim.hidaya.features.quranViewer

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.QuranViewTypes
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuranViewerRepo @Inject constructor(
    val sp: SharedPreferences,
    private val db: AppDatabase
) {

    fun getLanguage() = PrefUtils.getLanguage(sp)

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(sp)

    fun getTheme() = PrefUtils.getTheme(sp)

    fun getSuraPageNum(suraId: Int) = db.suarDao().getSuraPageNum(suraId)

    fun getAyaPageNum(ayaId: Int) = db.ayatDao().getAyaPageNum(ayaId)

    fun getAyat() = db.ayatDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getViewType() = QuranViewTypes.valueOf(
        PrefUtils.getString(sp, Prefs.QuranViewType)
    )

    fun getShowTutorial() = PrefUtils.getBoolean(sp, Prefs.ShowQuranViewerTutorial)

    fun getTextSize() = PrefUtils.getFloat(sp, Prefs.QuranTextSize)

    fun getBookmarkedPage() = PrefUtils.getInt(sp, Prefs.BookmarkedPage)
    fun setBookmarkedPage(pageNum: Int, suraNum: Int) {
        sp.edit()
            .putInt(Prefs.BookmarkedPage.key, pageNum)
            .putInt(Prefs.BookmarkedSura.key, suraNum)
            .apply()
    }

    fun getPagesRecord() = PrefUtils.getInt(sp, Prefs.QuranPagesRecord)
    fun setPagesRecord(record: Int) {
        sp.edit()
            .putInt(Prefs.QuranPagesRecord.key, record)
            .apply()
    }

    fun getWerdPage() = PrefUtils.getInt(sp, Prefs.WerdPage)
    fun setWerdDone() {
        sp.edit()
            .putBoolean(Prefs.WerdDone.key, true)
            .apply()
    }

    fun setDoNotShowTutorial() {
        sp.edit()
            .putBoolean(Prefs.ShowQuranViewerTutorial.key, false)
            .apply()
    }

}