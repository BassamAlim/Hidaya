package bassamalim.hidaya.core.data.repositories

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.AppStatePreferencesDataSource
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppStateRepository @Inject constructor(
    private val resources: Resources,
    private val appStatePreferencesDataSource: AppStatePreferencesDataSource,
    @ApplicationScope private val scope: CoroutineScope
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

    fun getWeekDaysAbbreviations(language: Language) = when (language) {
        Language.ARABIC -> listOf("أ", "إ", "ث", "أ", "خ", "ج", "س")
        Language.ENGLISH -> listOf("S", "M", "T", "W", "T", "F", "S")
    }

    fun isOnboardingCompleted() = appStatePreferencesDataSource.getOnboardingCompleted()

    fun setOnboardingCompleted(isCompleted: Boolean) {
        scope.launch {
            appStatePreferencesDataSource.updateOnboardingCompleted(isCompleted)
        }
    }

    fun getLastDailyUpdateMillis() = appStatePreferencesDataSource.getLastDailyUpdateMillis()

    fun setLastDailyUpdateMillis(millis: Long) {
        scope.launch {
            appStatePreferencesDataSource.updateLastDailyUpdateMillis(millis)
        }
    }

    fun getLastDbVersion() = appStatePreferencesDataSource.getLastDBVersion()

    suspend fun setLastDbVersion(version: Int) {
        appStatePreferencesDataSource.updateLastDBVersion(version)
    }

    fun getSources(): List<Source> {
        val titles = resources.getStringArray(R.array.source_titles)
        val sourceNames = resources.getStringArray(R.array.sources)
        val sourceUrls = resources.getStringArray(R.array.source_urls)

        return titles.mapIndexed { index, _ ->
            Source(
                title = titles[index],
                sourceName = sourceNames[index],
                url = sourceUrls[index]
            )
        }
    }

}