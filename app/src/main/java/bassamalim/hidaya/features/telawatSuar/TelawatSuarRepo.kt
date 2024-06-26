package bassamalim.hidaya.features.telawatSuar

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class TelawatSuarRepo @Inject constructor(
    private val sp: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    val language = PrefUtils.getLanguage(sp)

    fun getSuraNames() = db.suarDao().getNames()

    fun getSearchNames() = db.suarDao().getSearchNames()

    fun getReciterName(id: Int) =
        if (language == Language.ARABIC) db.telawatRecitersDao().getNameAr(id)
        else db.telawatRecitersDao().getNameEn(id)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatRewayatDao().getVersion(reciterId, versionId)

    fun getFavs() = db.suarDao().getFavs()

    fun setFav(suraNum: Int, value: Int) = db.suarDao().setFav(suraNum, value)

    fun updateFavorites() {
        val suarJson = gson.toJson(db.suarDao().getFavs())
        sp.edit()
            .putString(Prefs.FavoriteSuar.key, suarJson)
            .apply()
    }

}