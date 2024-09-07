package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingsRepository @Inject constructor(
    private val appSettingsPreferencesDataSource: AppSettingsPreferencesDataSource
) {

    fun getLanguage() = appSettingsPreferencesDataSource.flow.map {
        it.language
    }

    suspend fun setLanguage(language: Language) {
        appSettingsPreferencesDataSource.update { it.copy(
            language = language
        )}
    }

    fun getNumeralsLanguage() = appSettingsPreferencesDataSource.flow.map {
        it.numeralsLanguage
    }

    suspend fun setNumeralsLanguage(numeralsLanguage: Language) {
        appSettingsPreferencesDataSource.update { it.copy(
            numeralsLanguage = numeralsLanguage
        )}
    }

    fun getTheme() = appSettingsPreferencesDataSource.flow.map {
        it.theme
    }

    suspend fun setTheme(theme: Theme) {
        appSettingsPreferencesDataSource.update { it.copy(
            theme = theme
        )}
    }

    fun getTimeFormat() = appSettingsPreferencesDataSource.flow.map {
        it.timeFormat
    }

    suspend fun setTimeFormat(timeFormat: TimeFormat) {
        appSettingsPreferencesDataSource.update { it.copy(
            timeFormat = timeFormat
        )}
    }

    fun getDateOffset() = appSettingsPreferencesDataSource.flow.map {
        it.dateOffset
    }

    suspend fun setDateOffset(dateOffset: Int) {
        appSettingsPreferencesDataSource.update { it.copy(
            dateOffset = dateOffset
        )}
    }

}