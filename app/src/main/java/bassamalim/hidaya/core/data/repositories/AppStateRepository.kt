package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.AppStatePreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppStateRepository @Inject constructor(
    private val resources: Resources,
    private val appStatePreferencesDataSource: AppStatePreferencesDataSource
) {

    fun getIsOnboardingCompleted() = appStatePreferencesDataSource.flow.map {
        it.isOnboardingCompleted
    }
    suspend fun setIsOnboardingCompleted(isCompleted: Boolean) {
        appStatePreferencesDataSource.update { it.copy(
            isOnboardingCompleted = isCompleted
        )}
    }

    fun getLastDailyUpdateMillis() = appStatePreferencesDataSource.flow.map {
        it.lastDailyUpdateMillis
    }
    suspend fun setLastDailyUpdateMillis(millis: Long) {
        appStatePreferencesDataSource.update { it.copy(
            lastDailyUpdateMillis = millis
        )}
    }

    fun getLastDbVersion() = appStatePreferencesDataSource.flow.map {
        it.lastDBVersion
    }
    suspend fun setLastDbVersion(version: Int) {
        appStatePreferencesDataSource.update { it.copy(
            lastDBVersion = version
        )}
    }

    fun getHijriMonths() =
        resources.getStringArray(R.array.numbered_hijri_months) as Array<String>

    fun getGregorianMonths() =
        resources.getStringArray(R.array.numbered_gregorian_months) as Array<String>

    fun getMonths() =
        resources.getStringArray(R.array.hijri_months) as Array<String>

    fun getWeekDays() =
        resources.getStringArray(R.array.week_days) as Array<String>

    fun getWeekDaysAbb(language: Language) =
        if (language == Language.ENGLISH) listOf("S", "M", "T", "W", "T", "F", "S")
        else listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")

}