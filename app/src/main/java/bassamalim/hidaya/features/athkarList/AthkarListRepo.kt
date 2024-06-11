package bassamalim.hidaya.features.athkarList

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.AthkarDB
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class AthkarListRepo @Inject constructor(
    private val sp: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getLanguage() = PrefUtils.getLanguage(sp)

    fun updateFavorites() {
        val favAthkar = db.athkarDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        sp.edit()
            .putString(Prefs.FavoriteAthkar.key, athkarJson)
            .apply()
    }

    fun getAthkar(type: String, category: Int): List<AthkarDB> {
        return when (type) {
            ListType.Favorite.name -> db.athkarDao().getFavorites()
            ListType.Custom.name -> db.athkarDao().getList(category)
            else -> db.athkarDao().getAll()
        }
    }

    fun getThikrParts(thikrId: Int) =
        db.athkarPartsDao().getThikrParts(thikrId)

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