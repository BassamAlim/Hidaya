package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.QViewType
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QuranViewerRepo @Inject constructor(
    val pref: SharedPreferences,
    private val db: AppDatabase
) {

    val language = PrefUtils.getLanguage(pref)
    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getPage(suraId: Int) = db.suarDao().getPage(suraId)

    fun getAyat() = db.ayahDao().getAll()

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getViewType() = QViewType.valueOf(
        PrefUtils.getString(pref, Prefs.QuranViewType)
    )
    fun setViewType(viewType: QViewType) {
        pref.edit()
            .putString(Prefs.QuranViewType.key, viewType.name)
            .apply()
    }

    fun getTextSize() = PrefUtils.getInt(pref, Prefs.AthkarTextSize)
    fun setTextSize(textSize: Float) {
        pref.edit()
            .putInt(Prefs.QuranTextSize.key, textSize.toInt())
            .apply()
    }

    fun getBookmarkedPage() = PrefUtils.getInt(pref, Prefs.BookmarkedPage)
    fun setBookmarkedPage(pageNum: Int, suraNum: Int) {
        pref.edit()
            .putInt(Prefs.BookmarkedPage.key, pageNum)
            .putInt(Prefs.BookmarkedSura.key, suraNum)
            .apply()
    }

    fun getPagesRecord() = PrefUtils.getInt(pref, Prefs.QuranPagesRecord)
    fun setPagesRecord(record: Int) {
        pref.edit()
            .putInt(Prefs.QuranPagesRecord.key, record)
            .apply()
    }

    fun getWerdPage() = PrefUtils.getInt(pref, Prefs.TodayWerdPage)

    fun setWerdDone() {
        pref.edit()
            .putBoolean(Prefs.WerdDone.key, true)
            .apply()
    }

    fun setDoNotShowTutorial() {
        pref.edit()
            .putBoolean(Prefs.ShowQuranViewerTutorial.key, false)
            .apply()
    }

    fun getReciterNames() = db.ayatRecitersDao().getNames().toTypedArray()

}