package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class TelawatClientRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val db: AppDatabase,
) {

    fun getSuraNames() = db.suarDao().getNames()

    fun getReciterName(reciterId: Int) = db.telawatRecitersDao().getName(reciterId)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatVersionsDao().getVersion(reciterId, versionId)

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