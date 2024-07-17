package bassamalim.hidaya.features.recitationsPlayer

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class RecitationsPlayerClientRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase,
) {

    fun getSuraNames() = db.suarDao().getNames()

    fun getReciterName(reciterId: Int) =
        db.telawatRecitersDao().getNameAr(reciterId)

    fun getVersion(reciterId: Int, versionId: Int) =
        db.telawatRewayatDao().getVersion(reciterId, versionId)

    fun getRepeatMode() =
        preferencesDS.getInt(Preference.TelawatRepeatMode)

    fun setRepeatMode(mode: Int) {
        preferencesDS.setInt(Preference.TelawatRepeatMode, mode)
    }

    fun getShuffleMode() =
        preferencesDS.getInt(Preference.TelawatShuffleMode)

    fun setShuffleMode(mode: Int) {
        preferencesDS.setInt(Preference.TelawatShuffleMode, mode)
    }

}