package bassamalim.hidaya.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class RadioClientRepo @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun getLink() = remoteConfig.getString("quran_radio_url")

}