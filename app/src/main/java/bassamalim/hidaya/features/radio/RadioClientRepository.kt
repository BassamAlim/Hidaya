package bassamalim.hidaya.features.radio

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class RadioClientRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getLink() = remoteConfig.getString("quran_radio_url")

}