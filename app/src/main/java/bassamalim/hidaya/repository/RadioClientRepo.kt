package bassamalim.hidaya.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import bassamalim.hidaya.other.Global
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class RadioClientRepo @Inject constructor(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getLink(): String? {
        var link: String? = null

        remoteConfig.fetchAndActivate().addOnCompleteListener(context as Activity) { task ->
            if (task.isSuccessful) {
                link = remoteConfig.getString("quran_radio_url")
                Log.i(Global.TAG, "Config params updated")
                Log.i(Global.TAG, "Quran Radio URL: $link")
            }
            else Log.e(Global.TAG, "Fetch failed")
        }

        return link
    }

}