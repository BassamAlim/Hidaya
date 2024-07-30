package bassamalim.hidaya.features.hijriDatePicker.data

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HijriDatePickerRepository @Inject constructor(
    private val resources: Resources,
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource
) {

    suspend fun getLanguage() = appSettingsPrefsRepo.getLanguage().first()

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    fun getMonths() =
        resources.getStringArray(R.array.hijri_months) as Array<String>

    fun getWeekDays() =
        resources.getStringArray(R.array.week_days) as Array<String>

    fun getWeekDaysAbb(language: Language) =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

}