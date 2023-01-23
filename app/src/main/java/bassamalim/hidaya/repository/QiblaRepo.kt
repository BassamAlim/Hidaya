package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QiblaRepo @Inject constructor(
    private val pref: SharedPreferences
) {

    fun getLocation() = LocUtils.retrieveLocation(pref)

    fun numeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

}