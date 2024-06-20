package bassamalim.hidaya.features.supplicationsMenu

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.AthkarDB
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import com.google.gson.Gson
import javax.inject.Inject

class SupplicationsMenuRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun updateFavorites() {
        val favAthkar = db.athkarDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        preferencesDS.setString(Preference.FavoriteAthkar, athkarJson)
    }

    fun getAthkar(type: String, category: Int): List<AthkarDB> =
        when (type) {
            ListType.Favorite.name -> db.athkarDao().getFavorites()
            ListType.Custom.name -> db.athkarDao().getList(category)
            else -> db.athkarDao().getAll()
        }

    fun getThikrParts(thikrId: Int) =
        db.athkarPartsDao().getThikrParts(thikrId)

    fun getName(language: Language, category: Int): String =
        when (language) {
            Language.ARABIC -> db.athkarCategoryDao().getName(category)
            Language.ENGLISH -> db.athkarCategoryDao().getNameEn(category)
        }

    fun setFavorite(itemId: Int, value: Int) {
        db.athkarDao().setFav(itemId, value)
    }

}