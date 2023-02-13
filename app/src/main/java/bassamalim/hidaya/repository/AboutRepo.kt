package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class AboutRepo @Inject constructor(
    private val pref: SharedPreferences
) {

    fun getLastUpdate() = PrefUtils.getString(pref, Prefs.LastDailyUpdate)

}