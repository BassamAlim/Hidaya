package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.AthkarDB
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class AthkarListRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getLanguage() = PrefUtils.getLanguage(pref)

    fun updateFavorites() {
        val favAthkar = db.athkarDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        pref.edit()
            .putString(Prefs.FavoriteAthkar.key, athkarJson)
            .apply()
    }

    fun getAthkar(type: String, category: Int): List<AthkarDB> {
        return when (type) {
            "all" -> db.athkarDao().getAll()
            "favorite" -> db.athkarDao().getFavorites()
            else -> db.athkarDao().getList(category)
        }
    }

    fun getThikrs(thikrId: Int) = db.thikrsDao().getThikrs(thikrId)

    fun getName(language: Language, category: Int): String {
        return when (language) {
            Language.ARABIC -> db.athkarCategoryDao().getName(category)
            Language.ENGLISH -> db.athkarCategoryDao().getNameEn(category)
        }
    }

    fun setFavorite(itemId: Int, value: Int) {
        db.athkarDao().setFav(itemId, value)
    }

}