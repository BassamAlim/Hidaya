package bassamalim.hidaya.features.main

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class MainRepo @Inject constructor(
    private val res: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    val unchangedStr = res.getString(R.string.unchanged)

    fun getDateOffset() = preferencesDS.getInt(Preference.DateOffset)

    fun updateDateOffset(offset: Int) {
        preferencesDS.setInt(Preference.DateOffset, offset)
    }

    fun getWeekDays(): Array<String> = res.getStringArray(R.array.week_days)

    fun getHijriMonths(): Array<String> = res.getStringArray(R.array.hijri_months)

    fun getGregorianMonths(): Array<String> = res.getStringArray(R.array.gregorian_months)

}