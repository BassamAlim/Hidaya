package bassamalim.hidaya.features.remembrancesMenu

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.models.Remembrance
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.ListType
import com.google.gson.Gson
import javax.inject.Inject

class RemembrancesMenuRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getLanguage() = preferencesDS.getLanguage()

    fun updateFavorites() {
        val favAthkar = db.remembrancesDao().getFavs()
        val athkarJson = gson.toJson(favAthkar)
        preferencesDS.setString(Preference.RemembranceFavorites, athkarJson)
    }

    fun getRemembrances(type: String, category: Int): List<Remembrance> =
        when (type) {
            ListType.FAVORITES.name -> db.remembrancesDao().observeFavorites()
            ListType.CUSTOM.name -> db.remembrancesDao().observeCategoryRemembrances(category)
            else -> db.remembrancesDao().getAll()
        }

    fun getRemembrancePassages(remembrancesId: Int) =
        db.remembrancePassagesDao().getRemembrancePassages(remembrancesId)

    fun getName(language: Language, category: Int): String =
        when (language) {
            Language.ARABIC -> db.remembranceCategoriesDao().getNameAr(category)
            Language.ENGLISH -> db.remembranceCategoriesDao().getNameEn(category)
        }

    fun setFavorite(itemId: Int, value: Int) {
        db.remembrancesDao().setIsFavorite(itemId, value)
    }

}