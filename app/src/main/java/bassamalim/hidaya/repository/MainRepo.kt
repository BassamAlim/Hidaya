package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.LocUtils
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val resources: Resources,
    val pref: SharedPreferences
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getDateOffset() = PrefUtils.getInt(pref, Prefs.DateOffset)

    fun updateDateOffset(offset: Int) {
        pref.edit()
            .putInt(Prefs.DateOffset.key, offset)
            .apply()
    }

    fun getWeekDays(): Array<String> {
        return resources.getStringArray(R.array.week_days)
    }

    fun getHijriMonths(): Array<String> {
        return resources.getStringArray(R.array.hijri_months)
    }

    fun getGregorianMonths(): Array<String> {
        return resources.getStringArray(R.array.gregorian_months)
    }

    fun getLocation() = LocUtils.retrieveLocation(pref)

    fun getUnchangedStr() = resources.getString(R.string.unchanged)

}