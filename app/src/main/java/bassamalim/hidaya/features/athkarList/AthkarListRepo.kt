package bassamalim.hidaya.features.athkarList

import android.app.Application
import android.content.SharedPreferences
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.AthkarDB
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class AthkarListRepo @Inject constructor(
    private val app: Application,
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getLanguage() = PrefUtils.getLanguage(pref)

    fun updateFavorites() {
        val favAthkar = db.athkarDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        pref.edit()
            .putString(bassamalim.hidaya.core.data.Prefs.FavoriteAthkar.key, athkarJson)
            .apply()
    }

    fun getAthkar(type: String, category: Int): List<AthkarDB> {
        return when (type) {
            ListType.Favorite.name -> db.athkarDao().getFavorites()
            ListType.Custom.name -> db.athkarDao().getList(category)
            else -> db.athkarDao().getAll()
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

    fun getFavoriteAthkarStr() = app.getString(R.string.favorite_athkar)
    fun getAllAthkarStr() = app.getString(R.string.all_athkar)

}