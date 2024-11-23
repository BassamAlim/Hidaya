package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppSettingsRepository @Inject constructor(
    private val appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource,
    @ApplicationScope private val scope: CoroutineScope
) {

    fun getLanguage() = appSettingsPreferencesDataSource.getLanguage()

    fun setLanguage(language: Language) {
        scope.launch {
            appSettingsPreferencesDataSource.updateLanguage(language)
        }
    }

    fun getNumeralsLanguage() = appSettingsPreferencesDataSource.getNumeralsLanguage()

    fun setNumeralsLanguage(numeralsLanguage: Language) {
        scope.launch {
            appSettingsPreferencesDataSource.updateNumeralsLanguage(numeralsLanguage)
        }
    }

    fun getTheme() = appSettingsPreferencesDataSource.getTheme()

    fun setTheme(theme: Theme) {
        scope.launch {
            appSettingsPreferencesDataSource.updateTheme(theme)
        }
    }

    fun getTimeFormat() = appSettingsPreferencesDataSource.getTimeFormat()

    fun setTimeFormat(timeFormat: TimeFormat) {
        scope.launch {
            appSettingsPreferencesDataSource.updateTimeFormat(timeFormat)
        }
    }

    fun getDateOffset() = appSettingsPreferencesDataSource.getDateOffset()

    fun setDateOffset(dateOffset: Int) {
        scope.launch {
            appSettingsPreferencesDataSource.updateDateOffset(dateOffset)
        }
    }

}