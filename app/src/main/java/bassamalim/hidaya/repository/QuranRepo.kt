package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class QuranRepo @Inject constructor(
    private val resources: Resources,
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

    fun getBookmarkedPage() = PrefUtils.getInt(pref, Prefs.BookmarkedPage)

    fun getBookmarkedSura() = PrefUtils.getInt(pref, Prefs.BookmarkedSura)

    fun getAllSuras() = db.suarDao().getAll()

    fun getSuraNames(): List<String> {
        return if (PrefUtils.getLanguage(pref) == Language.ENGLISH) db.suarDao().getNamesEn()
        else db.suarDao().getNames()
    }

    fun getFavs() = db.suarDao().getFavs()

    fun updateFavorites(favs: List<Int>) {
        val json = gson.toJson(favs.toIntArray())
        pref.edit()
            .putString(Prefs.FavoriteSuras.key, json)
            .apply()
    }

    fun setDoNotShowAgain() {
        pref.edit()
            .putBoolean(Prefs.ShowQuranTutorial.key, false)
            .apply()
    }

    fun getSuraStr() = resources.getString(R.string.sura)
    fun getPageStr() = resources.getString(R.string.page)
    fun getNoBookmarkedPageStr() = resources.getString(R.string.no_bookmarked_page)
    fun getBookmarkedPageStr() = resources.getString(R.string.bookmarked_page)

}