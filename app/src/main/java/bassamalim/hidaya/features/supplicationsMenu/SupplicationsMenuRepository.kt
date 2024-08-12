package bassamalim.hidaya.features.supplicationsMenu

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.models.Remembrance
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

    fun getAthkar(type: String, category: Int): List<Remembrance> =
        when (type) {
            ListType.FAVORITES.name -> db.athkarDao().observeFavorites()
            ListType.CUSTOM.name -> db.athkarDao().observeCategoryRemembrances(category)
            else -> db.athkarDao().getAll()
        }

    fun getThikrParts(thikrId: Int) =
        db.athkarPartsDao().getRemembrancePassages(thikrId)

    fun getName(language: Language, category: Int): String =
        when (language) {
            Language.ARABIC -> db.athkarCategoryDao().getNameAr(category)
            Language.ENGLISH -> db.athkarCategoryDao().getNameEn(category)
        }

    fun setFavorite(itemId: Int, value: Int) {
        db.athkarDao().setIsFavorite(itemId, value)
    }

}