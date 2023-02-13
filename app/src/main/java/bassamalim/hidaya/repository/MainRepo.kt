package bassamalim.hidaya.repository

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val res: Resources,
    val sp: SharedPreferences
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(sp)

    fun getDateOffset() = PrefUtils.getInt(sp, Prefs.DateOffset)

    fun updateDateOffset(offset: Int) {
        sp.edit()
            .putInt(Prefs.DateOffset.key, offset)
            .apply()
    }

    fun getWeekDays(): Array<String> = res.getStringArray(R.array.week_days)
    fun getHijriMonths(): Array<String> = res.getStringArray(R.array.hijri_months)
    fun getGregorianMonths(): Array<String> = res.getStringArray(R.array.gregorian_months)

    fun getUnchangedStr() = res.getString(R.string.unchanged)

}