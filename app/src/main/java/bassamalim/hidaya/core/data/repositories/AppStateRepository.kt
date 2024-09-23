package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.AppStatePreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import javax.inject.Inject

class AppStateRepository @Inject constructor(
    private val resources: Resources,
    private val appStatePreferencesDataSource: AppStatePreferencesDataSource
) {

    fun getHijriMonthNames() =
        resources.getStringArray(R.array.hijri_months) as Array<String>

    fun getGregorianMonthNames() =
        resources.getStringArray(R.array.gregorian_months) as Array<String>

    fun getNumberedHijriMonthNames() =
        resources.getStringArray(R.array.numbered_hijri_months) as Array<String>

    fun getNumberedGregorianMonthNames() =
        resources.getStringArray(R.array.numbered_gregorian_months) as Array<String>

    fun getWeekDayNames() =
        resources.getStringArray(R.array.week_days) as Array<String>

    fun getWeekDaysAbbreviations(language: Language) =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

    fun isOnboardingCompleted() = appStatePreferencesDataSource.getOnboardingCompleted()

    suspend fun setOnboardingCompleted(isCompleted: Boolean) {
        appStatePreferencesDataSource.updateOnboardingCompleted(isCompleted)
    }

    fun getLastDailyUpdateMillis() = appStatePreferencesDataSource.getLastDailyUpdateMillis()

    suspend fun setLastDailyUpdateMillis(millis: Long) {
        appStatePreferencesDataSource.updateLastDailyUpdateMillis(millis)
    }

    fun getLastDbVersion() = appStatePreferencesDataSource.getLastDBVersion()

    suspend fun setLastDbVersion(version: Int) {
        appStatePreferencesDataSource.updateLastDBVersion(version)
    }

}