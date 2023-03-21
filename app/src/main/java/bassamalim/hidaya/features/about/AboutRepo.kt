package bassamalim.hidaya.features.about

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class AboutRepo @Inject constructor(
    private val sp: SharedPreferences
) {

    fun getLastUpdate() =
        PrefUtils.getString(sp, Prefs.DailyUpdateRecord)

}