package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.database.AppDatabase
import com.google.gson.Gson
import javax.inject.Inject

class TelawatSuarRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    fun getSuraNames() = db.suarDao().getNames()

    fun getSearchNames() = db.suarDao().getSearchNames()

    fun getReciterName(id: Int) = db.telawatRecitersDao().getName(id)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatVersionsDao().getVersion(reciterId, versionId)

    fun getFavs() = db.suarDao().getFavs()

    fun setFav(suraNum: Int, value: Int) = db.suarDao().setFav(suraNum, value)

    fun updateFavorites() {
        val surasJson = gson.toJson(db.suarDao().getFavs())
        pref.edit()
            .putString(Prefs.FavoriteSuras.key, surasJson)
            .apply()
    }

}