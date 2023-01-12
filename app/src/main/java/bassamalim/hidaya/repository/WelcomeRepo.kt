package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import javax.inject.Inject

class WelcomeRepo @Inject constructor(
    private val pref: SharedPreferences
) {

    fun unsetFirstTime() {
        pref.edit()
            .putBoolean(Prefs.FirstTime.key, false)
            .apply()
    }

}