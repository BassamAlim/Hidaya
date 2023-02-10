package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class DateConverterRepo @Inject constructor(
    private val resources: Resources,
    pref: SharedPreferences
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getHijriMonths(): Array<String> {
        return resources.getStringArray(R.array.numbered_hijri_months)
    }

    fun getGregorianMonths(): Array<String> {
        return resources.getStringArray(R.array.numbered_gregorian_months)
    }

}