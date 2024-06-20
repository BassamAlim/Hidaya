package bassamalim.hidaya.features.hijriDatePicker

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import javax.inject.Inject

class HijriDatePickerRepository @Inject constructor(
    private val resources: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    private val language = preferencesDS.getLanguage()
    fun getLanguage() = language

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getMonths() =
        resources.getStringArray(R.array.hijri_months) as Array<String>

    fun getWeekDays() =
        resources.getStringArray(R.array.week_days) as Array<String>

    fun getWeekDaysAbb() =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

}