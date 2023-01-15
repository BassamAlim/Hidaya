package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.enum.Language
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class QuranRepo @Inject constructor(
    private val context: Context,
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

    fun getSuraStr() = context.getString(R.string.sura)
    fun getPageStr() = context.getString(R.string.page)
    fun getNoBookmarkedPageStr() = context.getString(R.string.no_bookmarked_page)
    fun getBookmarkedPageStr() = context.getString(R.string.bookmarked_page)


}