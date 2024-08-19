package bassamalim.hidaya.core.data.repositories

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class LiveContentRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getRadioUrl() = remoteConfig.getString("quran_radio_url")

}