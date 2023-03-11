package bassamalim.hidaya.features.tv

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class TvRepo @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getApiKey(): String = remoteConfig.getString("yt_api_key")

    fun getMakkahVidId(): String = remoteConfig.getString("makkah_url")

    fun getMadinaVidId(): String = remoteConfig.getString("madina_url")

}