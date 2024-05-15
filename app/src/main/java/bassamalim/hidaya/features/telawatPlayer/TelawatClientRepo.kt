package bassamalim.hidaya.features.telawatPlayer

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class TelawatClientRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
) {

    fun getSuraNames() = db.suarDao().getNames()

    fun getReciterName(reciterId: Int) = db.telawatRecitersDao().getNameAr(reciterId)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatRewayatDao().getVersion(reciterId, versionId)

    fun getRepeatMode() = PrefUtils.getInt(pref, Prefs.TelawatRepeatMode)
    fun setRepeatMode(mode: Int) {
        pref.edit()
            .putInt(Prefs.TelawatRepeatMode.key, mode)
            .apply()
    }

    fun getShuffleMode() = PrefUtils.getInt(pref, Prefs.TelawatShuffleMode)
    fun setShuffleMode(mode: Int) {
        pref.edit()
            .putInt(Prefs.TelawatShuffleMode.key, mode)
            .apply()
    }

}