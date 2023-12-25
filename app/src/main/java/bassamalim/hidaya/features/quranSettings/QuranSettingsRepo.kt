package bassamalim.hidaya.features.quranSettings

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.enums.QuranViewTypes
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuranSettingsRepo @Inject constructor(
    val sp: SharedPreferences,
    private val db: AppDatabase
) {

    fun getViewType() = QuranViewTypes.valueOf(
        PrefUtils.getString(sp, Prefs.QuranViewType)
    )
    fun setViewType(type: QuranViewTypes) {
        sp.edit()
            .putString(Prefs.QuranViewType.key, type.name)
            .apply()
    }

    fun getReciterNames() = db.ayatRecitersDao().getNames()

}