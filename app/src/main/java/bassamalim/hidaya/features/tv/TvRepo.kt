package bassamalim.hidaya.features.tv

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class TvRepo @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getMakkahVidId() = remoteConfig.getString("makkah_url")

    fun getMadinaVidId() = remoteConfig.getString("madina_url")

}