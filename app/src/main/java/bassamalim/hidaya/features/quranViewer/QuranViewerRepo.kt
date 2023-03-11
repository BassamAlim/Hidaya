package bassamalim.hidaya.features.quranViewer

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.QViewType
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuranViewerRepo @Inject constructor(
    val sp: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(sp)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getTheme() = PrefUtils.getTheme(sp)

    fun getPage(suraId: Int) = db.suarDao().getPage(suraId)

    fun getAyat() = db.ayahDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getViewType() = QViewType.valueOf(
        PrefUtils.getString(sp, Prefs.QuranViewType)
    )
    fun setViewType(viewType: QViewType) {
        sp.edit()
            .putString(Prefs.QuranViewType.key, viewType.name)
            .apply()
    }

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

    fun getWerdPage() = PrefUtils.getInt(sp, Prefs.TodayWerdPage)

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

    fun getReciterNames() = db.ayatRecitersDao().getNames().toTypedArray()

}