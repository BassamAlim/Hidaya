package bassamalim.hidaya.features.recitationsSuarMenu

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import com.google.gson.Gson
import javax.inject.Inject

class RecitationsSuarRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val gson: Gson
) {

    private val language = preferencesDS.getLanguage()
    fun getLanguage() = language

    fun getSuraNames() = db.surasDao().getDecoratedNamesAr()

    fun getSearchNames() = db.surasDao().getPlainNamesAr()

    fun getReciterName(id: Int) =
        if (language == Language.ARABIC) db.recitationRecitersDao().getNameAr(id)
        else db.recitationRecitersDao().getNameEn(id)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.recitationVersionsDao().getVersion(reciterId, versionId)

    fun getFavs() = db.surasDao().observeIsFavorites()

    fun setFav(suraNum: Int, value: Int) {
        db.surasDao().setIsFavorite(suraNum, value)
    }

    fun updateFavorites() {
        val suarJson = gson.toJson(db.surasDao().observeIsFavorites())
        preferencesDS.setString(Preference.FavoriteSuar, suarJson)
    }

}