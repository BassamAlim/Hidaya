package bassamalim.hidaya.features.hijriDatePicker

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class HijriDatePickerRepo @Inject constructor(
    resources: Resources,
    pref: SharedPreferences
) {

    val language = PrefUtils.getLanguage(pref)

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    val months = resources.getStringArray(R.array.hijri_months) as Array<String>

    val weekDays = resources.getStringArray(R.array.week_days) as Array<String>

    val weekDaysAbb =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

}