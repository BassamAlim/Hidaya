package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.PrefUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class AboutRepo @Inject constructor(
    private val pref: SharedPreferences,
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getLastUpdate() = PrefUtils.getString(pref, Prefs.LastDailyUpdate)

    fun getUpdateURL() = remoteConfig.getString(Global.UPDATE_URL)

}