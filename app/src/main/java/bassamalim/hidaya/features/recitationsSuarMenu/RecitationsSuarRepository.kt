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

    fun getSuraNames() = db.suarDao().getDecoratedNamesAr()

    fun getSearchNames() = db.suarDao().getPlainNamesAr()

    fun getReciterName(id: Int) =
        if (language == Language.ARABIC) db.telawatRecitersDao().getNameAr(id)
        else db.telawatRecitersDao().getNameEn(id)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatRewayatDao().getVersion(reciterId, versionId)

    fun getFavs() = db.suarDao().observeIsFavorites()

    fun setFav(suraNum: Int, value: Int) {
        db.suarDao().setIsFavorite(suraNum, value)
    }

    fun updateFavorites() {
        val suarJson = gson.toJson(db.suarDao().observeIsFavorites())
        preferencesDS.setString(Preference.FavoriteSuar, suarJson)
    }

}