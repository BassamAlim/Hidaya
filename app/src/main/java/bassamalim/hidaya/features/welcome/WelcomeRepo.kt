package bassamalim.hidaya.features.welcome

import android.content.SharedPreferences
import javax.inject.Inject

class WelcomeRepo @Inject constructor(
    val pref: SharedPreferences
) {

    fun unsetFirstTime() {
        pref.edit()
            .putBoolean(bassamalim.hidaya.core.data.Prefs.FirstTime.key, false)
            .apply()
    }

}