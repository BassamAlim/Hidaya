package bassamalim.hidaya.features.quran

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import com.google.gson.Gson
import javax.inject.Inject

class QuranRepository @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getBookmarkedPage() = preferencesDS.getInt(Preference.BookmarkedPage)

    fun getBookmarkedSura() = preferencesDS.getInt(Preference.BookmarkedSura)

    fun getAllSuar() = db.suarDao().getAll()

    fun getSuraNames(): List<String> =
        if (preferencesDS.getLanguage() == Language.ENGLISH) db.suarDao().getNamesEn()
        else db.suarDao().getNames()

    fun getFavs() = db.suarDao().getFavs()

    fun setFav(suraId: Int, fav: Int) {
        db.suarDao().setFav(suraId, fav)
    }

    fun updateFavorites(favs: List<Int>) {
        val json = gson.toJson(favs.toIntArray())
        preferencesDS.setString(Preference.FavoriteSuar, json)
    }

    fun getShowTutorial() = preferencesDS.getBoolean(Preference.ShowQuranTutorial)

    fun setDoNotShowAgain() {
        preferencesDS.setBoolean(Preference.ShowQuranTutorial, false)
    }

    fun getSuraStr() = res.getString(R.string.sura)
    fun getPageStr() = res.getString(R.string.page)
    fun getNoBookmarkedPageStr() = res.getString(R.string.no_bookmarked_page)
    fun getBookmarkedPageStr() = res.getString(R.string.bookmarked_page)

}