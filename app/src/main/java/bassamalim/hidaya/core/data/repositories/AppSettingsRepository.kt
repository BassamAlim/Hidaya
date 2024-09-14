package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import javax.inject.Inject

class AppSettingsRepository @Inject constructor(
    private val appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource
) {

    fun getLanguage() = appSettingsPreferencesDataSource.getLanguage()

    suspend fun setLanguage(language: Language) {
        appSettingsPreferencesDataSource.updateLanguage(language)
    }

    fun getNumeralsLanguage() = appSettingsPreferencesDataSource.getNumeralsLanguage()

    suspend fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsPreferencesDataSource.updateNumeralsLanguage(numeralsLanguage)
    }

    fun getTheme() = appSettingsPreferencesDataSource.getTheme()

    suspend fun setTheme(theme: Theme) {
        appSettingsPreferencesDataSource.updateTheme(theme)
    }

    fun getTimeFormat() = appSettingsPreferencesDataSource.getTimeFormat()

    suspend fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsPreferencesDataSource.updateTimeFormat(timeFormat)
    }

    fun getDateOffset() = appSettingsPreferencesDataSource.getDateOffset()

    suspend fun setDateOffset(dateOffset: Int) {
        appSettingsPreferencesDataSource.updateDateOffset(dateOffset)
    }

}