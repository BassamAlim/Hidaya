package bassamalim.hidaya.features.telawat

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import com.google.gson.Gson
import javax.inject.Inject

class TelawatRepo @Inject constructor(
    private val res: Resources,
    private val sp: SharedPreferences,
    private val db: AppDatabase,
    private val gson: Gson
) {

    val language = PrefUtils.getLanguage(sp)

    fun getFavs() = db.telawatRecitersDao().getFavs()
    fun setFav(reciterId: Int, fav: Int) =
        db.telawatRecitersDao().setFav(reciterId, fav)

    fun getReciters() = db.telawatRecitersDao().getAll()

    fun getReciterTelawat(reciterId: Int) = db.telawatDao().getReciterTelawat(reciterId)

    fun getAllVersions() = db.telawatDao().all

    fun getRewaya(reciterId: Int, versionId: Int): String =
        db.telawatVersionsDao().getVersion(reciterId, versionId).getRewaya()

    fun getReciterName(reciterId: Int) = db.telawatRecitersDao().getName(reciterId)

    fun getSuraNames() = db.suarDao().getNames()
    fun getSuraNamesEn() = db.suarDao().getNamesEn()

    fun getSelectedVersions(): MutableList<Boolean> {
        val selectedVersions = mutableListOf<Boolean>()

        val json = PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.SelectedRewayat)
        if (json.isNotEmpty()) {
            val boolArr = gson.fromJson(json, BooleanArray::class.java)
            boolArr.forEach { bool -> selectedVersions.add(bool) }
        }
        else getRewayat().forEach { _ -> selectedVersions.add(true) }

        return selectedVersions
    }

    fun getLastPlayedMediaId() = PrefUtils.getString(sp, bassamalim.hidaya.core.data.Prefs.LastPlayedMediaId)

    fun updateFavorites() {
        val recitersJson = gson.toJson(db.telawatRecitersDao().getFavs())
        sp.edit()
            .putString(bassamalim.hidaya.core.data.Prefs.FavoriteReciters.key, recitersJson)
            .apply()
    }

    fun getRewayat(): Array<String> = res.getStringArray(R.array.rewayat)

    fun getLastPlayStr() = res.getString(R.string.last_play)
    fun getSuraStr() = res.getString(R.string.sura)
    fun getForReciterStr() = res.getString(R.string.for_reciter)
    fun getInRewayaOfStr() = res.getString(R.string.in_rewaya_of)
    fun getNoLastPlayStr() = res.getString(R.string.no_last_play)

}