package bassamalim.hidaya.features.qibla

import android.content.SharedPreferences
import bassamalim.hidaya.core.utils.LocUtils
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QiblaRepo @Inject constructor(
    private val pref: SharedPreferences
) {

    fun getLocation() = LocUtils.retrieveLocation(pref)

    fun numeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

}