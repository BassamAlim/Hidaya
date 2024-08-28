package bassamalim.hidaya.core.data.repositories

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class LiveContentRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getMakkahVideoId() = remoteConfig.getString("makkah_url")

    fun getMadinaVideoId() = remoteConfig.getString("madina_url")

    fun getRadioUrl() = remoteConfig.getString("quran_radio_url")

}