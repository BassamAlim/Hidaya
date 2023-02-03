package bassamalim.hidaya.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import bassamalim.hidaya.other.Global
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class TvRepo @Inject constructor(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun fetchInfo(
        onSuccess: (String, String, String) -> Unit
    ) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(context as Activity) { task ->
                if (task.isSuccessful) {
                    val apiKey = remoteConfig.getString("yt_api_key")
                    val makkahVidId = remoteConfig.getString("makkah_url")
                    val madinaVidId = remoteConfig.getString("madina_url")

                    Log.i(Global.TAG, "Config params updated")
                    Log.i(Global.TAG, "Makkah Video ID: $makkahVidId")
                    Log.i(Global.TAG, "Madina Video ID: $madinaVidId")

                    onSuccess(apiKey, makkahVidId, madinaVidId)
                }
                else Log.e(Global.TAG, "Fetch failed")
            }
    }

}