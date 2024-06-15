package bassamalim.hidaya.features.telawatSuar

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import com.google.gson.Gson
import javax.inject.Inject

class TelawatSuarRepo @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
    private val gson: Gson
) {

    private val language = preferencesDS.getLanguage()
    fun getLanguage() = language

    fun getSuraNames() = db.suarDao().getNames()

    fun getSearchNames() = db.suarDao().getSearchNames()

    fun getReciterName(id: Int) =
        if (language == Language.ARABIC) db.telawatRecitersDao().getNameAr(id)
        else db.telawatRecitersDao().getNameEn(id)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatRewayatDao().getVersion(reciterId, versionId)

    fun getFavs() = db.suarDao().getFavs()

    fun setFav(suraNum: Int, value: Int) {
        db.suarDao().setFav(suraNum, value)
    }

    fun updateFavorites() {
        val suarJson = gson.toJson(db.suarDao().getFavs())
        preferencesDS.setString(Preference.FavoriteSuar, suarJson)
    }

}