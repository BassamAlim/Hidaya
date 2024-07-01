package bassamalim.hidaya.features.hijriDatePicker.data

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.repositories.AppSettingsPreferencesRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HijriDatePickerRepository @Inject constructor(
    private val resources: Resources,
    private val appSettingsPrefsRepo: AppSettingsPreferencesRepository
) {

    suspend fun getLanguage() = appSettingsPrefsRepo.flow.first()
        .language

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.flow.first()
        .numeralsLanguage

    fun getMonths() =
        resources.getStringArray(R.array.hijri_months) as Array<String>

    fun getWeekDays() =
        resources.getStringArray(R.array.week_days) as Array<String>

    fun getWeekDaysAbb(language: Language) =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

}