package bassamalim.hidaya.features.telawatClient

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class TelawatClientRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
) {

    fun getSuraNames() = db.suarDao().getNames()

    fun getReciterName(reciterId: Int) = db.telawatRecitersDao().getName(reciterId)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatVersionsDao().getVersion(reciterId, versionId)

    fun getRepeatMode() = PrefUtils.getInt(pref, bassamalim.hidaya.core.data.Prefs.TelawatRepeatMode)
    fun setRepeatMode(mode: Int) {
        pref.edit()
            .putInt(bassamalim.hidaya.core.data.Prefs.TelawatRepeatMode.key, mode)
            .apply()
    }

    fun getShuffleMode() = PrefUtils.getInt(pref, bassamalim.hidaya.core.data.Prefs.TelawatShuffleMode)
    fun setShuffleMode(mode: Int) {
        pref.edit()
            .putInt(bassamalim.hidaya.core.data.Prefs.TelawatShuffleMode.key, mode)
            .apply()
    }

}